package com.example.memo_demo;

import android.content.Context;
import android.net.MacAddress;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wifi.ServerThread;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MemoMain extends AppCompatActivity {
    public static final String TAG = "MemoMain";

    public static String mode = "None";
    public PeerItemAdapter mPeerAdapter;
    /* host part */
    private ServerThread mServer;
    private WifiP2p mP2p;
    private List<WifiP2pDevice> mP2pDeviceList;
    private Set<SocketThread> mClients = new HashSet<SocketThread>();
    private WifiDirectListener mHostListener = new WifiDirectListener() {

        @Override
        public void onChannelDisconnected() {
            log("channel disconnect");
        }

        @Override
        public void onWifiDirectEnabled(boolean enabled) {
            log("wifi direct enabled:" + enabled);
        }

        @Override
        public void onPeersChanged(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            for (WifiP2pDevice device : wifiP2pDeviceList) {
                log("peer address:" + device.deviceAddress);
                log("peer name:" + device.deviceName);
                log("peer owner:" + device.isGroupOwner());
                log("peer status:" + device.status);
                log("--------------------------------");
            }

            mP2pDeviceList.clear();
            mP2pDeviceList.addAll(wifiP2pDeviceList);
            mPeerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConnect(WifiP2pInfo p2pInfo) {
            log("connect group address:" + p2pInfo.groupOwnerAddress.getHostAddress());
            log("connect group owner:" + p2pInfo.isGroupOwner);
            log("connect group formed:" + p2pInfo.groupFormed);
            log("--------------------------------");
        }

        @Override
        public void onDisconnect(WifiP2pInfo p2pInfo) {
            mP2pDeviceList.clear();
            mPeerAdapter.notifyDataSetChanged();

            log("disconnect group address: null");
            log("disconnect group owner:" + p2pInfo.isGroupOwner);
            log("disconnect group formed:" + p2pInfo.groupFormed);
            log("--------------------------------");

        }

        @Override
        public void onSelfChanged(WifiP2pDevice p2pDevice) {
            log("self address:" + p2pDevice.deviceAddress);
            log("self name:" + p2pDevice.deviceName);
            log("self owner:" + p2pDevice.isGroupOwner());
            log("self status:" + WifiP2p.getConnectStatus(p2pDevice.status));
            log("--------------------------------");
        }

        @Override
        public void onDiscoveryChanged(int discoveryState) {
            switch (discoveryState) {
                case WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED:
                    mP2p.discover(new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            log("discover again pass");
                        }

                        @Override
                        public void onFailure(int i) {
                            log("discover again failure");
                        }
                    });
                    break;
                case WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_main);
        Log.e(TAG, "created");

        MemoInfo memoinfo = getIntent().getParcelableExtra(MainActivity.MEMO_EXTRA);
        memoinfo.dump();

        Date currentTime = Calendar.getInstance().getTime();

        mode = memoinfo.type == MainActivity.MEMO_HOST ? "Host" : "Client";
        TextView title = findViewById(R.id.textView3);

        title.setText("Hi " + memoinfo.user + ", you are running as " + mode + " mode");

        TextView textView = findViewById(R.id.textView2);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.append("Status message:\n");
        textView.setTextSize(16);
        textView.append(currentTime.toString());

        EditText editText = findViewById(R.id.editText4);
        editText.setMovementMethod(new ScrollingMovementMethod());

        View.OnFocusChangeListener ofcListener = new MemoFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);

        start_relay(null);
    }

    public void start_relay(View v) {
        if (mode == "Host") {
            host_create_group();
            host_discovery();
            host_establist_connection();


            //Intent intent = new Intent(this, ConnectionHolder.class);
            //startActivity(intent);
        }
    }

    public void stop_relay(View v) {
        if (mode == "Host") {
            host_cancel_group();
        }
    }

    public void host_create_group() {
        // create group
        mP2p = new WifiP2p(this, mHostListener);
        mP2p.onCreate();

        mP2p.createGroup(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("create host group pass");
            }

            @Override
            public void onFailure(int i) {
                log("create host group failure");
            }
        });

        Log.e(TAG, "create group");
    }

    public void host_discovery() {
        //mP2pDeviceList.clear();
        //mPeerAdapter.notifyDataSetChanged();

        mP2p.discover(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("discover pass");
            }

            @Override
            public void onFailure(int i) {
                log("discover failure");
            }
        });
    }

    public void host_establist_connection() {
        mP2pDeviceList = new ArrayList<>();
        mPeerAdapter = new PeerItemAdapter(mP2pDeviceList);
        mPeerAdapter.setListener(new PeerItemAdapter.PeerItemListener() {
            @Override
            public void onItemClick(int position) {
                final WifiP2pDevice device = mP2pDeviceList.get(position);

                if (device.status == WifiP2pDevice.CONNECTED) return;

                WifiP2pConfig config = new WifiP2pConfig.Builder()
                        .setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                        .setNetworkName("DIRECT-GG")
                        .setPassphrase("1234567890")
                        .build();

                mP2p.connect(config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        log("connect onSuccess:" + device.deviceName);
                    }

                    @Override
                    public void onFailure(int reason) {
                        log("connect onFailure:" + device.deviceName);
                    }
                });

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (device.status != WifiP2pDevice.CONNECTED) {
                            WifiP2pConfig config = new WifiP2pConfig.Builder()
                                    .setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                                    .setNetworkName("DIRECT-GG")
                                    .setPassphrase("1234567890")
                                    .build();

                            mP2p.connect(config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    log("connect onSuccess:" + device.deviceName);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    log("connect onFailure:" + device.deviceName);
                                }
                            });
                        }
                    }
                }, 3000);
            }

        });

        Log.e(TAG, "establish connection");

        final TextView textView = findViewById(R.id.textView2);

        RecyclerView recycleView = findViewById(R.id.peer_list_view_2);
        if (null == recycleView)
            Log.e(TAG, "null obj");
        recycleView.setAdapter(mPeerAdapter);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        mServer = new ServerThread();
        mServer.setListener(new SocketListener() {
            @Override
            public void onAdded(SocketThread socketThread) {
                log(String.format("Socket add %s:%d", socketThread.getHostAddress(), socketThread.getPort()));
                mClients.add(socketThread);
            }

            @Override
            public void onRemoved(SocketThread socketThread) {
                log(String.format("Socket remove %s:%d", socketThread.getHostAddress(), socketThread.getPort()));
                mClients.remove(socketThread);
            }

            @Override
            public void onRead(SocketThread socketThread, byte[] message) {
                final String str = String.format("Socket read %s:%d [%s]",
                        socketThread.getHostAddress(),
                        socketThread.getPort(),
                        new String(message, StandardCharsets.UTF_8));
                log(str);
                MemoMain.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, str);
                        textView.setText(str);
                    }
                });
            }
        });
        mServer.start();
        Log.e(TAG, "server thread started");
    }

    public void host_cancel_group() {
        mP2p.disconnect(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("cancel host group pass");
            }

            @Override
            public void onFailure(int i) {
                log("cancel host group failure");
            }
        });

        for (SocketThread client : mClients) {
            client.close();
        }
    }

    public void saveTo(View v) {
        EditText editText = findViewById(R.id.editText4);
        TextView textView = findViewById(R.id.textView2);

        String message = editText.getText().toString();
        textView.setText(message);
        editText.getText().clear();
        Log.e(TAG, "save " + message);
    }

    private class MemoFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.editText4 && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    /**
     * check connection
     */
    public void checkConnection(View v) {
        //Intent intent = new Intent(this, ConnectionHolder.class);
        //startActivity(intent);
    }

    private void log(String message) {
        Log.d(TAG, String.format("[%s] %s", mode, message));
    }

}

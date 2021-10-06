package com.example.memo_demo;

import android.content.Context;
import android.graphics.Color;
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
import java.text.SimpleDateFormat;
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

    /* basic */
    public static String mMode;
    public static String mUser;

    /* host part */
    private ServerThread mServer;
    private WifiP2p mP2p;
    private List<WifiP2pDevice> mP2pDeviceList;
    private Set<SocketThread> mClients = new HashSet<SocketThread>();
    public PeerItemAdapter mPeerAdapter;

    /* client part */
    private SocketThread mClient;
    private boolean mConnected = false;
    private EditText mEditText;
    private boolean firstMsg = true;

    /*  utils */
    enum MEMO_SET_TYPE {
        MEMO_TEXT_SET,
        MEMO_TEXT_APPEND
    };
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
                log("peer: " + device.deviceAddress + " " + device.deviceName + " " + device.isGroupOwner() + " " + device.status);
            }

            if (mP2pDeviceList != null) {
                mP2pDeviceList.clear();
                mP2pDeviceList.addAll(wifiP2pDeviceList);
            }
            if (mPeerAdapter != null)
                mPeerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConnect(WifiP2pInfo p2pInfo) {
            log("Connect group: " + p2pInfo.groupOwnerAddress.getHostAddress() + "  " + p2pInfo.isGroupOwner + " " + p2pInfo.groupFormed);
            String msg = getEditText();
            log(msg);
            for (SocketThread client: mClients)
                client.write(msg);
        }

        @Override
        public void onDisconnect(WifiP2pInfo p2pInfo) {
            if (mP2pDeviceList != null)
                mP2pDeviceList.clear();
            if (mPeerAdapter != null)
                mPeerAdapter.notifyDataSetChanged();

            log("Disconnect group: " + p2pInfo.isGroupOwner + " " + p2pInfo.groupFormed);

        }

        @Override
        public void onSelfChanged(WifiP2pDevice p2pDevice) {
            log("self:" + p2pDevice.deviceAddress + " " + p2pDevice.deviceName + " " + p2pDevice.isGroupOwner() + " " + WifiP2p.getConnectStatus((p2pDevice.status)));
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


    private WifiDirectListener mClientListener = new WifiDirectListener() {

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
            for(WifiP2pDevice device : wifiP2pDeviceList) {
                log("peer address:" + device.deviceAddress);
                log("peer name:" + device.deviceName);
                log("peer owner:" + device.isGroupOwner());
                log("peer status:" + WifiP2p.getConnectStatus(device.status));
                log("--------------------------------");
            }
        }

        @Override
        public void onConnect(WifiP2pInfo p2pInfo) {
            log("connect group address:" + p2pInfo.groupOwnerAddress.getHostAddress());
            log("connect group owner:" + p2pInfo.isGroupOwner);
            log("connect group formed:" + p2pInfo.groupFormed);
            log("--------------------------------" );
            mConnected = true;
            if (mClient ==  null) {
                mClient = new SocketThread(p2pInfo.groupOwnerAddress, new SocketListener() {
                    @Override
                    public void onAdded(SocketThread socketThread) {
                        log(String.format("Socket add: %s %d", socketThread.getHostAddress(), socketThread.getPort()));
                    }

                    @Override
                    public void onRemoved(SocketThread socketThread) {
                        mClient.write(getEditText());
                        log(String.format("Socket remove: %s %d", socketThread.getHostAddress(), socketThread.getPort()));
                    }

                    @Override
                    public void onRead(final SocketThread socketThread, final byte[] message) {
                        MemoMain.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = String.format("[%s:%d] %s",
                                        socketThread.getHostAddress(),
                                        socketThread.getPort(),
                                        new String(message, StandardCharsets.UTF_8));
                                log(msg.toString());

                                if (firstMsg) {
                                    updateEditText(new String(message, StandardCharsets.UTF_8) + "\n", MEMO_SET_TYPE.MEMO_TEXT_SET);
                                    mClient.write("End relay\n");
                                    firstMsg = false;
                                } else {
                                    updateEditText(new String(message, StandardCharsets.UTF_8) + "\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                }
                            }
                        });
                    }
                });
                mClient.start();
                log("client started");
            }
        }

        @Override
        public void onDisconnect(WifiP2pInfo p2pInfo) {
            log("disconnect group address: null");
            log("disconnect group owner:" + p2pInfo.isGroupOwner);
            log("disconnect group formed:" + p2pInfo.groupFormed);
            log("--------------------------------" );
            mConnected = false;
            if (mClient != null) {
                mClient.close();
                mClient = null;
            }
        }

        @Override
        public void onSelfChanged(WifiP2pDevice p2pDevice) {
            log("self address:" + p2pDevice.deviceAddress);
            log("self name:" + p2pDevice.deviceName);
            log("self owner:" + p2pDevice.isGroupOwner());
            log("self status:" + p2pDevice.status);
            log("--------------------------------" );
        }

        @Override
        public void onDiscoveryChanged(int discoveryState) {
            switch(discoveryState) {
                case WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED:
                    if (mConnected) return;
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
        mMode = memoinfo.type == MainActivity.MEMO_HOST ? "Host" : "Client";
        mUser = memoinfo.user;
        TextView title = findViewById(R.id.textView3);
        title.setText("Hi " + mUser + ", you are running as " + mMode + " mMode");


        TextView textView = findViewById(R.id.textView2);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(16);
        textView.setText(getPrefix() + "start\n");

        EditText editText = findViewById(R.id.editText4);
        editText.setMovementMethod(new ScrollingMovementMethod());

        View.OnFocusChangeListener ofcListener = new MemoFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);


        if (mMode == "Host") {
            mP2p = new WifiP2p(this, mHostListener);
        } else if (mMode == "Client") {
            mEditText = findViewById(R.id.editText4);
            mP2p = new WifiP2p(this, mClientListener);
        }
        if (mP2p != null)
        mP2p.onCreate();
        mP2p.disconnect(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("disconnect remove group pass");
            }

            @Override
            public void onFailure(int i) {
                log("disconnect remove group failure");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("destroy");

        if (mP2p != null) {
            mP2p.disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("disconnect remove group pass");
                }

                @Override
                public void onFailure(int i) {
                    log("disconnect remove group failure");
                }
            });

            mP2p.onDestory();
            mP2p = null;
        }
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }

    public void force_sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void host_start() {
        if (mP2p != null) {
            // remove first
            mP2p.disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("cancel host group pass");
                }

                @Override
                public void onFailure(int i) {
                    log("cancel host group failure "  + i);
                }
            });

            //force_sleep(300);

            // create group
            mP2p.createGroup(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("create host group pass");
                }

                @Override
                public void onFailure(int i) {
                    log("create host group failure " + i);
                }
            });
            force_sleep(1000);

            // discovery
            mP2p.discover(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("discover pass");
                }

                @Override
                public void onFailure(int i) {
                    log("discover failure "+ i);
                }
            });
            //force_sleep(300);

            mP2pDeviceList = new ArrayList<>();
            mPeerAdapter = new PeerItemAdapter(mP2pDeviceList);
            mPeerAdapter.setListener(new PeerItemAdapter.PeerItemListener() {
                @Override
                public void onItemClick(int position) {
                    updateStatusText(getPrefix() + "start relay\n",
                            MEMO_SET_TYPE.MEMO_TEXT_APPEND);

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

            final TextView textView = findViewById(R.id.textView2);
            RecyclerView recycleView = findViewById(R.id.peer_list_view_2);
            recycleView.setAdapter(mPeerAdapter);
            recycleView.setLayoutManager(new LinearLayoutManager(this));

            mServer = new ServerThread();
            mServer.setListener(new SocketListener() {
                @Override
                public void onAdded(SocketThread socketThread) {
                    log(String.format("Socket add %s:%d", socketThread.getHostAddress(), socketThread.getPort()));
                    mClients.add(socketThread);


                    MemoMain.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hostWrite(null);                        }
                    });
                }

                @Override
                public void onRemoved(SocketThread socketThread) {
                    log(String.format("Socket remove %s:%d", socketThread.getHostAddress(), socketThread.getPort()));
                    mClients.remove(socketThread);
                }

                @Override
                public void onRead(SocketThread socketThread, byte[] message) {
                    final String str = String.format("%s\n", new String(message, StandardCharsets.UTF_8));
                    log(str);
                    MemoMain.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!firstMsg) {
                                updateStatusText(getPrefix() + "client send back\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                updateEditText(str, MEMO_SET_TYPE.MEMO_TEXT_SET);
                            } else {
                                updateStatusText(getPrefix() + str, MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                firstMsg = false;
                            }
                        }
                    });
                }
            });
            mServer.start();
            log("finish host start");
        }
    }

    public void client_start() {
        if (mP2p != null) {
            firstMsg = true;

            // remove first
            mP2p.disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("cancel host group pass");
                }

                @Override
                public void onFailure(int i) {
                    log("cancel host group failure "  + i);
                }
            });
            force_sleep(1000);
            mP2p.discover(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("client discover pass");
                }

                @Override
                public void onFailure(int i) {
                    mP2p.stopDiscover(null);
                    log("client discover failure");
                }
            });
        }
    }

    public void start_relay(View v) {
        if (mMode == "Host") {
            host_start();
        } else if (mMode == "Client")
            client_start();
    }

    public void host_stop() {
        firstMsg = true;
        if (mP2pDeviceList != null) {
            mP2pDeviceList.clear();
            //mP2pDeviceList = null;
        }
        if (mPeerAdapter != null) {
            mPeerAdapter.notifyDataSetChanged();
            //mPeerAdapter = null;
        }

        /*
        mP2p.stopDiscover(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("stop discover pass");
            }

            @Override
            public void onFailure(int i) {
                log("stop discover failure");
            }
        });
        */

        mP2p.cancelClient(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("cancelClient pass");
            }

            @Override
            public void onFailure(int i) {
                log("cancelClient failure" + i);
            }
        });

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

        if (mServer != null) {
            mServer.close();
            mServer = null;
        }

        if (mP2p != null) {
            //mP2p.onDestory();
            //mP2p = null;
        }
    }

    public void client_stop() {
        if (mP2p != null) {
            updateStatusText(getPrefix() + "stop relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
            if (mClient != null)
                mClient.write(getEditText());

            mP2p.disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("client exit pass");
                }

                @Override
                public void onFailure(int i) {
                    log("client exit failure");
                }
            });
        }
    }
    public void stop_relay(View v) {
        if (mMode == "Host") {
            host_stop();
        } else if (mMode == "Client")
            client_stop();
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
        Log.e(TAG, String.format("%s%s %s", mUser, mMode, message));
    }

    public void hostWrite(View v) {
        String msg = getEditText();
        log(msg);
        for (SocketThread client: mClients)
            client.write(msg);
    }

    public String getTime() {
        SimpleDateFormat now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        return now.format(new Date());
    }

    public String getUser() {
        return "[" + mUser + "] ";
    }

    public String getMode() {
        return "[" + mMode + "] ";
    }

    public String getPrefix() {
        return getTime() + getUser() + getMode();
    }
    public void clearText(View v) {
        updateStatusText(getPrefix() + "reset\n", MEMO_SET_TYPE.MEMO_TEXT_SET);
        updateEditText("", MEMO_SET_TYPE.MEMO_TEXT_SET);
    }

    public void updateStatusText(String msg, MEMO_SET_TYPE type) {
        TextView tv = findViewById(R.id.textView2);
        if (tv == null) {
            log("can't find target to update");
            return;
        }

        switch (type) {
            case MEMO_TEXT_SET:
                tv.setText(msg);
                break;
            case MEMO_TEXT_APPEND:
                tv.append(msg);
                break;
        }
        log("update status: " + type + " " + msg);
    }

    public void updateEditText(String msg, MEMO_SET_TYPE type) {
        EditText et = findViewById(R.id.editText4);
        if (et == null) {
            log("can't find target to update");
            return;
        }

        switch (type) {
            case MEMO_TEXT_SET:
                et.setText(msg);
                break;
            case MEMO_TEXT_APPEND:
                et.append(msg);
                break;
        }
        log("update editor: " + type + " " + msg);
    }

    public String getStatusText() {
        TextView tv = findViewById(R.id.textView2);
            if (tv == null)
                return "";
        return tv.getText().toString();
    }

    public String getEditText() {
        EditText et = findViewById(R.id.editText4);
        if (et == null)
            return "";
        return et.getText().toString();
    }
}

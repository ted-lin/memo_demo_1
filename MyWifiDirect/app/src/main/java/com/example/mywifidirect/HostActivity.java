package com.example.mywifidirect;

import android.net.MacAddress;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wifi.ServerThread;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HostActivity extends AppCompatActivity {

    private static final String TAG = HostActivity.class.getSimpleName();

    private ServerThread mServer;
    private WifiP2p mP2p;

    private PeerItemAdapter mPeerAdapter;
    private List<WifiP2pDevice> mP2pDeviceList;
    private Set<SocketThread> mClients = new HashSet<SocketThread>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Host");

        setContentView(R.layout.host_main);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    }

    private void init() {
        // create group
        mP2p = new WifiP2p(this, mHostListener);
        mP2p.onCreate();

        Button schedule = findViewById(R.id.schedule);
        //schedule.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View view) {


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
            //}
        //});

        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                for(SocketThread client : mClients) {
                    client.close();
                }
            }
        });

        Button discovery = findViewById(R.id.discovery);
        //discovery.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        mP2pDeviceList.clear();
        //        mPeerAdapter.notifyDataSetChanged();

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
        //    }
        //});

        mP2pDeviceList = new ArrayList<>();
        mPeerAdapter = new PeerItemAdapter(mP2pDeviceList);
        mPeerAdapter.setListener(new PeerItemAdapter.PeerItemListener() {
            @Override
            public void onItemClick(int position) {
                final WifiP2pDevice device = mP2pDeviceList.get(position);

                if(device.status == WifiP2pDevice.CONNECTED) return;

                WifiP2pConfig config = new WifiP2pConfig.Builder()
                        .setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                        .setNetworkName("DIRECT-GG")
                        .setPassphrase("1234567890")
                        .build();

                mP2p.connect(config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        log("connect onSuccess:" +  device.deviceName);
                    }

                    @Override
                    public void onFailure(int reason) {
                        log("connect onFailure:" +  device.deviceName);
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
                                    log("connect onSuccess:" +  device.deviceName);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    log("connect onFailure:" +  device.deviceName);
                                }
                            });
                        }
                    }
                }, 3000);
            }

        });

        EditText edit = findViewById(R.id.host_edit);

        Button to = findViewById(R.id.host_to);
        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        final TextView textView = findViewById(R.id.host_text);


        RecyclerView recycleView = findViewById(R.id.peer_list_view);
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
                HostActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(str);
                    }
                });
            }
        });
        mServer.start();

        final EditText editText = findViewById(R.id.host_edit);

        Button toButton = findViewById(R.id.host_to);
        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(SocketThread client : mClients) {
                    client.write(editText.getText().toString());
                }
            }
        });

    }

    private void log(String message) {
        Log.d(TAG, String.format("[Host] %s", message));
    }

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
            for(WifiP2pDevice device : wifiP2pDeviceList) {
                log("peer address:" + device.deviceAddress);
                log("peer name:" + device.deviceName);
                log("peer owner:" + device.isGroupOwner());
                log("peer status:" + device.status);
                log("--------------------------------" );
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
            log("--------------------------------" );
        }

        @Override
        public void onDisconnect(WifiP2pInfo p2pInfo) {
            mP2pDeviceList.clear();
            mPeerAdapter.notifyDataSetChanged();

            log("disconnect group address: null");
            log("disconnect group owner:" + p2pInfo.isGroupOwner);
            log("disconnect group formed:" + p2pInfo.groupFormed);
            log("--------------------------------" );

        }

        @Override
        public void onSelfChanged(WifiP2pDevice p2pDevice) {
            log("self address:" + p2pDevice.deviceAddress);
            log("self name:" + p2pDevice.deviceName);
            log("self owner:" + p2pDevice.isGroupOwner());
            log("self status:" + WifiP2p.getConnectStatus(p2pDevice.status));
            log("--------------------------------" );
        }

        @Override
        public void onDiscoveryChanged(int discoveryState) {
            switch(discoveryState) {
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
}

/*
public static final int AVAILABLE
        Constant Value: 3 (0x00000003)

        CONNECTED
        Added in API level 14

public static final int CONNECTED
        Constant Value: 0 (0x00000000)

        FAILED
        Added in API level 14

public static final int FAILED
        Constant Value: 2 (0x00000002)

        INVITED
        Added in API level 14

public static final int INVITED
        Constant Value: 1 (0x00000001)

        UNAVAILABLE
        Added in API level 14

public static final int UNAVAILABLE
        Constant Value: 4 (0x00000004)
*/
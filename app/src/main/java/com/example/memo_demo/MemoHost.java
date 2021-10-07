package com.example.memo_demo;

import android.net.MacAddress;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wifi.ServerThread;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MemoHost extends EditorActivity {
    private boolean mFirstMsg = true;
    public static final String HOST_EXTRA = "RecyclerViewExtra";
    public static final int HOST_RECYCLER_VIEW_ID = 1;

    private ServerThread mServer;
    private WifiP2p mP2p;
    private List<WifiP2pDevice> mP2pDeviceList;
    private Set<SocketThread> mClients = new HashSet<SocketThread>();
    public PeerItemAdapter mPeerAdapter;
    private boolean mStop = false;

    private static final int RETRY = 3;
    private RecyclerView mRecycleView;

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
                log("peer: " + device.deviceAddress + " " + device.deviceName + " " + device.isGroupOwner() + " " + WifiP2p.getConnectStatus(device.status));
            }

            if (mP2pDeviceList != null) {
                mP2pDeviceList.clear();
                if (!mStop) {
                    mP2pDeviceList.addAll(wifiP2pDeviceList);
                }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        log("xx");
        init(getIntent());

        Button start = findViewById(R.id.start_relay);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("xxxxx");
                start();
            }
        });

        Button stop = findViewById(R.id.stop_relay);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        Button writeTo = findViewById(R.id.write_to);
        writeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = getEditText();
                log(msg);
                for (SocketThread client : mClients)
                    client.write(msg);
            }
        });
    }

    private void disconnect() {
        if (mP2p != null) {
            mP2p.disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("disconnect success");
                }

                @Override
                public void onFailure(int i) {
                    log("disconnect failed " + i);
                }
            });
        }
    }

    private void createGroup() {
        if (mP2p != null) {
            // create group
            mP2p.createGroup(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("create group success");
                }

                @Override
                public void onFailure(int i) {
                    log("create group failed " + i);
                }
            });
        }
    }

    private void discover() {
        if (mP2p != null) {
            // discovery
            mP2p.discover(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("discover pass");
                }

                @Override
                public void onFailure(int i) {
                    log("discover failure:" + WifiP2p.getActionFailure(i));
                }
            });
        }
    }

    private void buildConnectionList() {
        mP2pDeviceList = new ArrayList<>();
        mPeerAdapter = new PeerItemAdapter(mP2pDeviceList);
        mPeerAdapter.setListener(new PeerItemAdapter.PeerItemListener() {
            @Override
            public void onItemClick(final int position) {
                updateStatusText(getPrefix() + "start relay\n",
                        MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                log(getPrefix() + "start relay\n");

                if (mRecycleView != null)
                    mRecycleView.setVisibility(View.INVISIBLE);
                setAllButtonView(View.VISIBLE);

                final WifiP2pDevice device = mP2pDeviceList.get(position);

                log("item status" + device.status);
                if (device.status == WifiP2pDevice.CONNECTED) return;

                WifiP2pConfig config = new WifiP2pConfig.Builder()
                        .setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                        .setNetworkName("DIRECT-GG")
                        .setPassphrase("1234567890")
                        .enablePersistentMode(false)
                        .build();

                mP2p.connect(config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        log("connect onSuccess:" + device.deviceName);
                    }

                    @Override
                    public void onFailure(int reason) {
                        log(String.format("connect onFailure: %s, %s", device.deviceName, WifiP2p.getActionFailure(reason)));
                    }
                });

                // wifi direct feature should be has some issue, that cannot fast response connection when invitation
                // retry it
                for(int i = 0; i < RETRY; ++i) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final WifiP2pDevice device = mP2pDeviceList.get(position);
                            if (device.status == WifiP2pDevice.INVITED) {
                                WifiP2pConfig config = new WifiP2pConfig.Builder()
                                        .setDeviceAddress(MacAddress.fromString(device.deviceAddress))
                                        .setNetworkName("DIRECT-GG")
                                        .setPassphrase("1234567890")
                                        .build();

                                mP2p.connect(config, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        log(String.format("connect again onSuccess: %s, %s", device.deviceName, WifiP2p.getConnectStatus(device.status)));
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        log("connect again onFailure:" + device.deviceName);
                                    }
                                });
                            }
                        }
                    }, 1500 * (i+1));
                }
            }
        });

        mRecycleView = findViewById(R.id.peer_list_view_2);

        setAllButtonView(View.INVISIBLE);
        mRecycleView.setVisibility(View.VISIBLE);
        mRecycleView.setAdapter(mPeerAdapter);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));


    }

    private void serverStart() {
        final TextView textView = findViewById(R.id.textViewStatus);
        if (mServer != null) return;
        mServer = new ServerThread();
        mServer.setListener(new SocketListener() {
            @Override
            public void onAdded(SocketThread socketThread) {
                log(String.format("Socket add %s:%d", socketThread.getHostAddress(), socketThread.getPort()));
                mClients.add(socketThread);

                MemoHost.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = getEditText();
                        log(msg);
                        for (SocketThread client: mClients)
                            client.write(msg);
                    }
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
                log(getPrefix() + str);
                MemoHost.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFirstMsg) {
                            updateStatusText(getPrefix() + "client relay back\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                            updateEditText(str, MEMO_SET_TYPE.MEMO_TEXT_SET);
                        } else {
                            updateStatusText(getPrefix() + str, MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                            mFirstMsg = false;
                        }
                    }
                });
            }
        });
        mServer.start();
    }

    /*
    protected void inflactConnectionList() {
        Intent intent = new Intent(this, MemoHost.class);
        PeerItemAdapterWrapper adapter = new PeerItemAdapterWrapper(HOST_RECYCLER_VIEW_ID, mPeerAdapter);
        intent.putExtra(HOST_EXTRA, adapter);

        startActivityForResult(intent, 0);


    }
    */

    protected void start() {
        mStop = false;
        if (mP2p == null) {
            mP2p = new WifiP2p(this, mHostListener);
            mP2p.onCreate();

        }
        if (mP2p != null) {
            // always disconnect before really use.
            disconnect();
            createGroup();
            discover();
            buildConnectionList();
            serverStart();
            log("start finished");
        } else
            log("mP2p is null");
    }

    protected void stop() {
        mStop = true;
        updateStatusText(getPrefix() + "stop relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);

        mFirstMsg = true;
        if (mP2pDeviceList != null) {
            mP2pDeviceList.clear();
            //mP2pDeviceList = null;
        }
        if (mPeerAdapter != null) {
            mPeerAdapter.notifyDataSetChanged();
            //mPeerAdapter = null;
        }

        if (mP2p != null) {
            discover();
            mP2p.disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("cancel host group pass");
                }

                @Override
                public void onFailure(int i) {
                    log("cancel host group failure:" + WifiP2p.getActionFailure(i));
                }
            });
        }

        for (SocketThread client : mClients) {
            client.close();
        }

        if (mServer != null) {
            mServer.close();
            mServer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
        if (mP2p != null){
            mP2p.onDestory();
        }
    }
}
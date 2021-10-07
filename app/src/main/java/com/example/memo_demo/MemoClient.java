package com.example.memo_demo;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class MemoClient extends MemoMain {
    private SocketThread mClient;
    private boolean mConnected = false;
    private boolean mFirstMsg = true;
    private WifiP2p mP2p;

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
            for (WifiP2pDevice device : wifiP2pDeviceList) {
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
            log("--------------------------------");
            mConnected = true;
            if (mClient == null) {
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
                        MemoClient.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = String.format("[%s:%d] %s",
                                        socketThread.getHostAddress(),
                                        socketThread.getPort(),
                                        new String(message, StandardCharsets.UTF_8));
                                log(msg.toString());

                                if (mFirstMsg) {
                                    updateStatusText(getPrefix() + "got relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                    updateEditText(new String(message, StandardCharsets.UTF_8) + "\n", MEMO_SET_TYPE.MEMO_TEXT_SET);
                                    mClient.write("end relay\n");
                                    mFirstMsg = false;
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
            log("--------------------------------");
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
            log("--------------------------------");
        }

        @Override
        public void onDiscoveryChanged(int discoveryState) {
            switch (discoveryState) {
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

        init(getIntent());

        mP2p = new WifiP2p(this, mClientListener);

        Button start = findViewById(R.id.start_relay);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mClient.write(msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mP2p != null) {
            disconnect();
            mP2p.onDestory();
            mP2p = null;
        }
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
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
                    log("discover failure " + i);
                }
            });
        }
    }

    protected void start() {
        if (mP2p != null) {
            mFirstMsg = true;
            mP2p.onCreate();
            // always disconnect before really use.
            disconnect();
            force_sleep(1000);
            discover();
            log("start finished");
        } else
            log("mP2p is null");
    }

    protected void stop() {
        mFirstMsg = true;
        updateStatusText(getPrefix() + "stop relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
        if (mClient != null)
            mClient.write(getEditText());
        disconnect();
    }
}
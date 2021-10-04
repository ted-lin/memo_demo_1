package com.example.mywifidirect;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wifi.SocketConfig;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import androidx.appcompat.app.AppCompatActivity;

public class ClientActivity extends AppCompatActivity {
    private static final String TAG = ClientActivity.class.getSimpleName();
    private WifiP2p mP2p;
    private boolean mConnected = false;
    private SocketThread mClient;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Client");
        setContentView(R.layout.client_main);

        init();
    }

    private void init() {
        mP2p = new WifiP2p(this, mClientListener);
        mP2p.onCreate();

        Button exit = findViewById(R.id.client_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                if(mClient != null) {
                    mClient.close();
                }
            }
        });

        Button start = findViewById(R.id.client_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        final EditText editText = findViewById(R.id.client_edit);

        Button to = findViewById(R.id.client_to);
        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("client write server" + mClient.getConnectAddress());
                log("client write:" + editText.getText().toString());
                mClient.write(editText.getText().toString());
            }
        });

        mTextView = findViewById(R.id.client_text);

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
        if(mClient != null) {
            mClient.close();
        }
    }

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
                        log(String.format("Socket remove: %s %d", socketThread.getHostAddress(), socketThread.getPort()));
                    }

                    @Override
                    public void onRead(final SocketThread socketThread, final byte[] message) {
                        ClientActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText(String.format("[%s:%d] %s",
                                        socketThread.getHostAddress(),
                                        socketThread.getPort(),
                                        new String(message, StandardCharsets.UTF_8)));
                            }
                        });
                    }
                });
                mClient.start();
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

    private void log(String message) {
        Log.d(TAG, String.format("[Client] %s", message));
    }

}


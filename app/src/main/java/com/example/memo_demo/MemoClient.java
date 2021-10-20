package com.example.memo_demo;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wifi.GroupListener;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.UdpClientThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import jp.wasabeef.richeditor.RichEditor;


public class MemoClient extends EditorActivity {
    private UdpClientThread mUdpThread;
    private SocketThread mClient;
    private boolean mConnected = false;

    private boolean mFirstMsg = true;

    private GroupListener mGroupListener = new GroupListener() {
        @Override
        public void onGroupHostConnect(InetAddress hostAddress, String user) {
            log(String.format("group connect %s,%s", user, hostAddress.getHostAddress()));
            mConnected = true;

            if (mClient == null) {
                mClient = new SocketThread(hostAddress, new SocketListener() {
                    @Override
                    public void onAdded(SocketThread socketThread) {
                        log(String.format("Socket add: %s %d", socketThread.getHostAddress(), socketThread.getPort()));
                    }

                    @Override
                    public void onRemoved(SocketThread socketThread) {
                        if (mClient != null) {
                            mClient.write(StringProcessor.statusToByteArray("client stop relay by remove itself\n"));
                            mClient.write(StringProcessor.htmlToByteArray(getEditText()));
                        }

                        log(String.format("Socket remove: %s %d", socketThread.getHostAddress(), socketThread.getPort()));
                    }

                    @Override
                    public void onRead(final SocketThread socketThread, final byte[] message) {
                        MemoClient.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ReturnMessage ret = StringProcessor.decodeByteArray(message);
                                log("[" + ret.type + "] " + ret.data);

                                if (mFirstMsg) {
                                    updateStatusText(getPrefix() + "got relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                    updateEditText(ret.data + "\n", MEMO_SET_TYPE.MEMO_TEXT_SET);
                                    if (mClient != null) {
                                        mClient.write(StringProcessor.statusToByteArray("end relay\n"));
                                    }
                                    mFirstMsg = false;
                                } else {
                                    updateEditText(ret.data + "\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
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
        public void obGroupHostDisConnect(InetAddress hostAddress, String user) {
            if (!mConnected) return;

            log(String.format("group disconnect %s,%s", user, hostAddress.getHostAddress()));

            mConnected = false;
            disconnect();

            if (mClient != null) {
                mClient.close();
                mClient = null;
            }
        }

        @Override
        public void onGroupClientConnect(InetAddress clientAddress, String user) {

        }

        @Override
        public void onGroupClientDisConnect(InetAddress clientAddress, String user) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(getIntent());
        setTitle("Memo " + mMode);

        mUdpThread = new UdpClientThread(getUser());
        mUdpThread.setListener(mGroupListener);
        mUdpThread.start();

        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {

            @Override
            public void onTextChange(String text) {
                String msg = getEditText();
                if (mClient != null) {
                    mClient.write(StringProcessor.statusToByteArray("client send back\n"));
                    mClient.write(StringProcessor.htmlToByteArray(msg));
                }
            }
        });

        Button start = findViewById(R.id.start_relay);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        start.setText("WAIT");
        Button stop = findViewById(R.id.stop_relay);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        stop.setText("END");
        Button writeTo = findViewById(R.id.write_to);
        writeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = getEditText();
                if (mClient != null) {
                    mClient.write(StringProcessor.statusToByteArray("client send back\n"));
                    mClient.write(StringProcessor.htmlToByteArray(msg));
                }
            }
        });

        Button showList = findViewById(R.id.show_list);
        ((ViewGroup) showList.getParent()).removeView(showList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
    }

    private void disconnect() {
       mUdpThread.leaveGroup();
    }

    private void discover() {
        mUdpThread.joinGroup();
    }

    protected void start() {
        updateStatusText(getPrefix() + "wait relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);

        mFirstMsg = true;
        discover();
        log("start finished");

    }

    protected void stop() {
        mFirstMsg = true;
        updateStatusText(getPrefix() + "stop relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
        if (mClient != null) {
            mClient.write(StringProcessor.statusToByteArray("client stop relay\n"));
            mClient.write(StringProcessor.htmlToByteArray(getEditText()));
        }
        disconnect();
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }
}

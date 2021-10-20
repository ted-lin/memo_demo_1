package com.example.memo_demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.wifi.GroupListener;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.UdpClientThread;

import java.net.InetAddress;

import jp.wasabeef.richeditor.RichEditor;


public class MemoClient extends EditorActivity {
    private UdpClientThread mUdpThread;
    private SocketThread mClient;
    private boolean mConnected = false;

//    private boolean mFirstMsg = true;

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

                                switch (ret.type) {
                                    case StringProcessor.editor:
                                        updateEditText(ret.data, MEMO_SET_TYPE.MEMO_TEXT_SET);
                                        break;
                                    case StringProcessor.clipResult:
                                        updateStatusText(getPrefix() + "got clip\n" + ret.data, MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                        copyToClipBoard(ret.data);
                                        break;
                                    case StringProcessor.status:
                                        updateStatusText(getPrefix() + "got relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                        break;

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

        final Button waitBtn = findViewById(R.id.start_relay);
        waitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitBtn.setText("WAITING");
                waiting();
            }
        });
        waitBtn.setText("WAIT");
        Button stopBtn = findViewById(R.id.stop_relay);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        stopBtn.setText("END");
        Button writeTo = findViewById(R.id.write_to);
        writeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClient != null) {
                    mClient.write(StringProcessor.clipRequestToByteArray());
                }
            }
        });
        writeTo.setText("getServerClip");

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

    protected void waiting() {
        updateStatusText(getPrefix() + "wait relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);

//        mFirstMsg = true;
        discover();
        log("start finished");

    }

    protected void stop() {
//        mFirstMsg = true;
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

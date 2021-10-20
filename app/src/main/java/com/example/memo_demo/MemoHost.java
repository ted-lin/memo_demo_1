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

import com.example.wifi.GroupListener;
import com.example.wifi.ServerThread;
import com.example.wifi.SocketConfig;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.UdpServerThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import org.junit.Assert;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.richeditor.RichEditor;

public class MemoHost extends EditorActivity {
    private boolean mFirstMsg = true;
    public static final String HOST_EXTRA = "RecyclerViewExtra";
    public static final int HOST_RECYCLER_VIEW_ID = 1;

    private ServerThread mServer;

    private UdpServerThread mUdpSeverThread;

    private List<ClientDeviceStatus> mClientList = new ArrayList<ClientDeviceStatus>();
    private Set<SocketThread> mClients = new HashSet<SocketThread>();
    private Set<SocketThread> mPendingRemovedClients = new HashSet<SocketThread>();
    public PeerItemAdapter mPeerAdapter;
    private boolean mStop = false;

    private RecyclerView mRecycleView;

    public static class ClientDeviceStatus {
        public String name;
        public InetAddress address;
        public SocketConfig.WifiDeviceStatus status;

        public ClientDeviceStatus(String name, InetAddress address, SocketConfig.WifiDeviceStatus status) {
            this.name = name;
            this.address = address;
            this.status = status;
        }
    }



    private GroupListener mGroupListener = new GroupListener() {
        @Override
        public void onGroupHostConnect(InetAddress hostAddress, String user) {

            }

        @Override
        public void obGroupHostDisConnect(InetAddress hostAddress, String user) {

        }

        @Override
        public void onGroupClientConnect(InetAddress clientAddress, String user) {
            boolean found = false;
            for(int i = 0; i < mClientList.size(); i++) {
                if (mClientList.get(i).address.getHostAddress().equals(clientAddress.getHostAddress())) {
                    mClientList.get(i).status = SocketConfig.WifiDeviceStatus.Available;
                    found = true;
                }
            }

            if (!found) {
                mClientList.add(new ClientDeviceStatus(user, clientAddress, SocketConfig.WifiDeviceStatus.Available));
            }

            updateClientAdapter();
        }

        @Override
        public void onGroupClientDisConnect(InetAddress clientAddress, String user) {
            int i = 0;
            for(; i < mClientList.size(); i++) {
                if (mClientList.get(i).address.getHostAddress().equals(clientAddress.getHostAddress())) {
                    break;

                }
            }

            if(i!= mClientList.size()) {
                mClientList.remove(i);
            }

            updateClientAdapter();
        }
    };

    private void updateClientAdapter() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPeerAdapter != null)
                    mPeerAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(getIntent());
        setTitle("Memo " + mMode);

        mUdpSeverThread = new UdpServerThread(getUser());
        mUdpSeverThread.setListener(mGroupListener);
        mUdpSeverThread.start();

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
                log(msg);
                updateStatusText(getPrefix() + "write message to clients\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                for (SocketThread client : mClients)
                    client.write(StringProcessor.htmlToByteArray(msg));
            }
        });

        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                String msg = getEditText();
                log(msg);
                updateStatusText(getPrefix() + "write message to clients\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                for (SocketThread client : mClients)
                    client.write(StringProcessor.htmlToByteArray(msg));
            }
        });

        Button showList = findViewById(R.id.show_list);
        showList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecycleView != null && mStop == false) {
                    setAllButtonView(View.INVISIBLE);
                    mRecycleView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mRecycleView != null && mRecycleView.getVisibility() == View.VISIBLE) {
            mRecycleView.setVisibility(View.INVISIBLE);
            setAllButtonView(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void disconnect() {
        mUdpSeverThread.disconnect();
    }


    private void buildConnectionList() {
        mPeerAdapter = new PeerItemAdapter(mClientList);
        mPeerAdapter.setListener(new PeerItemAdapter.PeerItemListener() {
            @Override
            public void onItemClick(final int position) {
                updateStatusText(getPrefix() + "start establish connection\n",
                        MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                log(getPrefix() + "start relay\n");

                if (mRecycleView != null)
                    mRecycleView.setVisibility(View.INVISIBLE);
                setAllButtonView(View.VISIBLE);

                ClientDeviceStatus clientDevice = mClientList.get(position);

                log("item status" + clientDevice.status);
                if (clientDevice.status == SocketConfig.WifiDeviceStatus.Connected) return;

                mUdpSeverThread.connect(clientDevice.address);
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

                for(int i = 0; i < mClientList.size(); i++) {
                    if (mClientList.get(i).address.getHostAddress().equals(socketThread.getHostAddress())) {
                        mClientList.get(i).status = SocketConfig.WifiDeviceStatus.Connected;

                    }
                }

                updateClientAdapter();

                mClients.add(socketThread);
                updateStatusText(getPrefix() + "client added\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                MemoHost.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = getEditText();
                        log(msg);
                        updateStatusText(getPrefix() + "start relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                        for (SocketThread client: mClients)
                            client.write(StringProcessor.htmlToByteArray(msg));
                    }
                });
            }

            @Override
            public void onRemoved(SocketThread socketThread) {
                log(String.format("Socket remove %s:%d", socketThread.getHostAddress(), socketThread.getPort()));
                mPendingRemovedClients.add(socketThread);
                updateStatusText(getPrefix() + "receive client removed\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
            }

            @Override
            public void onRead(SocketThread socketThread, byte[] message) {
                final ReturnMessage ret = StringProcessor.decodeByteArray(message);
                log(getPrefix() + ret.data);
                MemoHost.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (ret.type) {
                            case StringProcessor.status:
                                updateStatusText(getPrefix() + ret.data, MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                                break;
                            case StringProcessor.editor:
                                updateEditText(ret.data, MEMO_SET_TYPE.MEMO_TEXT_SET);
                                break;
                        }
                        log("[" + ret.type + "] " + ret.data);
                    }
                });
            }
        });
        mServer.start();
    }

    protected void start() {
        mStop = false;

        buildConnectionList();
        serverStart();
        log("start finished");

    }

    protected void stop() {
        mStop = true;
        updateStatusText(getPrefix() + "stop relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);

        mFirstMsg = true;
        if (mClientList != null) {
            mClientList.clear();
        }
        if (mPeerAdapter != null) {
            mPeerAdapter.notifyDataSetChanged();
            //mPeerAdapter = null;
        }

        disconnect();

        for (SocketThread client : mClients) {
            client.close();
        }

        for (SocketThread client : mPendingRemovedClients) {
            mClients.remove(client);
        }
        mPendingRemovedClients.clear();

        if (mServer != null) {
            mServer.close();
            mServer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
    }
}
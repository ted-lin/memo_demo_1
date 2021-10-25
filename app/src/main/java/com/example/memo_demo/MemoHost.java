package com.example.memo_demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.wifi.GroupListener;
import com.example.wifi.ServerThread;
import com.example.wifi.SocketConfig;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.UdpServerThread;

import java.net.InetAddress;
import java.util.*;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MemoHost extends EditorActivity {

    private ServerThread mServer;

    private UdpServerThread mUdpSeverThread;

    private final List<ClientDeviceStatus> mClientList = new ArrayList<>();
    private final Set<SocketThread> mClients = new HashSet<>();
    private final Set<SocketThread> mPendingRemovedClients = new HashSet<>();
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

    private void recoverHtmlIfNeeded() {
        String last_string = memoFileManager.quick_load();
        if (!Objects.equals(last_string, "")) {
            dialog.recoverCover((dialog, which) -> mEditor.setHtml(last_string), (dialog, which) -> {
            });
        }
    }

    @Override
    protected void onPause() {
        memoFileManager.quick_save(mEditor.getHtml());
        super.onPause();
    }

    private final GroupListener mGroupListener = new GroupListener() {
        @Override
        public void onGroupHostConnect(InetAddress hostAddress, String user) {

        }

        @Override
        public void obGroupHostDisConnect(InetAddress hostAddress, String user) {

        }

        @Override
        public void onGroupClientConnect(InetAddress clientAddress, String user) {
            boolean found = false;
            for (ClientDeviceStatus clientDeviceStatus : mClientList) {
                if (clientDeviceStatus.address.getHostAddress().equals(clientAddress.getHostAddress())) {
                    clientDeviceStatus.status = SocketConfig.WifiDeviceStatus.Available;
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
            for (; i < mClientList.size(); i++) {
                if (mClientList.get(i).address.getHostAddress().equals(clientAddress.getHostAddress())) {
                    break;

                }
            }

            if (i != mClientList.size()) {
                mClientList.remove(i);
            }

            updateClientAdapter();
        }
    };

    private void updateClientAdapter() {
        this.runOnUiThread(() -> {
            if (mPeerAdapter != null)
                mPeerAdapter.notifyDataSetChanged();
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recoverHtmlIfNeeded();

        init(getIntent());
        setTitle("Memo " + mMode);

        mUdpSeverThread = new UdpServerThread(getUser());
        mUdpSeverThread.setListener(mGroupListener);
        mUdpSeverThread.start();

        Button start = findViewById(R.id.start_relay);
        start.setOnClickListener(v -> start());

        Button stop = findViewById(R.id.stop_relay);
        stop.setOnClickListener(v -> stop());

        Button writeTo = findViewById(R.id.write_to);

        writeTo.setOnClickListener(v -> {
            String msg = getEditText();
            log(msg);
            updateStatusText(getPrefix() + "write message to clients\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
            for (SocketThread client : mClients)
                client.write(StringProcessor.htmlToByteArray(msg));
        });

        mEditor.setOnTextChangeListener(text -> {
            String msg = getEditText();
            //log(msg);
            //if (mClients.size() > 0)
            //    updateStatusText(getPrefix() + "write message to clients\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
            for (SocketThread client : mClients)
                client.write(StringProcessor.htmlToByteArray(msg));
        });

        Button showList = findViewById(R.id.show_list);
        showList.setOnClickListener(v -> {
            if (mRecycleView != null && !mStop) {
                setAllButtonView(View.INVISIBLE);
                mRecycleView.setVisibility(View.VISIBLE);
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
        mPeerAdapter.setListener(position -> {
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
        });

        mRecycleView = findViewById(R.id.peer_list_view_2);

        setAllButtonView(View.INVISIBLE);
        mRecycleView.setVisibility(View.VISIBLE);
        mRecycleView.setAdapter(mPeerAdapter);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));


    }

    private void serverStart() {
        if (mServer != null) return;
        mServer = new ServerThread();
        mServer.setListener(new SocketListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onAdded(SocketThread socketThread) {
                log(String.format("Socket add %s:%d", socketThread.getHostAddress(), socketThread.getPort()));

                for (ClientDeviceStatus clientDeviceStatus : mClientList) {
                    if (clientDeviceStatus.address.getHostAddress().equals(socketThread.getHostAddress())) {
                        clientDeviceStatus.status = SocketConfig.WifiDeviceStatus.Connected;

                    }
                }

                updateClientAdapter();

                mClients.add(socketThread);
                updateStatusText(getPrefix() + "client added\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                MemoHost.this.runOnUiThread(() -> {
                    String msg = getEditText();
                    log(msg);
                    updateStatusText(getPrefix() + "start relay\n", MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                    for (SocketThread client : mClients) {
                        client.write(StringProcessor.statusToByteArray("send relay\n"));
                        client.write(StringProcessor.htmlToByteArray(msg));
                    }
                });
            }

            @SuppressLint("DefaultLocale")
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
                MemoHost.this.runOnUiThread(() -> {
                    switch (ret.type) {
                        case StringProcessor.status:
                            updateStatusText(getPrefix() + ret.data, MEMO_SET_TYPE.MEMO_TEXT_APPEND);
                            break;
                        case StringProcessor.editor:
                            updateEditText(ret.data, MEMO_SET_TYPE.MEMO_TEXT_SET);
                            /* broadcast to other client */
                            for (SocketThread client : mClients) {
                                if (client != socketThread)
                                    client.write(StringProcessor.htmlToByteArray(ret.data));
                            }
                            break;
                        case StringProcessor.clipRequest:
                            sendPaste();
                            break;
                    }
                    //log("[" + StringProcessor.getType(ret.type) + "] " + ret.data);
                });
            }
        });
        mServer.start();
    }

    private void sendPaste() {
        updatePasteUri();
        String result = getPasteText();
        for (SocketThread client : mClients) {
            client.write(StringProcessor.clipResultToByteArray(result));
        }
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

        mClientList.clear();
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
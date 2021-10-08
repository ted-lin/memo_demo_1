package com.example.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;

import java.lang.reflect.Method;

import androidx.core.app.ActivityCompat;

public class WifiP2p {
    Context mContext;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiDirectBroadcastReceiver mReceiver;
    boolean mRegisted = false;

    private ConnectThread mConnectThread = null;


    public WifiP2p(Context context, WifiDirectListener listener) {
        mContext = context;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), listener);
        mReceiver = new WifiDirectBroadcastReceiver(mContext, listener);
    }

    public static String getConnectStatus(int status) {
        switch (status) {
            case 0:
                return "Connected";
            case 1:
                return "Invited";
            case 2:
                return "Failed";
            case 3:
                return "Available";
            case 4:
                return "UnAvailable";
        }

        return "Unknown";
    }

    public static String getActionFailure(int status) {
        switch (status) {
            case 0:
                return "Error";
            case 1:
                return "P2p unsupport";
            case 2:
                return "Busy";
            case 3:
                return "No service sequest";

        }
        return "";
    }

    public void onCreate() {
        if (!mRegisted) {
            mContext.registerReceiver(mReceiver, getIntentFilter());
            mRegisted = true;

            mConnectThread = new ConnectThread();
            mConnectThread.start();
        }
    }

    public void onDestory() {
        if (mRegisted) {
            mContext.unregisterReceiver(mReceiver);
            mRegisted = false;
            mConnectThread.close();
            mConnectThread = null;
        }
    }

    public void discover(final WifiP2pManager.ActionListener listener) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mConnectThread.addTask(new Runnable() {

            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                ProxyListener proxyListener = new ProxyListener();
                proxyListener.setConnect(mConnectThread);
                proxyListener.addListener(listener);
                mManager.discoverPeers(mChannel, proxyListener);
            }
        });
    }

    public void stopDiscover(final WifiP2pManager.ActionListener listener) {
        mConnectThread.addTask(new Runnable() {

            @Override
            public void run() {
                ProxyListener proxyListener = new ProxyListener();
                proxyListener.setConnect(mConnectThread);
                proxyListener.addListener(listener);
                mManager.stopPeerDiscovery(mChannel, proxyListener);
            }
        });

    }

    public void createGroup(final WifiP2pManager.ActionListener listener) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mConnectThread.addTask(new Runnable() {

            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                ProxyListener proxyListener = new ProxyListener();
                proxyListener.setConnect(mConnectThread);
                proxyListener.addListener(listener);
                mManager.createGroup(mChannel, proxyListener);
            }
        });
    }

    public void connect(final WifiP2pConfig config, final WifiP2pManager.ActionListener listener) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mConnectThread.addTask(new Runnable() {

            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                ProxyListener proxyListener = new ProxyListener();
                proxyListener.setConnect(mConnectThread);
                proxyListener.addListener(listener);
                mManager.connect(mChannel, config, proxyListener);
            }
        });
    }

    public void disconnect(final WifiP2pManager.ActionListener listener) {
        mConnectThread.addTask(new Runnable() {

            @Override
            public void run() {
                ProxyListener proxyListener = new ProxyListener();
                proxyListener.setConnect(mConnectThread);
                proxyListener.addListener(listener);
                deletePersistentGroups();
                mManager.removeGroup(mChannel, proxyListener);
            }
        });

    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(mManager, mChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        return intentFilter;
    }

    private static class ProxyListener implements WifiP2pManager.ActionListener {
        private WifiP2pManager.ActionListener mListener = null;
        private ConnectThread mConnectThread = null;

        public void addListener(WifiP2pManager.ActionListener listener) {
            mListener = listener;
        }

        public void setConnect(ConnectThread connectThread) {
            mConnectThread = connectThread;
        }

        @Override
        public void onSuccess() {
            if(mListener != null) {
                mListener.onSuccess();
            }

            if (mConnectThread != null) {
                mConnectThread.notifyState(ConnectThread.CONNECT_PASS);
            }

        }

        @Override
        public void onFailure(int status) {
            if(mListener != null) {
                mListener.onFailure(status);
            }
            if (mConnectThread != null) {
                mConnectThread.notifyState(ConnectThread.CONNECT_FAIL);
            }

        }
    }
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
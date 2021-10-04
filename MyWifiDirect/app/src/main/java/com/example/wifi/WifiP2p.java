package com.example.wifi;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.core.app.ActivityCompat;

public class WifiP2p {
    Context mContext;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiDirectBroadcastReceiver mReceiver;

    public WifiP2p(Context context, WifiDirectListener listener) {
        mContext = context;
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mContext, mContext.getMainLooper(), listener);
        mReceiver = new WifiDirectBroadcastReceiver(mContext, listener);
    }

    public void onCreate() {
        mContext.registerReceiver(mReceiver, getIntentFilter());
    }

    public void onDestory() {
        mContext.unregisterReceiver(mReceiver);
    }

    public void discover(WifiP2pManager.ActionListener listener) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mManager.discoverPeers(mChannel, listener);
    }

    public void stopDiscover(WifiP2pManager.ActionListener listener) {
        mManager.stopPeerDiscovery(mChannel, listener);
    }

    public void createGroup(WifiP2pManager.ActionListener listener) {
        mManager.createGroup(mChannel, listener);
    }

    public void connect(WifiP2pConfig config, WifiP2pManager.ActionListener listener) {
        mManager.connect(mChannel, config, listener);
    }

    public void disconnect(WifiP2pManager.ActionListener listener) {
        mManager.removeGroup(mChannel, listener);
    }

    public static String getConnectStatus(int status) {
        switch(status) {
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


    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
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
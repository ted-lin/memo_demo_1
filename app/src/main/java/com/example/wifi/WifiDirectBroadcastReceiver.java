package com.example.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    Context mContext;
    WifiDirectListener mListener;

    public WifiDirectBroadcastReceiver(Context context, WifiDirectListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        if (mListener == null) return;

        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    mListener.onWifiDirectEnabled(true);
                } else {
                    mListener.onWifiDirectEnabled(false);
                    List<WifiP2pDevice> wifiP2pDeviceList = new ArrayList<>();
                    mListener.onPeersChanged(wifiP2pDeviceList);
                }
                break;
            }

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                WifiP2pDeviceList peerList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                mListener.onPeersChanged(peerList.getDeviceList());

                break;
            }
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

                if (isWifiConnected() && p2pInfo.groupOwnerAddress != null) {
                    mListener.onConnect(p2pInfo);
                } else {
                    mListener.onDisconnect(p2pInfo);
                }
                break;
            }

            case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION: {
                int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
                mListener.onDiscoveryChanged(discoveryState);
            }

            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                WifiP2pDevice wifiP2pDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                mListener.onSelfChanged(wifiP2pDevice);
                break;
            }
        }
    }

    private Boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }
}

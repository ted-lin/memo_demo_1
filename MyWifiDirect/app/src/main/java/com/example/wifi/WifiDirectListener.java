package com.example.wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;

public interface WifiDirectListener extends WifiP2pManager.ChannelListener {
    void onWifiDirectEnabled(boolean enabled);
    void onPeersChanged(Collection<WifiP2pDevice> wifiP2pDeviceList);
    void onConnect(WifiP2pInfo p2pInfo);
    void onDisconnect(WifiP2pInfo p2pInfo);
    void onSelfChanged(WifiP2pDevice p2pDevice);
    void onDiscoveryChanged(int discoveryState);
}

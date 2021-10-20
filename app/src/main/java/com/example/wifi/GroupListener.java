package com.example.wifi;

import java.net.InetAddress;

public interface GroupListener {
    void onGroupHostConnect(InetAddress hostAddress, String user);
    void obGroupHostDisConnect(InetAddress hostAddress, String user);
    void onGroupClientConnect(InetAddress clientAddress, String user);
    void onGroupClientDisConnect(InetAddress clientAddress, String user);

}

package com.example.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SocketConfig {
    public static final int SOCKET_PORT = 9999;
    public static final String END_STR = "`/30175G~";

    //any address between 224.0.0.0 to 239.255.255.255
    public static final String GROUP_ADDRESS = "237.251.252.253";
    public static final int MULTI_CAST_PORT = 8763;
    public static final String JOIN_GROUP_MSG = "JOIN__MEMO_GROUP__MSG";
    public static final String LEAVE_GROUP_MSG = "LEAVE__MEMO_GROUP__MSG";
    public static final String CONNECT_MSG = "connect_memo_host";
    public static final String DISCONNECT_MSG = "disconnect_moemo_host";

    public enum WifiDeviceStatus {
        Available("Available"),
        Connected("Connected");

        private final String value;

        private WifiDeviceStatus(String s) {
            value = s;
        }

        public String toString() {
            return value;
        }
    }

    public static InetAddress getHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while(nias.hasMoreElements()) {
                    InetAddress ia= (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
    }

    public static boolean isWifiAvailable (Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            for (Network net : connectivityManager.getAllNetworks()) {
                NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(net);
                if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                    return true;
            }
        }
        return false;
    }
}

package com.example.wifi;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.security.acl.Group;
import java.util.Locale;

import static com.example.wifi.SocketConfig.CONNECT_MSG;
import static com.example.wifi.SocketConfig.DISCONNECT_MSG;
import static com.example.wifi.SocketConfig.GROUP_ADDRESS;
import static com.example.wifi.SocketConfig.JOIN_GROUP_MSG;
import static com.example.wifi.SocketConfig.LEAVE_GROUP_MSG;
import static com.example.wifi.SocketConfig.MULTI_CAST_PORT;

public class UdpClientThread extends Thread {

    private static final String TAG = UdpClientThread.class.getSimpleName();

    private String mUser;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;
    private MulticastSocket mSocket = null;
    private GroupListener mListener = null;


    public UdpClientThread(String user) {
        mUser = user;
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void setListener(GroupListener listener) {
        mListener = listener;
    }

    public void joinGroup() {
        JoinRunnable joinRunnable = new JoinRunnable(mUser, mSocket);
        mHandler.post(joinRunnable);
    }

    public void leaveGroup() {
        LeaveRunnable leaveRunnable = new LeaveRunnable(mUser, mSocket);
        mHandler.post(leaveRunnable);
    }

    @Override
    public void run() {
        try {
            mSocket = new MulticastSocket(MULTI_CAST_PORT);
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            mSocket.joinGroup(group);

            while (true) {
                byte[] recvBuffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);
                mSocket.receive(packet);


                String recvData = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                String[] parseData = recvData.split(",");

                String msg = parseData[0];
                if (!msg.equals(CONNECT_MSG) && !msg.equals(DISCONNECT_MSG)) {
                    continue;
                }

                String clientAddress = parseData[1];
                String host = parseData[2];
                if (host == null || host.isEmpty()) {
                    host = "Host";
                }

                Log.e(TAG, String.format("%s, %s, %s, %s", msg, clientAddress, host, packet.getAddress().getHostAddress()));
                Log.e(TAG, "client:" + SocketConfig.getHostAddress());

                if (mListener != null) {
                    if (CONNECT_MSG.equals(msg)) {
                        if (clientAddress.equals(SocketConfig.getHostAddress().getHostAddress())) {
                            mListener.onGroupHostConnect(packet.getAddress(), host);
                        }
                    } else if (DISCONNECT_MSG.equals(msg)) {
                        mListener.obGroupHostDisConnect(packet.getAddress(), host);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] generateMsgPackage(String user, String msg) {
        return String.format("%s,%s", msg, user).getBytes(StandardCharsets.UTF_8);
    }

    private static class JoinRunnable implements Runnable {
        private String mUser;
        private MulticastSocket mSocket = null;

        public JoinRunnable(String user, MulticastSocket socket) {
            mUser = user;
            mSocket = socket;
        }

        @Override
        public void run() {
            try {
                if (mSocket != null) {
                    InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
                    byte[] buf = generateMsgPackage(mUser, JOIN_GROUP_MSG);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MULTI_CAST_PORT);
                    mSocket.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class LeaveRunnable implements Runnable {
        private String mUser;
        private MulticastSocket mSocket = null;

        public LeaveRunnable(String user, MulticastSocket socket) {
            mUser = user;
            mSocket = socket;
        }

        @Override
        public void run() {
            try {
                if (mSocket != null) {
                    InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
                    byte[] buf = generateMsgPackage(mUser, LEAVE_GROUP_MSG);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MULTI_CAST_PORT);
                    mSocket.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

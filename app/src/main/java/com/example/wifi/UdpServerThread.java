package com.example.wifi;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

import static com.example.wifi.SocketConfig.CONNECT_MSG;
import static com.example.wifi.SocketConfig.DISCONNECT_MSG;
import static com.example.wifi.SocketConfig.GROUP_ADDRESS;
import static com.example.wifi.SocketConfig.JOIN_GROUP_MSG;
import static com.example.wifi.SocketConfig.LEAVE_GROUP_MSG;
import static com.example.wifi.SocketConfig.MULTI_CAST_PORT;

public class UdpServerThread extends Thread {
    private static final String TAG = UdpServerThread.class.getSimpleName();

    private String mUser;
    private MulticastSocket mSocket = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;


    private boolean mExit = false;
    private GroupListener mListener = null;

    public UdpServerThread(String user) {
        mUser = user;
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void setListener(GroupListener listener) {
        mListener = listener;
    }

    public void connect(InetAddress address) {
        ConnectRunnable connectRunnable = new ConnectRunnable(mUser, mSocket, address);
        mHandler.post(connectRunnable);
    }

    public void disconnect() {
        LeaveRunnable leaveRunnable = new LeaveRunnable(mUser, mSocket);
        mHandler.post(leaveRunnable);
    }

    @Override
    public void run() {
        while(!mExit) {
            try  {
                mSocket = new MulticastSocket(MULTI_CAST_PORT);
                InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
                mSocket.joinGroup(group);

                byte[] recvBuffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);
                mSocket.receive(packet);

                String data = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                String[] parseData = data.split(",");

                String msg = parseData[0];
                if (!msg.equals(JOIN_GROUP_MSG) && !msg.equals(LEAVE_GROUP_MSG)) {
                    continue;
                }

                String host = parseData[1];
                if (host == null || host.isEmpty()) {
                    host = "Client";
                }

                Log.e(TAG, String.format("%s, %s, %s, %s", TAG, msg, host, packet.getAddress().getHostAddress()));
                Log.e(TAG, "server:" + SocketConfig.getHostAddress());

                if (mListener != null) {
                    if (JOIN_GROUP_MSG.equals(msg)) {
                        mListener.onGroupClientConnect(packet.getAddress(), host);
                    } else if (LEAVE_GROUP_MSG.equals(msg)) {
                        mListener.onGroupClientDisConnect(packet.getAddress(), host);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mSocket != null) {
            mSocket.close();
        }
    }

    private static byte[] generateMsgPackage(String user, String address, String msg) {
        return String.format("%s,%s,%s", msg, address, user).getBytes(StandardCharsets.UTF_8);
    }

    private static class ConnectRunnable implements Runnable {
        private String mUser;
        private MulticastSocket mSocket = null;
        private InetAddress mClient = null;

        public ConnectRunnable(String user, MulticastSocket socket, InetAddress address) {
            mUser = user;
            mSocket = socket;
            mClient = address;
        }

        @Override
        public void run() {
            try {
                if (mSocket != null) {
                    InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
                    byte[] buf = generateMsgPackage(mUser, mClient.getHostAddress(), CONNECT_MSG);
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
                    byte[] buf = generateMsgPackage(mUser, SocketConfig.getHostAddress().getHostAddress(), DISCONNECT_MSG);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MULTI_CAST_PORT);
                    mSocket.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

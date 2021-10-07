package com.example.wifi;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerThread extends Thread {
    private static final String TAG = ServerThread.class.getSimpleName();
    private ServerSocket mServerSocket;
    private SocketListener mListener = null;

    public ServerThread() {

    }


    public void setListener(SocketListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        try {
            Log.e(TAG, "server try to run");
            mServerSocket = new ServerSocket(SocketConfig.SOCKET_PORT);
            while (!mServerSocket.isClosed()) {

                Socket client = mServerSocket.accept();
                Log.d(TAG, "[Server] client accept");
                Log.d(TAG, "[Server] client: " + client.getInetAddress().getHostAddress());
                Log.d(TAG, "[Server] client: " + client.getPort());
                SocketThread socketThread = new SocketThread(client, mListener);
                socketThread.start();

            }

        } catch (SocketException e) {
            try {
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
                Log.d(TAG, "[Server] server socket close");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

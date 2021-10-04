package com.example.wifi;

public interface SocketListener {
    void onAdded(SocketThread socketThread);

    void onRemoved(SocketThread socketThread);

    void onRead(SocketThread socketThread, byte[] message);
}

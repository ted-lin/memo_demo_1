package com.example.wifi;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.wifi.SocketConfig.END_CHAR;

public class SocketThread extends Thread {
    private static final String TAG = SocketThread.class.getSimpleName();
    private InetAddress mAddress;
    private Socket mSocket = null;
    private SocketListener mListener = null;
    private WriterThread mWriter = null;

    public SocketThread(InetAddress address, SocketListener listener) {
        mAddress = address;
        mListener = listener;
    }

    public SocketThread(Socket socket, SocketListener listener) {
        mSocket = socket;
        mListener = listener;
    }

    public String getConnectAddress() {
        return mSocket.getInetAddress().getHostAddress();
    }


    public void write(String message) {
        if (mSocket == null || mSocket.isClosed()) return;
        if (mWriter != null) {
            mWriter.setMessage(message);
        }
    }

    public String getHostAddress() {
        if (mSocket != null) {
            return mSocket.getInetAddress().getHostAddress();
        }

        return "NULL";
    }

    public int getPort() {
        if (mSocket != null) {
            return mSocket.getPort();
        }

        return -1;
    }

    public void close() {
        try {
            if (mSocket != null) {
                if (mListener != null) {
                    mListener.onRemoved(this);
                }
                if (mWriter != null) {
                    mWriter.close();
                }
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        try {
            if (mSocket == null) {
                mSocket = new Socket(mAddress, SocketConfig.SOCKET_PORT);
            }
            if (mListener != null) {
                mListener.onAdded(this);
            }
            mWriter = new WriterThread(mSocket);
            mWriter.start();
            bis = new BufferedInputStream(mSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "SocketThread start run:" + mSocket.isClosed());
        while (!mSocket.isClosed()) {

            try {

                ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                int c;
                while ((c = bis.read()) != -1) {
                    if (c == END_CHAR) break;
                    byteArrayOS.write(c);
                }

                if (c == -1) {
                    Log.d(TAG, "ScoketClosed");
                    close();
                    break;
                }
                byte[] message = byteArrayOS.toByteArray();
                String str = new String(message, StandardCharsets.UTF_8);

                Log.d(TAG, String.format("SocketThread read: %s, addr:%s", str, mSocket.getInetAddress().getHostAddress()));
                if (mListener != null) {
                    mListener.onRead(this, message);
                }


            } catch (SocketException e) {
                Log.d(TAG, "SocketExcetion:" + e.getMessage());
                close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (bis != null) {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class WriterThread extends Thread {
        private List<byte[]> mMessages = new ArrayList<byte[]>();
        private boolean mExit = false;
        private Socket mSocket = null;


        public WriterThread(Socket socket) {
            mSocket = socket;
        }

        public void setMessage(String message) {
            synchronized (this) {
                mMessages.add(message.getBytes(StandardCharsets.UTF_8));
                this.notifyAll();
            }
        }

        public void setMessage(byte[] bytes) {

        }

        public void close() {
            mExit = true;
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void run() {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(mSocket.getOutputStream());
                while (!mExit) {
                    List<byte[]> next = new ArrayList<byte[]>();
                    synchronized (this) {
                        try {
                            if (mExit) {
                                break;
                            }
                            if (mMessages.isEmpty()) {
                                wait();
                            } else {
                                List<byte[]> tmp = mMessages;
                                mMessages = next;
                                next = tmp;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    for (byte[] message : next) {
                        bos.write(message);
                        bos.write(END_CHAR);
                        Log.d(TAG, String.format("SocketThread write: %s, addr:%s", message, mSocket.getInetAddress().getHostAddress()));
                    }
                    bos.flush();

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }

}

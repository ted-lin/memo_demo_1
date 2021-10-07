package com.example.wifi;

import java.util.ArrayList;
import java.util.List;

public class ConnectThread extends Thread {
    public static final int CONNECT_UNINIT = -1;
    public static final int CONNECT_PASS = 0;
    public static final int CONNECT_FAIL = 1;
    public int mStatus = CONNECT_UNINIT;

    private static final int ERR_RETRY = 2;

    private List<Runnable> mRunList = new ArrayList<Runnable>();
    private boolean mExit = false;

    public void addTask(Runnable runnable) {
        synchronized (this) {
            mRunList.add(runnable);
            this.notifyAll();
        }
    }

    public void close() {
        mExit = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public boolean isClose() {
        return mExit;
    }

    @Override
    public void run() {
        while(!isClose()) {
            List<Runnable> localRunList = new ArrayList<Runnable>();
            synchronized (this) {
                if(mRunList.isEmpty()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<Runnable> tmp = mRunList;
                mRunList = localRunList;
                localRunList = tmp;

            }

            for(Runnable runnable : localRunList) {

                for(int i = 0; i < ERR_RETRY; i++) {
                    resetState();
                    runnable.run();
                    while (getStatus() == CONNECT_UNINIT) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(getStatus() == CONNECT_PASS) break;
                }
            }
        }

    }

    public int getStatus() {
        return mStatus;
    }

    public void resetState() {
        mStatus = CONNECT_UNINIT;
    }

    public void notifyState(int status) {
        mStatus = status;
    }
}

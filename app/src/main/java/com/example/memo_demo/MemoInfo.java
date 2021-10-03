package com.example.memo_demo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class MemoInfo implements Parcelable {
    public static final Parcelable.Creator<MemoInfo> CREATOR =
            new Parcelable.Creator<MemoInfo>() {

                @Override
                public MemoInfo createFromParcel(Parcel source) {
                    return new MemoInfo(source.readInt(), source.readString());
                }

                @Override
                public MemoInfo[] newArray(int size) {
                    return new MemoInfo[size];
                }
            };
    static final String TAG = "MemoInfo";
    int type = 0;
    String msg;

    public MemoInfo(int t, String s) {
        type = t;
        msg = s;
        Log.e(TAG, "creator: type: " + type + ", msg: " + msg);
    }

    public void dump() {
        Log.e(TAG, "dump: " + type + " " + msg);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(msg);
    }
}
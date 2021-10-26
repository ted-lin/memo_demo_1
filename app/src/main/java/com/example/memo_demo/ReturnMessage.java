package com.example.memo_demo;

import com.google.protobuf.ByteString;

public class ReturnMessage {
    public int type;
    public String data;
    public byte[] bytes;

    ReturnMessage(int _type, String _data) {
        type = _type;
        data = _data;
        bytes = null;
    }

    ReturnMessage(int _type, String _data, byte[] _bytes) {
        type = _type;
        data = _data;
        bytes = _bytes;
    }

    ReturnMessage(int _type, String _data, ByteString _bytes) {
        type = _type;
        data = _data;
        bytes = _bytes.toByteArray();
    }
}

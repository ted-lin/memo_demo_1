package com.example.memo_demo;

import com.google.protobuf.ByteString;

public class ReturnMessage {
    public int type;
    public String data;
    public byte[] bytes;
    public int messageId;

    ReturnMessage(int _type, String _data, int _messageId) {
        type = _type;
        data = _data;
        bytes = null;
        messageId = _messageId;
    }

    ReturnMessage(int _type, String _data, byte[] _bytes) {
        type = _type;
        data = _data;
        bytes = _bytes;
    }

    ReturnMessage(int _type, String _data, ByteString _bytes, int _messageId) {
        type = _type;
        data = _data;
        bytes = _bytes.toByteArray();
        messageId = _messageId;
    }
}

package com.example.memo_demo;

import com.example.protos.Data;
import com.google.protobuf.InvalidProtocolBufferException;

public class StringProcessor {

    static final int status = 0;
    static final int editor = 1;

    static byte[] htmlToByteArray(String data) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setData(data)
                .setMessageType(editor)
                .build();
        return editorMessage.toByteArray();
    }

    static byte[] statusToByteArray(String data) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setData(data)
                .setMessageType(status)
                .build();
        return editorMessage.toByteArray();
    }

    static ReturnMessage decodeByteArray(byte[] msg) {
        try {
            Data.EditorMessage editorMessage = Data.EditorMessage.parseFrom(msg);
            return new ReturnMessage(editorMessage.getMessageType(), editorMessage.getData());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return new ReturnMessage(-1, "");
        }
    }
}

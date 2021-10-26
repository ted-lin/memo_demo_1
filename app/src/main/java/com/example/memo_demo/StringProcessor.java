package com.example.memo_demo;

import com.example.protos.Data;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class StringProcessor {

    static final int status = 0;
    static final int editor = 1;
    static final int clipResult = 2;
    static final int clipRequest = 3;
    static final int img = 4;

    static String getType(int type) {
        switch (type) {
            case status:
                return "Status";
            case editor:
                return "Editor";
            case clipResult:
                return "ClipResult";
            case clipRequest:
                return "ClipRequest";
            case img:
                return "img";
        }
        return "Unknown";
    }

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

    static byte[] clipResultToByteArray(String data) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setData(data)
                .setMessageType(clipResult)
                .build();
        return editorMessage.toByteArray();
    }

    static byte[] clipRequestToByteArray() {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setMessageType(clipRequest)
                .build();
        return editorMessage.toByteArray();
    }

    static byte[] imgToByteArray(byte[] bytes) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setMessageType(img)
                .setImageData(ByteString.copyFrom(bytes))
                .build();
        return editorMessage.toByteArray();
    }

    static ReturnMessage decodeByteArray(byte[] msg) {
        try {
            Data.EditorMessage editorMessage = Data.EditorMessage.parseFrom(msg);
            return new ReturnMessage(editorMessage.getMessageType(), editorMessage.getData(), editorMessage.getImageData());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return new ReturnMessage(-1, "");
        }
    }
}

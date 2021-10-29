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
    static final int clientReturn = 5;
    static final int editorWithId = 6;

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
            case editorWithId:
                return "editorWithId";
            case clientReturn:
                return "clientReturn";
            default:
                return "Unknown";
        }
    }

    static byte[] htmlToByteArray(String data) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setData(data)
                .setMessageType(editor)
                .build();
        return editorMessage.toByteArray();
    }

    static byte[] htmlToByteArrayWithMsgId(String data, int msgId) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setData(data)
                .setMessageType(editorWithId)
                .setMessageId(msgId)
                .build();
        return editorMessage.toByteArray();
    }

    static byte[] editorRetMsg(int msgId) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setMessageType(clientReturn)
                .setMessageId(msgId)
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

    static byte[] clipResultToByteArray(String data, int msgId) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setData(data)
                .setMessageId(msgId)
                .setMessageType(clipResult)
                .build();
        return editorMessage.toByteArray();
    }

    static byte[] clipRequestToByteArray(int msgId) {
        Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                .setMessageType(clipRequest)
                .setMessageId(msgId)
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
            return new ReturnMessage(editorMessage.getMessageType(), editorMessage.getData(), editorMessage.getImageData(), editorMessage.getMessageId());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return new ReturnMessage(-1, "", -1);
        }
    }
}

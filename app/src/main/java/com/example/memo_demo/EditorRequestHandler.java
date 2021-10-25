package com.example.memo_demo;

import android.net.Uri;

public class EditorRequestHandler {
    public static final int WRITE_REQUEST_CODE_HTML = 43;
    public static final int WRITE_REQUEST_CODE = 42;
    public static final int READ_REQUEST_CODE_HTML = 41;
    public static final int READ_REQUEST_CODE = 40;
    public EditorActivity editorActivity;
    public MemoFileManager memoFileManager;

    public EditorRequestHandler(EditorActivity editorActivity, MemoFileManager memoFileManager) {
        this.editorActivity = editorActivity;
        this.memoFileManager = memoFileManager;

    }

    public void handleRequestCode(int requestCode, Uri uri) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
            case WRITE_REQUEST_CODE_HTML:
                String input = requestCode == WRITE_REQUEST_CODE ?
                        TextHelper.toPlainTxt(editorActivity.mEditor.getHtml()): editorActivity.mEditor.getHtml();
                memoFileManager.saveToFile(uri, input);
                break;
            case READ_REQUEST_CODE:
            case READ_REQUEST_CODE_HTML:
                boolean isHtml = requestCode == READ_REQUEST_CODE_HTML;
                String output = memoFileManager.loadFromFile(uri, isHtml);
                editorActivity.mEditor.setHtml(output);
                break;
            default:
                break;
        }
    }
}

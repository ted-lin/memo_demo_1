package com.example.memo_demo;

import android.content.Intent;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MemoFileManager {
    private final EditorActivity editorActivity;
    public MemoFileManager(EditorActivity editorActivity) {
        this.editorActivity = editorActivity;
    }

    public void saveToFile(Uri uri, String text) {
        try {
            byte[] bytes = text.getBytes();
            FileOutputStream fos = (FileOutputStream) editorActivity.getContentResolver().openOutputStream(uri);
            if (fos != null) {
                fos.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String loadFromFile(Uri uri, Boolean isHtml) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            byte[] bytes = new byte[1024];
            FileInputStream fins = (FileInputStream) editorActivity.getContentResolver().openInputStream(uri);
            if (fins != null) {
                while (fins.read(bytes) != -1) {
                    stringBuilder.append(new String(bytes));
                }
            }
            String fileContents = stringBuilder.toString();
            if (isHtml)
                return fileContents;
            else
                return TextHelper.toHtml(fileContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void openFile(String mimeType, boolean isHtml) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        if (isHtml) {
            editorActivity.startActivityForResult(intent, EditorRequestHandler.READ_REQUEST_CODE_HTML);
        } else {
            editorActivity.startActivityForResult(intent, EditorRequestHandler.READ_REQUEST_CODE);
        }
    }

    public void createFile(String mimeType, String fileName, boolean isHtml) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        if (isHtml) {
            editorActivity.startActivityForResult(intent, EditorRequestHandler.WRITE_REQUEST_CODE_HTML);
        } else {
            editorActivity.startActivityForResult(intent, EditorRequestHandler.WRITE_REQUEST_CODE);
        }
    }
}

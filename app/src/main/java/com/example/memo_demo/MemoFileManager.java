package com.example.memo_demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.*;

public class MemoFileManager {
    private final EditorActivity editorActivity;
    private final String quick_save_file_name;
    private final File externalFilesDir;

    public MemoFileManager(EditorActivity editorActivity) {
        this.editorActivity = editorActivity;
        quick_save_file_name = "quick_save.html";
        externalFilesDir = editorActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    }


    protected String quick_load() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File inPutFile = new File(externalFilesDir, quick_save_file_name);
            StringBuilder stringBuilder = new StringBuilder();
            try {
                FileReader fr = new FileReader(inPutFile);
                BufferedReader br = new BufferedReader(fr);
                String line = br.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuilder.toString();
        }
        return "";
    }

    protected void quick_save(String str) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File outPutFile = new File(externalFilesDir, quick_save_file_name);
            try {
                FileOutputStream fos = new FileOutputStream(outPutFile);
                byte[] bytes;
                bytes = str.getBytes();
                fos.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

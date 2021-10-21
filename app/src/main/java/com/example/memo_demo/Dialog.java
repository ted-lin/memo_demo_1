package com.example.memo_demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

public class Dialog {
    EditorActivity editorActivity;

    public Dialog(EditorActivity editorActivity) {
        this.editorActivity = editorActivity;
    }

    void checkDialog(String positiveString, String negativeString, String title,
                     DialogInterface.OnClickListener positiveListener,
                     DialogInterface.OnClickListener negativeListener) {
        // TODO need to fix link problem
        AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity);
        builder.setCancelable(false);
        View view = editorActivity.getLayoutInflater().inflate(R.layout.dialog_new, null, false);
        builder.setView(view);
        builder.setTitle(title);
        builder.setPositiveButton(positiveString, positiveListener);
        builder.setNegativeButton(negativeString, negativeListener);
        builder.create().show();
    }

    void showLinkDialog() {
        // TODO need to fix link problem
        AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity);
        builder.setCancelable(false);

        View view = editorActivity.getLayoutInflater().inflate(R.layout.dialog_link, null, false);
        final EditText link = view.findViewById(R.id.link_url);
        final EditText editText = view.findViewById(R.id.link_content);
        builder.setView(view);
        builder.setTitle("Insert link");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = link.getText().toString().trim();
                String content = editText.getText().toString().trim();
                if (TextUtils.isEmpty(url) || TextUtils.isEmpty(content)) {

                    return;
                }
                editorActivity.mEditor.insertLink(url, content);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DO NOTHING HERE
            }
        });

        builder.create().show();
    }
}

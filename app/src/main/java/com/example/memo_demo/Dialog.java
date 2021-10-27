package com.example.memo_demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Dialog {
    EditorActivity editorActivity;

    public Dialog(EditorActivity editorActivity) {
        this.editorActivity = editorActivity;
    }

    @SuppressLint("SetTextI18n")
    void checkBox(DialogInterface.OnClickListener positiveListener,
                  DialogInterface.OnClickListener negativeListener, String title, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity);
        builder.setCancelable(false);
        View view = editorActivity.getLayoutInflater().inflate(R.layout.dialog_check_box, null, false);
        ((TextView) view.findViewById(R.id.checkboxTextView)).setText(text);
        builder.setView(view);
        builder.setTitle(title);
        builder.setPositiveButton("OK", positiveListener);
        builder.setNegativeButton("cancel", negativeListener);
        builder.create().show();
    }

    void showLink(String content) {
        // TODO need to fix link problem
        AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity);
        builder.setCancelable(false);

        View view = editorActivity.getLayoutInflater().inflate(R.layout.dialog_link, null, false);
        final EditText link = view.findViewById(R.id.link_url);
        final EditText editText = view.findViewById(R.id.link_content);
        builder.setView(view);
        builder.setTitle("Insert link");
        editText.setText(content);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String url = link.getText().toString().trim();
            String content1 = editText.getText().toString().trim();
            if (TextUtils.isEmpty(url) || TextUtils.isEmpty(content1)) {

                return;
            }
            editorActivity.mEditor.insertLink(url, content1);
        });

        builder.setNegativeButton("cancel", (dialog, which) -> {
            // DO NOTHING HERE
        });

        builder.create().show();
    }

    void select(DialogInterface.OnClickListener onClickListener, String[] listItems, String chooseType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(editorActivity);
        builder.setTitle(chooseType);
        builder.setCancelable(true);
        builder.setItems(listItems, onClickListener);

        builder.create().show();
    }
}

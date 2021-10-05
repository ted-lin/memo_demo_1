package com.example.memo_demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import jp.wasabeef.richeditor.RichEditor;

public class EditorActivity extends AppCompatActivity {
    public static final String TAG = "EditorActivity";

    private RichEditor mEditor;
    private Button redBtn;
    private Button blackBtn;
    private Button bigBtn;
    private Button smallBtn;
    private Button htmlBtn;
    private Button loadHtmlBtn;
    String html;
    private int mFontSize = 5;
    private View.OnClickListener toRed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setTextColor(Color.RED);

        }
    };
    private View.OnClickListener loadHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setHtml("hello world");
        }
    };
    private View.OnClickListener toBlack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mEditor.setTextColor(Color.BLACK);
        }
    };
    private View.OnClickListener toBig = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFontSize += 1;
            if (mFontSize >= 7)
                mFontSize = 7;
            mEditor.setFontSize(mFontSize);
        }
    };
    private View.OnClickListener toSmall = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFontSize -= 1;
            if (mFontSize <= 0)
                mFontSize = 1;
            mEditor.setFontSize(mFontSize);
        }
    };

    private View.OnClickListener getHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            html = mEditor.getHtml();
            System.out.println(html);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activit_editor);

        mEditor = findViewById(R.id.editor);
        mEditor.setFontSize(22);
        mEditor.setEditorHeight(200);

        redBtn = findViewById(R.id.btnRed);
        blackBtn = findViewById(R.id.btnBlack);
        bigBtn = findViewById(R.id.btnBig);
        smallBtn = findViewById(R.id.btnSmall);
        htmlBtn = findViewById(R.id.btnHtml);
        loadHtmlBtn = findViewById(R.id.btnLoadHtml);
        redBtn.setOnClickListener(toRed);
        blackBtn.setOnClickListener(toBlack);
        smallBtn.setOnClickListener(toSmall);
        bigBtn.setOnClickListener(toBig);
        htmlBtn.setOnClickListener(getHtml);
        loadHtmlBtn.setOnClickListener(loadHtml);

    }
}

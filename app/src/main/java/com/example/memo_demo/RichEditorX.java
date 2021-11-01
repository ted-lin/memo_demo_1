package com.example.memo_demo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebView;
import jp.wasabeef.richeditor.RichEditor;

public class RichEditorX extends RichEditor {
    public RichEditorX(Context context) {
        super(context);
    }

    public RichEditorX(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RichEditorX(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadUrl("file:///android_asset/editor.html");
    }

    @Override
    public String getHtml() {
        String str = super.getHtml();
        return str != null ? str : "";
    }

    private static final String CALLBACK_SCHEME = "re-callback://";
    private static final String STATE_SCHEME = "re-state://";

    @Override
    protected EditorWebViewClient createWebviewClient() {
        return new EditorWebViewClientX();
    }

    protected class EditorWebViewClientX extends EditorWebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            view.getContext().startActivity(intent);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    public void insertText(String html) {
        String s = html;
        for (int i = 0; i < s.length(); i++) {
            exec("javascript:RE.prepareInsert();");
            if (s.charAt(i) != '\n')
                exec("javascript:RE.insertText('" + s.charAt(i) + "');");
            if (s.charAt(i) == '\n')
                exec("javascript:document.execCommand('insertParagraph',false);");
        }
    }
}

package com.example.memo_demo;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MemoMain extends AppCompatActivity {
    public static final String TAG = "MemoMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_main);
        Log.e(TAG, "created");
        MemoInfo memoinfo = getIntent().getParcelableExtra(MainActivity.MEMO_EXTRA);
        memoinfo.dump();

        TextView textView = findViewById(R.id.textView2);
        textView.setMovementMethod(new ScrollingMovementMethod());
        String test[] = {
                "Status message:\n",
                "hahaha\n",
                "is\n",
                "me\n",
                "....\n",
                "test...\n",
                "test...\n",
                "test...\n",
                "test...\n",
                "test...\n",
                "test...\n",
                "test...\n",
                "test...\n",
                "test...\n"
        };
        for (int i = 0; i < test.length; ++i) {
            textView.append(test[i]);
        }

        EditText editText = findViewById(R.id.editText4);
        editText.setMovementMethod(new ScrollingMovementMethod());

        View.OnFocusChangeListener ofcListener = new MemoFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);
    }

    public void saveTo(View v) {
        EditText editText = findViewById(R.id.editText4);
        TextView textView = findViewById(R.id.textView2);


        String message = editText.getText().toString();
        textView.setText(message);
        editText.getText().clear();
        Log.e(TAG, "save " + message);
    }

    private class MemoFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.editText4 && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}

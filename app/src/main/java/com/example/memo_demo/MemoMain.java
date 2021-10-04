package com.example.memo_demo;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

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

        Date currentTime = Calendar.getInstance().getTime();

        String mode = memoinfo.type == MainActivity.MEMO_HOST ? "Host" : "Client";
        TextView title = findViewById(R.id.textView3);

        title.setText("Hi " + memoinfo.user + ", you are running as " + mode + " mode");

        TextView textView = findViewById(R.id.textView2);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.append("Status message:\n");
        textView.setTextSize(16);
        textView.append(currentTime.toString());

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

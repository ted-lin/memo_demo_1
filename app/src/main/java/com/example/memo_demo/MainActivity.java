package com.example.memo_demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.memo_demo.MESSAGE";
    public static final String MEMO_EXTRA = "memo_extra";

    public static final int MEMO_HOST = 1;
    public static final int MEMO_CLIENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user taps the button
     */
    public void sendMessage(View v) {
        Intent intent = new Intent(this, DisplayMesageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message + "XXXXSS");
        startActivity(intent);
        Log.e("TED", "send: " + message);
    }

    /**
     * As Host
     */
    public void asHost(View v) {
        Intent intent = new Intent(this, MemoMain.class);
        intent.putExtra(MEMO_EXTRA, new MemoInfo(MEMO_HOST, "as host"));

        startActivity(intent);
    }

    /**
     * As client
     */
    public void asClient(View v) {

    }
}
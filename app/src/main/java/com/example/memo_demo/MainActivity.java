package com.example.memo_demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final String EXTRA_MESSAGE = "com.example.memo_demo.MESSAGE";
    public static final String MEMO_EXTRA = "memo_extra";

    public static String user = "nobody";

    public static final int MEMO_HOST = 1;
    public static final int MEMO_CLIENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.editText);
        View.OnFocusChangeListener ofcListener = new MainActivity.MainFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);
    }

    private class MainFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.editText && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
    /**
     * Called when the user taps the button
     */
    public void sendMessage(View v) {
        Intent intent = new Intent(this, DisplayMesageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message );
        startActivity(intent);
        Log.e("TED", "send: " + message);
    }

    public boolean checkName() {
        EditText editView = (EditText) findViewById(R.id.editText);
        String string = editView.getText().toString();
        if (0 == string.length())
            return false;
        user = string;
        return true;
    }
    /**
     * As Host
     */
    public void asHost(View v) {
        if (!checkName()) {
            Toast.makeText(getApplicationContext(), "Need a name", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, MemoMain.class);
        intent.putExtra(MEMO_EXTRA, new MemoInfo(MEMO_HOST, "host", user));
        startActivity(intent);
    }

    /**
     * here for jacky to debug
     */
    public void toEditorActivity(View v) {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivity(intent);
    }

   /**
      * As client
      */
    public void asClient(View v) {
        if (!checkName()) {
            Toast.makeText(getApplicationContext(), "Need a name", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, MemoMain.class);
        intent.putExtra(MEMO_EXTRA, new MemoInfo(MEMO_CLIENT, "client", user));
        startActivity(intent);
    }

   /**
     *  check connection
     */
   public void checkConnection(View v) {
        Intent intent = new Intent(this, ConnectionHolder.class);
        startActivity(intent);
   }
}
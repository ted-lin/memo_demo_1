package com.example.memo_demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.MacAddress;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wifi.ServerThread;
import com.example.wifi.SocketListener;
import com.example.wifi.SocketThread;
import com.example.wifi.WifiDirectListener;
import com.example.wifi.WifiP2p;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MemoMain extends AppCompatActivity {
    public static final String TAG = "MemoMain";

    /* basic */
    public static String mMode;
    public static String mUser;
    private List<Button> mButtons;


    public void init(Intent intent) {
        Log.e(TAG, "created");

        MemoInfo memoInfo = intent.getParcelableExtra(MainActivity.MEMO_EXTRA);
        mMode = memoInfo.type == MainActivity.MEMO_HOST ? "Host" : "Client";
        mUser = memoInfo.user;

        TextView title = findViewById(R.id.textView3);
        title.setText("Hi " + mUser + ", you are running as " + mMode + " mMode");

        TextView textView = findViewById(R.id.textView2);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(16);
        textView.setText(getPrefix() + "start\n");

        EditText editText = findViewById(R.id.editText4);
        editText.setMovementMethod(new ScrollingMovementMethod());

        View.OnFocusChangeListener ofcListener = new MemoFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);

        findAllButtons();
    }

    protected void findAllButtons() {
        mButtons = new ArrayList<>();
        mButtons.add((Button) findViewById(R.id.open));
        mButtons.add((Button) findViewById(R.id.save));
        mButtons.add((Button) findViewById(R.id.write_to));
        mButtons.add((Button) findViewById(R.id.clear));
        mButtons.add((Button) findViewById(R.id.start_relay));
        mButtons.add((Button) findViewById(R.id.stop_relay));
    }

    protected void setAllButtonView(int v) {
        for (Button btn: mButtons)
            btn.setVisibility(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    /*  utils */
    enum MEMO_SET_TYPE {
        MEMO_TEXT_SET,
        MEMO_TEXT_APPEND
    };

    public void force_sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        Log.e(TAG, String.format("%s %s", getPrefix(), message));
    }

    public String getTime() {
        SimpleDateFormat now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        return now.format(new Date());
    }

    public String getUser() {
        return "[" + mUser + "] ";
    }

    public String getMode() {
        return "[" + mMode + "] ";
    }

    public String getPrefix() {
        return getTime() + getUser() + getMode();
    }

    public void clearText(View v) {
        updateStatusText(getPrefix() + "reset\n", MEMO_SET_TYPE.MEMO_TEXT_SET);
        updateEditText("", MEMO_SET_TYPE.MEMO_TEXT_SET);
    }

    public void updateStatusText(String msg, MEMO_SET_TYPE type) {
        TextView tv = findViewById(R.id.textView2);
        if (tv == null) {
            log("can't find target to update");
            return;
        }

        switch (type) {
            case MEMO_TEXT_SET:
                tv.setText(msg);
                break;
            case MEMO_TEXT_APPEND:
                tv.append(msg);
                break;
        }
        log("update status: " + type + " " + msg);
    }

    public void updateEditText(String msg, MEMO_SET_TYPE type) {
        EditText et = findViewById(R.id.editText4);
        if (et == null) {
            log("can't find target to update");
            return;
        }

        switch (type) {
            case MEMO_TEXT_SET:
                et.setText(msg);
                break;
            case MEMO_TEXT_APPEND:
                et.append(msg);
                break;
        }
        log("update editor: " + type + " " + msg);
    }

    public String getStatusText() {
        TextView tv = findViewById(R.id.textView2);
            if (tv == null)
                return "";
        return tv.getText().toString();
    }

    public String getEditText() {
        EditText et = findViewById(R.id.editText4);
        if (et == null)
            return "";
        return et.getText().toString();
    }
}

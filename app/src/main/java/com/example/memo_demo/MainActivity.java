package com.example.memo_demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String EXTRA_MESSAGE = "com.example.memo_demo.MESSAGE";
    public static final String MEMO_EXTRA = "memo_extra";

    private static final int REQ_WIFI_DIRECT_PERMISSION = 5566;

    private static String user = "nobody";

    public static final int MEMO_HOST = 1;
    public static final int MEMO_CLIENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* hidding keybox after without focus */
        EditText editText = findViewById(R.id.editText);
        View.OnFocusChangeListener ofcListener = new MainActivity.MainFocusChangeListener();
        editText.setOnFocusChangeListener(ofcListener);

        /* acquire reletive permission for connection */
        performPermissionGrant();

    }

    protected void findAllButton() {

    }
    public void performPermissionGrant() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        Manifest.permission.CHANGE_NETWORK_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQ_WIFI_DIRECT_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WIFI_DIRECT_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    showToast("permission failure: " + permissions[i]);
                    Log.e(TAG, "set to false");
                    return;
                }
            }

            if (grantResults.length == 0)
                return;

            showToast("permission pass");
            //permissionComplete = true;
            Log.e(TAG, "set to true " + grantResults.length);
        }
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
            showToast("Need a name");
            return;
        }
        Intent intent = new Intent(this, MemoHost.class);
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
            showToast("Need a name");
            return;
        }
        Intent intent = new Intent(this, MemoClient.class);
        intent.putExtra(MEMO_EXTRA, new MemoInfo(MEMO_CLIENT, "client", user));
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}
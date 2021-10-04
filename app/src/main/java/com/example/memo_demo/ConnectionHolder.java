package com.example.memo_demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ConnectionHolder extends AppCompatActivity {
    public static final String TAG = "ConnectionHolder";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_holder);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ItemFragment fragment = new ItemFragment();
            transaction.replace(R.id.connection_fragment, fragment);
            transaction.commit();
        }
    }

    public void retriveInfo(View v) {
        TextView textView = (TextView) v.findViewById(R.id.textView5);
        String message = textView.getText().toString();
        Log.e(TAG, "" + v.getId() + " " + message);
    }
}

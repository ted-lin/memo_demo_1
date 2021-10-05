package com.example.memo_demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class ConnectionHolder extends AppCompatActivity {
    public static final String TAG = "ConnectionHolder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_holder);

        //RecyclerView recycleView = findViewById(R.id.peer_list_view);
        //recycleView.setAdapter(MemoMain.mPeerAdapter);
        //recycleView.setLayoutManager(new LinearLayoutManager(this));

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

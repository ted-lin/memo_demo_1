package com.example.mywifidirect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_WIFI_DIRECT_PERMISSION = 5566;
    private static final int HOST_PAGE = 1;
    private static final int CLIENT_PAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button permissionButton = findViewById(R.id.permission);

        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CHANGE_NETWORK_STATE,
                                     Manifest.permission.ACCESS_NETWORK_STATE,
                                     Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                     Manifest.permission.READ_EXTERNAL_STORAGE,
                                     Manifest.permission.ACCESS_WIFI_STATE,
                                     Manifest.permission.CHANGE_WIFI_STATE,
                                     Manifest.permission.ACCESS_FINE_LOCATION}, REQ_WIFI_DIRECT_PERMISSION);
            }
        });

        Button hostButton = findViewById(R.id.host);
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HostActivity.class);
                startActivityForResult(intent, HOST_PAGE);
            }
        });

        Button clientButton = findViewById(R.id.client);
        clientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivityForResult(intent, HOST_PAGE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WIFI_DIRECT_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    showToast("permission failure: " + permissions[i]);
                    return;
                }
            }
            showToast("permission pass");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.eviger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class chooseAuth extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_auth);

        Button auth = findViewById(R.id.auth_chooseAuth);
        Button register = findViewById(R.id.register_chooseAuth);

        auth.setOnClickListener(v -> {
            Intent in = new Intent(chooseAuth.this, authActivity.class);
            startActivity(in);
        });

        register.setOnClickListener(v -> {
            Intent in = new Intent(chooseAuth.this, registerActivity.class);
            startActivity(in);
        });

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

}
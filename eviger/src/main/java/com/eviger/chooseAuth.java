package com.eviger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import static com.eviger.globals.grantPermissionStorage;

public class chooseAuth extends AppCompatActivity {
    Button btnsignin, btnsignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_auth);

        btnsignin = findViewById(R.id.btnsignin);
        btnsignup = findViewById(R.id.btnsignup);

        btnsignin.setOnClickListener(v -> logwin());
        btnsignup.setOnClickListener(v -> regwin());

        grantPermissionStorage(chooseAuth.this);
    }

    private void logwin() {

        Intent in = new Intent(chooseAuth.this, authActivity.class);
        startActivity(in);

    }

    private void regwin() {

        Intent in = new Intent(chooseAuth.this, registerActivity.class);
        startActivity(in);

    }
}
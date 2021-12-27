package com.eviger;

import static com.eviger.z_globals.grantPermissionStorage;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class chooseAuth extends AppCompatActivity {
    Button signIn, signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_auth);

        signIn = findViewById(R.id.signIn);
        signUp = findViewById(R.id.signUp);

        signIn.setOnClickListener(v -> {
            Intent in = new Intent(chooseAuth.this, authActivity.class);
            startActivity(in);
        });

        signUp.setOnClickListener(v -> {
            Intent in = new Intent(chooseAuth.this, registerActivity.class);
            startActivity(in);
        });

        grantPermissionStorage(chooseAuth.this);
    }
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
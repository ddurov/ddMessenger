package com.eviger;

import static com.eviger.z_globals.getToken;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;
import static com.eviger.z_globals.tokenSet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class settingsAccount extends AppCompatActivity {

    ImageButton btnMessages, btnProfile;
    Button changeName, signOut;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_account);

        changeName = findViewById(R.id.toChangeName);
        signOut = findViewById(R.id.signOut);

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        btnProfile = findViewById(R.id.toProfile);
        btnProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsAccount.this, profilePage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.toMessages);
        btnMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsAccount.this, messagesPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        changeName.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsAccount.this, changeName.class);
            in.putExtra("token", getToken());
            in.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(in);
        });

        signOut.setOnClickListener(v -> {
            SharedPreferences.Editor ed = tokenSet.edit();
            ed.putBoolean("isSigned", false);
            ed.apply();
            setOffline();
            Intent in = new Intent(settingsAccount.this, chooseAuth.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);
        });

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
            setOffline();
            sendingOnline = false;
            activatedMethodUserLeaveHint = true;
        }
    }
    protected void onResume() {
        super.onResume();
        if (activatedMethodUserLeaveHint) {
            setOnline();
            sendingOnline = true;
            inAnotherActivity = false;
            activatedMethodUserLeaveHint = false;
        }
    }

}
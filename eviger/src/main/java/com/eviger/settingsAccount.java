package com.eviger;

import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;

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

        btnProfile = findViewById(R.id.buttonGoToProfile);
        btnProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsAccount.this, profilePage.class);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.buttonGoToMessages);
        btnMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsAccount.this, messagesPage.class);
            startActivity(in);
        });

        changeName.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsAccount.this, changeName.class);
            in.putExtra("token", getToken());
            startActivity(in);
        });

        signOut.setOnClickListener(v -> {


            SharedPreferences.Editor ed = globals.tokenSet.edit();
            ed.putBoolean("isSigned", false);
            ed.apply();
            setOffline();
            Intent intent = new Intent(settingsAccount.this, chooseAuth.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        });

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
            setOffline();
            profilePage.onlineSending = false;
            activatedMethodUserLeaveHint = true;
        }
    }
    protected void onResume() {
        super.onResume();
        if (activatedMethodUserLeaveHint) {
            setOnline();
            profilePage.onlineSending = true;
            inAnotherActivity = false;
            activatedMethodUserLeaveHint = false;
        }
    }

}
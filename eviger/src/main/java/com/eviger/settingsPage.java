package com.eviger;

import static com.eviger.z_globals.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class settingsPage extends AppCompatActivity {

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_account);

        ImageButton toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsPage.this, profilePage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        ImageButton toMessages = findViewById(R.id.toMessages);
        toMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsPage.this, messagesPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        Button changeName = findViewById(R.id.toChangeName);
        changeName.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(settingsPage.this, changeName.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(in);
        });

        Button signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(v -> {
            SharedPreferences.Editor tokenEditor = tokenSet.edit();
            tokenEditor.putBoolean("isSigned", false);
            tokenEditor.apply();
            Intent in = new Intent(settingsPage.this, chooseAuth.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);
        });

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
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
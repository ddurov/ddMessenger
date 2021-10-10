package com.eviger;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;

public class profilePage extends AppCompatActivity {

    ImageButton toMessages, toSettings, toSearch;
    Button sendMessageToYourSelf;
    TextView nameProfile;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    public static boolean onlineSending = true;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        sendMessageToYourSelf = findViewById(R.id.sendMessagesYourself);
        nameProfile = findViewById(R.id.welcomeUser);

        toSearch = findViewById(R.id.buttonToSearch);
        toSearch.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, searchUsers.class);
            startActivity(in);
        });

        toMessages = findViewById(R.id.buttonGoToMessages);
        toMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, messagesPage.class);
            startActivity(in);
        });

        toSettings = findViewById(R.id.buttonGoToSettings);
        toSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, settingsAccount.class);
            startActivity(in);
        });

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        try {

            JSONObject json = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));
            sendMessageToYourSelf.setOnClickListener(v -> {

                try {
                    inAnotherActivity = true;
                    Intent intent = new Intent(profilePage.this, messagesChat.class);
                    intent.putExtra("eid", json.getJSONObject("response").getInt("eid"));
                    startActivity(intent);
                } catch (Throwable ex) {
                    runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                }

            });

            nameProfile.setText("Текущее имя профиля: " + json.getJSONObject("response").getString("username"));

            Thread checkInternet = new Thread(() -> {
                while (true) {
                    try {
                        onlineSending = hasConnection(getApplicationContext());
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        onlineSending = false;
                        runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                    }
                }
            });
            checkInternet.start();

            Thread sendingOnlineThread = new Thread(() -> {
                while (profilePage.onlineSending) {
                    try {
                        setOnline();
                        Thread.sleep(300000);
                    } catch (Exception ex) {
                        profilePage.onlineSending = false;
                        runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                    }
                }
            });
            sendingOnlineThread.start();

        } catch (Throwable ex) {
            runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
        }

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
            onlineSending = false;
            activatedMethodUserLeaveHint = true;
            setOffline();
        }
    }
    protected void onResume() {
        super.onResume();
        if (activatedMethodUserLeaveHint) {
            onlineSending = true;
            inAnotherActivity = false;
            activatedMethodUserLeaveHint = false;
            setOnline();
        }
    }
    public void onBackPressed() {
        super.onBackPressed();
        onlineSending = false;
        setOffline();
    }

}

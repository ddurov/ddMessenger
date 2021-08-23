package com.eviger;

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

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class profilePage extends AppCompatActivity {

    ImageButton btnMessages, btnSettings, btnToSearch;
    Button sendMessageToYourSelf;
    TextView nameProfile, onlineProf;
    boolean hasNextActivity;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        sendMessageToYourSelf = findViewById(R.id.sendMessagesYourSelf);
        hasNextActivity = false;

        btnToSearch = findViewById(R.id.buttonToSearch);
        btnToSearch.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePage.this, searchUsers.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.buttonGoToMessages);
        btnMessages.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePage.this, messagesPage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.buttonGoToSettings);
        btnSettings.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePage.this, settingsAccount.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        nameProfile = findViewById(R.id.WelcomeUserInProfile);
        onlineProf = findViewById(R.id.onlineUserProfile);

        if (hasConnection(this)) {

            try {

                JSONObject json = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));
                sendMessageToYourSelf.setOnClickListener(v -> {

                    try {
                        hasNextActivity = true;
                        Intent intent = new Intent(profilePage.this, messagesChat.class);
                        intent.putExtra("eid", json.getJSONObject("response").getInt("eid"));
                        startActivity(intent);
                    } catch (Throwable ex) {
                        runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                    }

                });

                nameProfile.setText("Текущее имя профиля: " + json.getJSONObject("response").getString("username"));

            } catch (Throwable ex) {
                runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
            }

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }
    public void onBackPressed() {

        super.onBackPressed();
        if (!getIntent().getBooleanExtra("hasLastActivity", false)) {
            setOffline();
            finish();
        }

    }
    public void onUserLeaveHint() {

        super.onUserLeaveHint();
        if (!hasNextActivity) {

            if (hasConnection(this)) {

                if (getToken() != null) {

                    setOffline();

                }

            }

        }

    }
    protected void onResume() {

        super.onResume();
        if (hasConnection(this)) {

            if (getToken() != null) {

                setOnline();

            }

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }

}

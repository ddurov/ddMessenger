package com.eviger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getAccount;
import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class profilePageAny extends AppCompatActivity {

    ImageButton btnBackToSearch, btnMessages, btnProfile, btnSettings;
    Button btnToStartDialog;
    TextView nameProfile, onlineProfile;
    boolean hasNextActivity;
    int id;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page_any);

        hasNextActivity = false;
        id = getIntent().getIntExtra("id", -1);

        btnBackToSearch = findViewById(R.id.backToSearchButton);
        btnBackToSearch.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePageAny.this, searchUsers.class);
            startActivity(in);
            finish();
        });

        btnToStartDialog = findViewById(R.id.startDialog);
        btnToStartDialog.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent intent = new Intent(profilePageAny.this, messagesChat.class);
            intent.putExtra("eid", id);
            startActivity(intent);
        });

        btnProfile = findViewById(R.id.buttonGoToProfile);
        btnProfile.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePageAny.this, profilePage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.buttonGoToMessages);
        btnMessages.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePageAny.this, messagesPage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.buttonGoToSettings);
        btnSettings.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(profilePageAny.this, settingsAccount.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        nameProfile = findViewById(R.id.WelcomeUserInProfile);
        onlineProfile = findViewById(R.id.onlineUserProfile);

        if (hasConnection(this)) {

            try {

                JSONObject json = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{"id", String.valueOf(id)}}));

                nameProfile.setText("Это профиль \""+json.getJSONObject("response").getString("username")+"\"");
                Locale.setDefault(Locale.getDefault());

                if (getAccount(json.getJSONObject("response").getInt("eid"))[0].equals(true)) {

                    onlineProfile.setText("В сети");

                } else {

                    int lastSeen = (int) getAccount(json.getJSONObject("response").getInt("eid"))[2];

                    String date = new SimpleDateFormat("d MMM yyyy 'года' HH:mm", Locale.getDefault()).format(new Date(lastSeen * 1000L));
                    onlineProfile.setText("Не заходил с " + date);

                }

            } catch (Exception ex) {
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
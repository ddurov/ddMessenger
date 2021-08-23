package com.eviger;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class settingsAccount extends AppCompatActivity {

    ImageButton btnMessages, btnProfile;
    Button changeName, signOut;
    TextView nickname;
    boolean hasNextActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_account);

        changeName = findViewById(R.id.settings_buttonChangeName);
        signOut = findViewById(R.id.settings_signOut);
        nickname = findViewById(R.id.settings_changeName);
        hasNextActivity = false;

        btnProfile = findViewById(R.id.buttonGoToProfile);
        btnProfile.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(settingsAccount.this, profilePage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.buttonGoToMessages);
        btnMessages.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(settingsAccount.this, messagesPage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        changeName.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(settingsAccount.this, changeName.class);
            in.putExtra("token", getToken());
            startActivity(in);
        });

        if (hasConnection(this)) {

            signOut.setOnClickListener(v -> {

                SharedPreferences.Editor ed = globals.tokenSet.edit();
                ed.putBoolean("isSigned", false);
                ed.apply();
                setOffline();
                Intent intent = new Intent(settingsAccount.this, chooseAuth.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            });

            try {
                nickname.setText(new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}})).getJSONObject("response").getString("username"));
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
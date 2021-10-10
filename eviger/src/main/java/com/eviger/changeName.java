package com.eviger;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getHashCodeEmail;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class changeName extends AppCompatActivity {

    Button confirmChangeName;
    EditText newName;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_name);

        newName = findViewById(R.id.newName);
        confirmChangeName = findViewById(R.id.confirmChangeName);

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        confirmChangeName.setOnClickListener(v -> {

            if (newName.getText().toString().length() <= 6 && newName.getText().toString().length() >= 128) {
                Toast.makeText(getApplicationContext(), "Имя должен быть больше 6 и меньше 128 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (Pattern.matches("^e?id+[\\d]+", newName.getText().toString())) {
                Toast.makeText(this, "Имя не должно содержать в себе id или eid", Toast.LENGTH_LONG).show();
                return;
            }

            try {

                inAnotherActivity = true;
                Intent in = new Intent(changeName.this, emailConfirm.class);
                in.putExtra("type", "changeName");
                in.putExtra("newName", newName.getText().toString().trim());
                JSONObject getEmailFromMyAccount = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));
                in.putExtra("email", getEmailFromMyAccount.getJSONObject("response").getString("email"));
                in.putExtra("hash", getHashCodeEmail(getEmailFromMyAccount.getJSONObject("response").getString("email")));
                startActivity(in);

            } catch (Exception ex) {
                runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
            }

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
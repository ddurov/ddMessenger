package com.eviger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;

import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class recoveryAccountConfirmed extends AppCompatActivity {

    public SharedPreferences tokenSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recovery_account_confirmed);

        Button recovery = findViewById(R.id.recovery);

        EditText newPassword = findViewById(R.id.newPasswordRecovery);

        TextView descriptionRecovery = findViewById(R.id.descriptionRecovery);
        descriptionRecovery.setText("В поле ниже, введите новый пароль для аккаунта с почтой: " + getIntent().getStringExtra("email"));

        recovery.setOnClickListener(v -> {

            if (newPassword.getText().toString().length() < 8) {
                Toast.makeText(this, "Пароль меньше 8 символов", Toast.LENGTH_LONG).show();
                return;
            }

            try {

                JSONObject JSON = new JSONObject();
                JSON.put("email", getIntent().getStringExtra("email"));
                JSON.put("confirmCode", getIntent().getStringExtra("code"));
                JSON.put("newPassword", newPassword.getText().toString());
                JSON.put("hash", getIntent().getStringExtra("hash"));

                JSONObject recoveryJsonObject = new JSONObject(executeApiMethodPost("user", "restorePassword", JSON));

                if (recoveryJsonObject.getJSONObject("response").getString("status").equals("ok")) {

                    tokenSet = getSharedPreferences("tokens", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = tokenSet.edit();
                    ed.remove("token");
                    ed.putString("token", recoveryJsonObject.getJSONObject("response").getString("newToken"));
                    ed.apply();
                    Intent in = new Intent(recoveryAccountConfirmed.this, profilePage.class);
                    startActivity(in);
                    finish();

                }

            } catch (Throwable ex) {
                runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
            }

        });

    }
}
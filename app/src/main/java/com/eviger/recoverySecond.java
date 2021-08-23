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

public class recoverySecond extends AppCompatActivity {

    public SharedPreferences tokenSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recovery_second);

        Button goToRecovery_button = findViewById(R.id.goToRecovery_button);

        EditText newPassword = findViewById(R.id.newPasswordRecovery);

        TextView descriptionRecovery = findViewById(R.id.descriptionRecovery);
        descriptionRecovery.setText("В поле ниже, введите новый пароль для аккаунта с почтой: "+getIntent().getStringExtra("email"));

        goToRecovery_button.setOnClickListener(v -> {

            try {

                if (!newPassword.getText().toString().isEmpty() && newPassword.getText().toString().length() > 8) {

                    JSONObject JSON = new JSONObject();
                    JSON.put("confirmCode", getIntent().getStringExtra("code"));
                    JSON.put("hash", getIntent().getStringExtra("hash"));
                    JSON.put("newPassword", newPassword.getText().toString());
                    JSON.put("email", getIntent().getStringExtra("email"));

                    JSONObject recoveryJsonObject = new JSONObject(executeApiMethodPost("user", "restorePassword", JSON));

                    if (recoveryJsonObject.getJSONObject("response").getString("status").equals("ok")) {

                        tokenSet = getSharedPreferences("tokens", Context.MODE_PRIVATE);
                        SharedPreferences.Editor ed = tokenSet.edit();
                        ed.putString("token", recoveryJsonObject.getJSONObject("response").getString("token"));
                        ed.apply();
                        Intent in = new Intent(recoverySecond.this, profilePage.class);
                        startActivity(in);
                        finish();

                    }

                } else if (!newPassword.getText().toString().isEmpty() && newPassword.getText().toString().length() < 8) {

                    Toast.makeText(this, "Пароль меньше 8 символов!", Toast.LENGTH_LONG).show();

                } else if (newPassword.getText().toString().isEmpty()) {

                    Toast.makeText(this, "Укажите пароль!", Toast.LENGTH_LONG).show();

                }

            } catch (Throwable ex) {
                if (Objects.equals(ex.getMessage(), "Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference")) {
                    runOnUiThread(() -> Toast.makeText(this, "Произошла неизвестная ошибка на наших серверах. Невозможно подключиться.", Toast.LENGTH_LONG).show());
                } else {
                    Log.e("l/e", ex.getMessage());
                }
            }

        });

    }
}
package com.eviger;

import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;
import static com.eviger.globals.submitHashAndCodeEmail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class emailConfirm extends AppCompatActivity {

    String email, type, hash;
    TextView descriptionConfirmEmail;
    EditText codeConfirm;
    Button checkCode;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_confirm);

        email = getIntent().getStringExtra("email");
        type = getIntent().getStringExtra("type");
        hash = getIntent().getStringExtra("hashCode");
        descriptionConfirmEmail = findViewById(R.id.descriptionConfirmEmail);
        codeConfirm = findViewById(R.id.codeFromEmail);
        checkCode = findViewById(R.id.сheckCode);

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        descriptionConfirmEmail.setText("Введите присланный код с почты (" + email + ") в поле ниже");

        checkCode.setOnClickListener(v -> {

            if (codeConfirm.getText().toString().length() != 16 || !Pattern.matches("/[A-Z0-9]/g", codeConfirm.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Введенный код не соответсвует формату", Toast.LENGTH_LONG).show();
                return;
            }

            try {

                switch (type) {

                    case "changeName": {

                        JSONObject JSON = new JSONObject();
                        JSON.put("newName", getIntent().getStringExtra("newName"));
                        JSON.put("email", email);
                        JSON.put("code", codeConfirm.getText().toString());
                        JSON.put("hash", hash);

                        JSONObject jsonChangeName = new JSONObject(executeApiMethodPost("user", "changeName", JSON));
                        if (!jsonChangeName.getJSONObject("response").getString("status").equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Произошла ошибка при выполнении запроса", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(new Intent(emailConfirm.this, profilePage.class));
                        Toast.makeText(getApplicationContext(), "Имя успешно изменено на: " + getIntent().getStringExtra("newName"), Toast.LENGTH_SHORT).show();
                        finish();

                        break;

                    }
                    case "registerAccount": {

                        JSONObject JSON = new JSONObject();
                        JSON.put("login", getIntent().getStringExtra("login"));
                        JSON.put("password", getIntent().getStringExtra("password"));
                        JSON.put("email", getIntent().getStringExtra("email"));
                        JSON.put("registrationEmailCode", codeConfirm.getText().toString());
                        JSON.put("userName", getIntent().getStringExtra("name"));
                        JSON.put("hashCode", hash);

                        JSONObject jsonAccount = new JSONObject(executeApiMethodPost("user", "registerAccount", JSON));
                        SharedPreferences.Editor ed = globals.tokenSet.edit();
                        ed.putString("token", jsonAccount.getJSONObject("response").getString("token"));
                        ed.putBoolean("isSigned", true);
                        ed.apply();
                        Intent inAccount = new Intent(emailConfirm.this, profilePage.class);
                        inAccount.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(inAccount);
                        finish();

                        break;

                    }
                    case "recoveryAccount": {

                        if (!submitHashAndCodeEmail(email, codeConfirm.getText().toString(), hash)) {
                            Toast.makeText(getApplicationContext(), "Введенный код указан неверно!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent inRecovery = new Intent(emailConfirm.this, recoveryAccountConfirmed.class);
                        inRecovery.putExtra("email", email);
                        inRecovery.putExtra("code", codeConfirm.getText().toString());
                        inRecovery.putExtra("hash", hash);
                        startActivity(inRecovery);
                        finish();

                        break;

                    }

                }

            } catch (Throwable ex) {
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
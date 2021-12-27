package com.eviger;

import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class authActivity extends AppCompatActivity {

    Button toAuth, toRecovery;
    EditText login, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        login = findViewById(R.id.authLogin);
        password = findViewById(R.id.authPassword);
        toAuth = findViewById(R.id.authSignIn);
        toRecovery = findViewById(R.id.recoveryAccount);

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        toAuth.setOnClickListener(v -> {

            if (!hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                return;
            }

            if (login.getText().toString().length() <= 6 || login.getText().toString().length() >= 20) {
                Toast.makeText(getApplicationContext(), "Логин должен быть больше 6 и меньше 20 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Pattern.compile("[a-zA-Z0-9_]").matcher(login.getText().toString()).find()) {
                Toast.makeText(getApplicationContext(), "Логин должен содержать только английские буквы (регистр учитывается), цифры и нижнее подчёркивание (_)", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.getText().toString().length() <= 8) {
                Toast.makeText(getApplicationContext(), "Пароль должен быть больше 8 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Pattern.compile("[a-zA-Z0-9_]").matcher(password.getText().toString()).find()) {
                Toast.makeText(getApplicationContext(), "Пароль должен содержать только английские буквы (регистр учитывается), цифры и нижнее подчёркивание (_)", Toast.LENGTH_LONG).show();
                return;
            }

            try {

                JSONObject jsonAuthToken = new JSONObject(executeApiMethodGet("user", "auth", new String[][]{
                        {"login", login.getText().toString().trim()},
                        {"password", password.getText().toString().trim()}
                }));

                if (!jsonAuthToken.getString("status").equals("error")) {

                    SharedPreferences.Editor tokensEditor = z_globals.tokenSet.edit();
                    tokensEditor.putString("token", jsonAuthToken.getJSONObject("response").getString("token"));
                    tokensEditor.putBoolean("isSigned", true);
                    tokensEditor.apply();
                    z_globals.myProfile = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}})).getJSONObject("response");
                    Intent in = new Intent(authActivity.this, profilePage.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(in);
                    finish();

                } else {

                    switch (jsonAuthToken.getJSONObject("response").getString("message")) {

                        case "account banned":
                            Intent in = new Intent(authActivity.this, restoreUserPage.class);
                            in.putExtra("reason", jsonAuthToken.getJSONObject("response").getJSONObject("details").getString("reason"));
                            in.putExtra("canRestore", jsonAuthToken.getJSONObject("response").getJSONObject("details").getBoolean("canRestoreNow"));
                            startActivity(in);
                            finish();
                            break;

                        case "user not found":
                            Toast.makeText(this, "Пользователь с таким логином не найден", Toast.LENGTH_LONG).show();
                            break;

                        case "invalid login or password":
                            Toast.makeText(this, "Логин или пароль указаны неверно", Toast.LENGTH_LONG).show();
                            break;

                        default:
                            Toast.makeText(this, "Ошибка API: " + jsonAuthToken.getJSONObject("response").getString("message"), Toast.LENGTH_LONG).show();
                            break;

                    }

                }

            } catch (Throwable ex) {
                runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex)));
            }

        });

        toRecovery.setOnClickListener(v -> {
            Intent in = new Intent(authActivity.this, resetPassword.class);
            startActivity(in);
            finish();
        });

    }

}
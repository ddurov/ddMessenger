package com.eviger;

import static com.eviger.z_globals.executeApiMethodPost;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class registerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        EditText login = findViewById(R.id.loginInput);
        EditText password = findViewById(R.id.passwordInput);
        EditText email = findViewById(R.id.emailInput);
        EditText nickname = findViewById(R.id.nickInput);

        Button toEmailCheck = findViewById(R.id.toEmailCheck);

        toEmailCheck.setOnClickListener(v -> {

            if (!hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                return;
            }

            if ((login.getText().toString().length() >= 20 || login.getText().toString().length() <= 6)) {
                Toast.makeText(this, "Логин должен быть больше 6 и меньше 20 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Pattern.compile("[a-zA-Z0-9_]").matcher(login.getText().toString()).find()) {
                Toast.makeText(this, "Логин должен содержать только английские буквы, цифры и нижнее подчёркивание (_)", Toast.LENGTH_LONG).show();
                return;
            }

            if ((password.getText().toString().length() <= 8 || password.getText().toString().length() >= 64)) {
                Toast.makeText(this, "Пароль должен быть больше 8 и меньше 64 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Pattern.compile("[a-zA-Z0-9_]").matcher(password.getText().toString()).find()) {
                Toast.makeText(this, "Пароль должен содержать только английские буквы, цифры и нижнее подчёркивание (_)", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$").matcher(email.getText().toString()).find()) {
                Toast.makeText(this, "Почта должна соответствовать стандартам (например: example@example.com)", Toast.LENGTH_LONG).show();
                return;
            }

            if ((nickname.getText().toString().length() <= 6 || nickname.getText().toString().length() >= 128)) {
                Toast.makeText(this, "Имя должен быть больше 6 и меньше 128 символов", Toast.LENGTH_LONG).show();
                return;
            }

            if (Pattern.compile("^e?id+[\\d]+").matcher(nickname.getText().toString()).find()) {
                Toast.makeText(this, "Имя не должно содержать в себе id или eid", Toast.LENGTH_LONG).show();
                return;
            }

            String name = nickname.getText().toString().isEmpty() ? null : nickname.getText().toString().trim();

            Intent in = new Intent(registerActivity.this, emailConfirm.class);
            in.putExtra("type", "registerAccount");
            in.putExtra("login", login.getText().toString().toLowerCase().trim());
            in.putExtra("password", password.getText().toString().trim());
            in.putExtra("email", email.getText().toString());
            in.putExtra("name", name);

            try {

                JSONObject json = new JSONObject();
                json.put("email", email.getText().toString());

                JSONObject getStatusEmailSend = new JSONObject(executeApiMethodPost("email", "createCode", json));

                if (!getStatusEmailSend.getJSONObject("response").has("error")) {

                    in.putExtra("hashCode", getStatusEmailSend.getJSONObject("response").getString("hash"));
                    startActivity(in);
                    finish();

                } else {

                    switch (getStatusEmailSend.getJSONObject("response").getString("error")) {

                        case "email is busy":
                            Toast.makeText(this, "Почта уже зарегистрирована", Toast.LENGTH_LONG).show();
                            break;
                        case "the user is already registered":
                            Toast.makeText(this, "Пользователь с таким логином уже зарегистрирован", Toast.LENGTH_LONG).show();
                            break;
                        case "username is busy":
                            Toast.makeText(this, "Пользователь с таким именем уже зарегистрирован", Toast.LENGTH_LONG).show();
                            break;
                        case "cooldown":
                            Toast.makeText(this, "Почта временно в блокировке. Попробуйте повторить попытку через 5 минут", Toast.LENGTH_LONG).show();
                            break;
                        case "email must not contain domains of any level eviger.ru":
                            Toast.makeText(this, "Почта не должна содержать в себе (под)домены eviger.ru", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(this, getStatusEmailSend.toString(), Toast.LENGTH_LONG).show();
                            break;

                    }

                }

            } catch (Throwable ex) {
                runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
            }

        });

    }
}
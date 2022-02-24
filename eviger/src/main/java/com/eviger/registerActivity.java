package com.eviger;

import static com.eviger.z_globals.executeApiMethodPost;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.requestEmailCode;
import static com.eviger.z_globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class registerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        EditText login = findViewById(R.id.login_registerActivity);
        EditText password = findViewById(R.id.password_registerActivity);
        EditText email = findViewById(R.id.email_registerActivity);
        EditText nickname = findViewById(R.id.name_registerActivity);

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

            try {

                JSONObject parametersPostRequest_userRegisterAccount = new JSONObject();
                parametersPostRequest_userRegisterAccount.put("login", login.getText().toString().toLowerCase().trim());
                parametersPostRequest_userRegisterAccount.put("password", password.getText().toString().trim());
                parametersPostRequest_userRegisterAccount.put("email", email.getText().toString());
                parametersPostRequest_userRegisterAccount.put("userName", name);

                JSONObject postResponse_userRegister = new JSONObject(executeApiMethodPost("user", "registerAccount", parametersPostRequest_userRegisterAccount));

                if (postResponse_userRegister.getJSONObject("response").getString("message").equals("confirm email")) {

                    JSONObject postResponse_requestEmailCode = new JSONObject(requestEmailCode(email.getText().toString()));

                    if (postResponse_requestEmailCode.getString("status").equals("ok") || postResponse_requestEmailCode.getJSONObject("response").getString("message").equals("code has already been requested")) {

                        Intent in = new Intent(registerActivity.this, emailConfirm.class)
                                .putExtra("type", "registerAccount")
                                .putExtra("login", login.getText().toString().toLowerCase().trim())
                                .putExtra("password", password.getText().toString().trim())
                                .putExtra("name", name)
                                .putExtra("email", email.getText().toString())
                                .putExtra("hashCode", postResponse_requestEmailCode.getJSONObject("response").getString("hash"));

                        startActivity(in);
                        finish();

                    } else {

                        Toast.makeText(getApplicationContext(), postResponse_requestEmailCode.getJSONObject("response").getString("message"), Toast.LENGTH_LONG).show();

                    }

                } else {

                    switch (postResponse_userRegister.getJSONObject("response").getString("message")) {

                        case "user with provided login already registered":
                            Toast.makeText(getApplicationContext(), "Пользователь с таким логином уже зарегистрирован", Toast.LENGTH_LONG).show();
                            break;
                        case "user with provided email already registered":
                            Toast.makeText(getApplicationContext(), "Пользователь с такой почтой уже зарегистрирован", Toast.LENGTH_LONG).show();
                            break;
                        case "user with provided username already registered":
                            Toast.makeText(getApplicationContext(), "Пользователь с таким именем уже зарегистрирован", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), postResponse_userRegister.getJSONObject("response").getString("message"), Toast.LENGTH_LONG).show();
                            break;

                    }

                }

            } catch (Exception ex) {
                runOnUiThread(() -> writeErrorInLog(ex));
            }

        });

    }
}
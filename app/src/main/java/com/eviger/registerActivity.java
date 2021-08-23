package com.eviger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class registerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        if (hasConnection(getApplicationContext())) {

            EditText login = findViewById(R.id.loginInput);
            EditText password = findViewById(R.id.passwordInput);
            EditText email = findViewById(R.id.emailInput);
            EditText nickname = findViewById(R.id.nickInput);

            Button btntocheck = findViewById(R.id.gotoemailcheck);

            btntocheck.setOnClickListener(v -> {

                if ((login.getText().toString().length() < 20 && login.getText().toString().length() > 6) && Pattern.compile("[a-zA-Z0-9_]").matcher(login.getText().toString()).find()) {

                    if ((password.getText().toString().length() > 8 && password.getText().toString().length() < 60) && Pattern.compile("[a-zA-Z0-9_]").matcher(password.getText().toString()).find()) {

                        if (!email.getText().toString().isEmpty() && Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$").matcher(email.getText().toString()).find()) {

                            if (nickname.getText().toString().length() < 128) {

                                String name = nickname.getText().toString().isEmpty() ? null : nickname.getText().toString();

                                Intent in = new Intent(registerActivity.this, emailConfirm.class);
                                in.putExtra("type", "registerAccount");
                                in.putExtra("login", login.getText().toString().toLowerCase().trim());
                                in.putExtra("password", password.getText().toString().trim());
                                in.putExtra("email", email.getText().toString());
                                in.putExtra("name", name);

                                try {

                                    JSONObject json = new JSONObject();

                                    json.put("login", login.getText().toString());
                                    json.put("password", password.getText().toString());
                                    json.put("email", email.getText().toString());

                                    JSONObject getStatusEmailSend = new JSONObject(executeApiMethodPost("user", "registerAccount", json));

                                    if (!getStatusEmailSend.getJSONObject("response").has("error")) {

                                        in.putExtra("hashCode", getStatusEmailSend.getJSONObject("response").getString("hash"));
                                        startActivity(in);
                                        finish();

                                    } else {

                                        switch (getStatusEmailSend.getJSONObject("response").getString("error")) {

                                            case "email is busy":
                                                Toast.makeText(this, "Почта уже зарегистрирована.", Toast.LENGTH_LONG).show();
                                                break;
                                            case "the user is already registered":
                                                Toast.makeText(this, "Пользователь с таким логином уже зарегистрирован.", Toast.LENGTH_LONG).show();
                                                break;
                                            case "username is busy":
                                                Toast.makeText(this, "Пользователь с таким именем уже зарегистрирован.", Toast.LENGTH_LONG).show();
                                                break;
                                            case "cooldown":
                                                Toast.makeText(this, "Почта временно в блокировке. Попробуйте повторить попытку через 5 минут.", Toast.LENGTH_LONG).show();
                                                break;
                                            default:
                                                Toast.makeText(this, getStatusEmailSend.toString(), Toast.LENGTH_LONG).show();
                                                break;

                                        }

                                    }

                                } catch (Throwable ex) {
                                    runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                                }

                            } else {

                                Toast.makeText(this, "Имя больше 128 символов.", Toast.LENGTH_LONG).show();

                            }

                        } else if (!email.getText().toString().isEmpty() && !Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$").matcher(email.getText().toString()).find()) {

                            Toast.makeText(this, "Почта должна соответствовать стандартам (например: example@example.com).", Toast.LENGTH_LONG).show();

                        } else if (email.getText().toString().isEmpty()) {

                            Toast.makeText(this, "Укажите почту!", Toast.LENGTH_LONG).show();

                        }


                    } else if ((password.getText().toString().length() <= 8 || password.getText().toString().length() >= 60) || !Pattern.compile("[a-zA-Z0-9_]").matcher(password.getText().toString()).find()) {

                        Toast.makeText(this, "Пароль должен быть больше 8, меньше 60 символов, содержать только английские буквы (регистр учитывается), цифры и нижнее подчёркивание (_).", Toast.LENGTH_LONG).show();

                    }

                } else if ((login.getText().toString().length() >= 20 || login.getText().toString().length() <= 6) || !Pattern.compile("[a-zA-Z0-9_]").matcher(login.getText().toString()).find()) {

                    Toast.makeText(this, "Логин должен быть больше 6 и меньше 20 символов, содержать только английские буквы (регистр учитывается), цифры и нижнее подчёркивание (_).", Toast.LENGTH_LONG).show();

                }

            });

        } else {

            Toast.makeText(this, "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }
}
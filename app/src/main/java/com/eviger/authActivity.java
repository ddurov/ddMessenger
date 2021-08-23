package com.eviger;

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

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class authActivity extends AppCompatActivity {

    Button btntoAuth;
    Button btntoRecovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        if (hasConnection(getApplicationContext())) {

            EditText login = findViewById(R.id.authLogin);
            EditText password = findViewById(R.id.authPassword);

            btntoRecovery = findViewById(R.id.recoveryAccount);

            btntoRecovery.setOnClickListener(v -> {
                Intent in = new Intent(authActivity.this, recoveryAccount.class);
                startActivity(in);
                finish();
            });

            btntoAuth = findViewById(R.id.authSignIn);

            btntoAuth.setOnClickListener(v -> {

                if ((login.getText().toString().length() > 6 && login.getText().toString().length() < 20) && Pattern.compile("[a-zA-Z0-9_]").matcher(login.getText().toString()).find()) {

                    if ((password.getText().toString().length() > 8 && password.getText().toString().length() < 60) && Pattern.compile("[a-zA-Z0-9_]").matcher(password.getText().toString()).find()) {

                        try {

                            JSONObject jsonAuthToken = new JSONObject(executeApiMethodGet("user", "auth", new String[][]{
                                    {"login", login.getText().toString().toLowerCase().trim()},
                                    {"password", password.getText().toString().trim()}
                            }));

                            if (!jsonAuthToken.getJSONObject("response").has("error")) {

                                JSONObject jsonUserStatus = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{"token", jsonAuthToken.getJSONObject("response").getString("token")}}));

                                if (!jsonUserStatus.getJSONObject("response").has("error")) {

                                    SharedPreferences.Editor tokensEditor = globals.tokenSet.edit();
                                    tokensEditor.putString("token", jsonAuthToken.getJSONObject("response").getString("token"));
                                    tokensEditor.putBoolean("isSigned", true);
                                    tokensEditor.apply();
                                    Intent in = new Intent(authActivity.this, profilePage.class);
                                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(in);
                                    finish();

                                } else {

                                    if (jsonUserStatus.getJSONObject("response").getString("error").equals("token inactive due hacking") || jsonUserStatus.getJSONObject("response").getString("error").equals("token inactive due delete profile at own request")) {

                                        Intent in = new Intent(authActivity.this, restoreUserPage.class);
                                        in.putExtra("reason", jsonUserStatus.getJSONObject("response").getString("error"));
                                        in.putExtra("canRestore", jsonUserStatus.getJSONObject("response").getBoolean("canRestore"));
                                        startActivity(in);
                                        finish();

                                    }

                                }

                            } else {

                                switch (jsonAuthToken.getJSONObject("response").getString("error")) {

                                    case "the user not found":
                                        Toast.makeText(this, "Пользователь с таким логином не найден", Toast.LENGTH_LONG).show();
                                    break;

                                    case "invalid login or password":
                                        Toast.makeText(this, "Логин или пароль указаны неверно", Toast.LENGTH_LONG).show();
                                    break;

                                    default:
                                        Toast.makeText(this, "Ошибка API: "+jsonAuthToken.getJSONObject("response").getString("error"), Toast.LENGTH_LONG).show();
                                    break;

                                }

                            }

                        } catch (Throwable ex) {
                            runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                        }

                    } else if ((password.getText().toString().length() >= 60 || password.getText().toString().length() <= 8) || !Pattern.compile("[a-zA-Z0-9_]").matcher(password.getText().toString()).find()) {

                        Toast.makeText(getApplicationContext(), "Пароль должен быть больше 8 и меньше 60 символов, содержать только английские буквы (регистр учитывается), цифры и нижнее подчёркивание (_).", Toast.LENGTH_LONG).show();

                    }

                } else if ((login.getText().toString().length() >= 20 || login.getText().toString().length() <= 6) || !Pattern.compile("[a-zA-Z0-9_]").matcher(login.getText().toString()).find()) {

                    Toast.makeText(getApplicationContext(), "Логин должен быть больше 6 и меньше 20 символов, содержать только английские буквы (регистр учитывается), цифры и нижнее подчёркивание (_).", Toast.LENGTH_LONG).show();

                }

            });

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }

}
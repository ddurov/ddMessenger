package com.eviger;

import static com.eviger.z_globals.dialogs;
import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.getProfileById;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.myProfile;
import static com.eviger.z_globals.writeErrorInLog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class authActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth);

        EditText login = findViewById(R.id.login_authActivity);
        EditText password = findViewById(R.id.password_authActivity);
        Button auth = findViewById(R.id.auth_authActivity);
        Button resetPassword = findViewById(R.id.resetPassword_authActivity);

        auth.setOnClickListener(v -> {

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
                    myProfile = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}})).getJSONObject("response");
                    JSONArray responseGetDialogs = new JSONObject(executeApiMethodGet("messages", "getDialogs", new String[][]{{}})).getJSONArray("response");
                    for (int i = 0; i < responseGetDialogs.length(); i++) {
                        dialogs.add(
                                new z_dialog(responseGetDialogs.getJSONObject(i).getInt("peerId"),
                                        (String) getProfileById(responseGetDialogs.getJSONObject(i).getInt("peerId"))[1],
                                        new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(responseGetDialogs.getJSONObject(i).getInt("lastMessageDate") * 1000L)),
                                        responseGetDialogs.getJSONObject(i).getString("lastMessage").replaceAll("\\n", "")));
                    }
                    Intent in = new Intent(authActivity.this, profilePage.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(in);
                    finish();

                } else {

                    switch (jsonAuthToken.getJSONObject("response").getString("message")) {

                        case "account banned":
                            /*Intent in = new Intent(authActivity.this, restoreProfile.class);
                            in.putExtra("reason", jsonAuthToken.getJSONObject("response").getJSONObject("details").getString("reason"));
                            in.putExtra("canRestore", jsonAuthToken.getJSONObject("response").getJSONObject("details").getBoolean("canRestoreNow"));
                            startActivity(in);
                            finish();*/
                            Toast.makeText(getApplicationContext(), "Аккаунт заблокирован", Toast.LENGTH_LONG).show();
                            break;

                        case "user not found":
                            Toast.makeText(getApplicationContext(), "Пользователь с таким логином не найден", Toast.LENGTH_LONG).show();
                            break;

                        case "invalid login or password":
                            Toast.makeText(getApplicationContext(), "Логин или пароль указаны неверно", Toast.LENGTH_LONG).show();
                            break;

                        default:
                            Toast.makeText(getApplicationContext(), "Ошибка API: " + jsonAuthToken.getJSONObject("response").getString("message"), Toast.LENGTH_LONG).show();
                            break;

                    }

                }

            } catch (Exception ex) {
                runOnUiThread(() -> writeErrorInLog(ex));
            }

        });

        resetPassword.setOnClickListener(v -> {
            Intent in = new Intent(authActivity.this, resetPassword.class);
            startActivity(in);
            finish();
        });

    }

}
package com.eviger;

import static com.eviger.z_globals.executeApiMethodPost;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;
import static com.eviger.z_globals.tokenSet;

import android.annotation.SuppressLint;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_confirm);

        email = getIntent().getStringExtra("email");
        type = getIntent().getStringExtra("type");
        hash = getIntent().getStringExtra("hashCode");
        codeConfirm = findViewById(R.id.codeFromEmail);
        checkCode = findViewById(R.id.сheckCode);

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        checkCode.setOnClickListener(v -> {

            if (!hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                return;
            }

            if (codeConfirm.getText().toString().length() != 16 || !Pattern.compile("[A-Z0-9]").matcher(codeConfirm.getText().toString()).find()) {
                Toast.makeText(getApplicationContext(), "Введенный код не соответсвует формату", Toast.LENGTH_LONG).show();
                return;
            }

            try {

                switch (type) {

                    case "changeName": {

                        JSONObject parametersPostRequest = new JSONObject();
                        parametersPostRequest.put("newName", getIntent().getStringExtra("newName"));
                        parametersPostRequest.put("email", email);
                        parametersPostRequest.put("emailCode", codeConfirm.getText().toString());
                        parametersPostRequest.put("hashCode", hash);

                        JSONObject postResponse_changeName = new JSONObject(executeApiMethodPost("user", "changeName", parametersPostRequest));

                        if (postResponse_changeName.getString("status").equals("ok")) {
                            Toast.makeText(getApplicationContext(), "Имя успешно изменено на: " + getIntent().getStringExtra("newName"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), postResponse_changeName.getJSONObject("response").getString("message"), Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(emailConfirm.this, profilePage.class));
                        finish();

                        break;

                    }
                    case "registerAccount": {

                        Intent intent;

                        JSONObject parametersPostRequest = new JSONObject();
                        parametersPostRequest.put("login", getIntent().getStringExtra("login"));
                        parametersPostRequest.put("password", getIntent().getStringExtra("password"));
                        parametersPostRequest.put("userName", getIntent().getStringExtra("name"));
                        parametersPostRequest.put("email", getIntent().getStringExtra("email"));
                        parametersPostRequest.put("emailCode", codeConfirm.getText().toString());
                        parametersPostRequest.put("hashCode", hash);

                        JSONObject postResponse_registerAccount = new JSONObject(executeApiMethodPost("user", "registerAccount", parametersPostRequest));

                        if (postResponse_registerAccount.getString("status").equals("ok")) {
                            SharedPreferences.Editor editor = tokenSet.edit();
                            editor.putString("token", postResponse_registerAccount.getJSONObject("response").getString("token"));
                            editor.putBoolean("isSigned", true);
                            editor.apply();
                            intent = new Intent(emailConfirm.this, profilePage.class);
                        } else {
                            Toast.makeText(getApplicationContext(), postResponse_registerAccount.getJSONObject("response").getString("message"), Toast.LENGTH_SHORT).show();
                            intent = new Intent(emailConfirm.this, chooseAuth.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                        break;

                    }
                    case "resetPassword": {

                        Intent intent;

                        JSONObject parametersPostRequest = new JSONObject();
                        parametersPostRequest.put("login", getIntent().getStringExtra("login"));
                        parametersPostRequest.put("newPassword", getIntent().getStringExtra("newPassword"));
                        parametersPostRequest.put("email", getIntent().getStringExtra("email"));
                        parametersPostRequest.put("emailCode", codeConfirm.getText().toString());
                        parametersPostRequest.put("hashCode", getIntent().getStringExtra("hashCode"));

                        JSONObject postResponse_resetPassword = new JSONObject(executeApiMethodPost("user", "resetPassword", parametersPostRequest));

                        if (postResponse_resetPassword.getString("status").equals("ok")) {
                            SharedPreferences.Editor editor = tokenSet.edit();
                            editor.putString("token", postResponse_resetPassword.getJSONObject("response").getString("token"));
                            editor.putBoolean("isSigned", true);
                            editor.apply();
                            intent = new Intent(emailConfirm.this, profilePage.class);
                        } else {
                            Toast.makeText(getApplicationContext(), postResponse_resetPassword.getJSONObject("response").getString("message"), Toast.LENGTH_SHORT).show();
                            intent = new Intent(emailConfirm.this, chooseAuth.class);
                        }
                        startActivity(intent);
                        finish();

                        break;

                    }
                    case "restoreAccount": {

                        //TODO: make restore user

                        break;
                    }

                }

            } catch (Throwable ex) {
                runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
            }

        });

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity && tokenSet.getBoolean("isSigned", true)) {
            setOffline();
            sendingOnline = false;
            activatedMethodUserLeaveHint = true;
        }
    }
    protected void onResume() {
        super.onResume();
        if (activatedMethodUserLeaveHint) {
            setOnline();
            sendingOnline = true;
            inAnotherActivity = false;
            activatedMethodUserLeaveHint = false;
        }
    }

}
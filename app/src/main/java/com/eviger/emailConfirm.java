package com.eviger;

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

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class emailConfirm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_confirm);

        if (hasConnection(getApplicationContext())) {

            String email = getIntent().getStringExtra("email");
            String type = getIntent().getStringExtra("type");

            TextView textSecondConfirm = findViewById(R.id.textSecondConfirm);
            EditText codeConfirm = findViewById(R.id.codeFromEmail);
            Button checkCode = findViewById(R.id.сheckCode);

            String hash = getIntent().getStringExtra("hashCode");

            textSecondConfirm.setText("Сейчас вам необходимо подтвердить свою электронную почту: "+email+", на эту почту был отправлен 16ти значный код подтверждения, введите его ниже.");

            checkCode.setOnClickListener(v -> {

                if (!codeConfirm.getText().toString().isEmpty() && codeConfirm.getText().toString().length() == 16 && Pattern.compile("[A-Z0-9]").matcher(codeConfirm.getText().toString()).find()) {

                    try {

                        if (type.equals("changeName")) {

                            JSONObject JSON = new JSONObject();
                            JSON.put("name", getIntent().getStringExtra("newName"));
                            JSON.put("email", email);
                            JSON.put("code", codeConfirm.getText().toString());
                            JSON.put("hash", hash);

                            executeApiMethodPost("user", "changeName", JSON);
                            startActivity(new Intent(emailConfirm.this, settingsAccount.class));
                            Toast.makeText(getApplicationContext(), "Имя успешно изменено на: " + getIntent().getStringExtra("newName"), Toast.LENGTH_SHORT).show();
                            finish();

                        } else if (type.equals("registerAccount")) {

                            JSONObject JSON = new JSONObject();
                            JSON.put("login", getIntent().getStringExtra("login"));
                            JSON.put("password", getIntent().getStringExtra("password"));
                            JSON.put("email", getIntent().getStringExtra("email"));
                            JSON.put("registration_email_code", codeConfirm.getText().toString());
                            JSON.put("username", getIntent().getStringExtra("name"));
                            JSON.put("hashCode", hash);

                            JSONObject jsonAccount = new JSONObject(executeApiMethodPost("user", "registerAccount", JSON));
                            SharedPreferences.Editor ed = globals.tokenSet.edit();
                            ed.putString("token", jsonAccount.getJSONObject("response").getString("token"));
                            ed.putBoolean("isSigned", true);
                            ed.apply();
                            Intent inAccount = new Intent(emailConfirm.this, profilePage.class);
                            startActivity(inAccount);
                            finish();

                        } else {

                            JSONObject JSON = new JSONObject();
                            JSON.put("email", email);
                            JSON.put("code", codeConfirm.getText().toString());
                            JSON.put("hash", hash);

                            JSONObject json = new JSONObject(executeApiMethodPost("email", "confirmCode", JSON));

                            if (json.getJSONObject("response").getString("status").equals("correct")) {

                                switch (type) {

                                    case "recoveryAccount":

                                        Intent inRecovery = new Intent(emailConfirm.this, recoverySecond.class);
                                        inRecovery.putExtra("email", email);
                                        inRecovery.putExtra("code", codeConfirm.getText().toString());
                                        inRecovery.putExtra("hash", hash);
                                        startActivity(inRecovery);
                                        finish();

                                     break;
                                }

                            } else {

                                Toast.makeText(getApplicationContext(), "Введенный код указан неверно!", Toast.LENGTH_LONG).show();

                            }

                        }

                    } catch (Throwable ex) {
                        runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                    }

                } else if (codeConfirm.getText().toString().isEmpty() || codeConfirm.getText().toString().length() != 16 || !Pattern.compile("[A-Z0-9]").matcher(codeConfirm.getText().toString()).find()) {

                    Toast.makeText(getApplicationContext(), "Введенный код не соответсвует формату!", Toast.LENGTH_LONG).show();

                }

            });

        }

    }

}
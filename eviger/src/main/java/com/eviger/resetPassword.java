package com.eviger;

import static com.eviger.z_globals.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class resetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        EditText login = findViewById(R.id.login_resetPassword);
        EditText email = findViewById(R.id.email_resetPassword);
        EditText newPassword = findViewById(R.id.newPassword_resetPassword);
        Button checkEmail = findViewById(R.id.toEmailCheck_resetPassword);

        checkEmail.setOnClickListener(v -> {
            if (!hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                return;
            }

            if (!Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$").matcher(email.getText().toString()).find()) {
                Toast.makeText(getApplicationContext(), "Почта введена некорректно", Toast.LENGTH_LONG).show();
                return;
            }

            try {

                JSONObject parametersPostRequest = new JSONObject();
                parametersPostRequest.put("login", login.getText().toString());
                parametersPostRequest.put("newPassword", newPassword.getText().toString());
                parametersPostRequest.put("email", email.getText().toString());

                JSONObject postResponse_resetPassword = new JSONObject(executeApiMethodPost("user", "resetPassword", parametersPostRequest));

                if (postResponse_resetPassword.getJSONObject("response").getString("status").equals("confirm your email")) {

                    JSONObject postResponse_requestEmailCode = new JSONObject(requestEmailCode(z_globals.myProfile.getString("email")));

                    if (postResponse_requestEmailCode.getString("status").equals("ok") || postResponse_requestEmailCode.getJSONObject("response").getString("message").equals("code has already been requested")) {

                        Intent in = new Intent(resetPassword.this, emailConfirm.class);
                        in.putExtra("type", "resetPassword")
                                .putExtra("login", login.getText().toString())
                                .putExtra("email", email.getText().toString())
                                .putExtra("newPassword", newPassword.getText().toString())
                                .putExtra("hashCode", postResponse_requestEmailCode.getJSONObject("response").getString("hash"));

                        startActivity(in);
                        finish();

                    } else {

                        Toast.makeText(getApplicationContext(), postResponse_resetPassword.getJSONObject("response").getString("message"), Toast.LENGTH_LONG).show();

                    }

                } else {

                    switch (postResponse_resetPassword.getJSONObject("response").getString("message")) {
                        case "login/email pair is not correct":
                            Toast.makeText(getApplicationContext(), "Пара логин/почта не подошли", Toast.LENGTH_LONG).show();
                            break;

                        default:
                            Toast.makeText(getApplicationContext(), postResponse_resetPassword.getJSONObject("response").getString("message"), Toast.LENGTH_LONG).show();
                            break;
                    }

                }

            } catch (Exception ex) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    writeErrorInLog(ex);
                });
            }

        });

    }

}
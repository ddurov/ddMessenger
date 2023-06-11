package com.ddprojects.messager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.method.User;
import com.ddprojects.messager.service.globals;

import java.util.Hashtable;
import java.util.regex.Pattern;

public class welcomeActivity extends AppCompatActivity {

    EditText login;
    EditText password;
    Button auth;
    Button cancelToRegister;
    Button forgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        login = findViewById(R.id.loginField);
        password = findViewById(R.id.passwordField);
        auth = findViewById(R.id.authButton);
        cancelToRegister = findViewById(R.id.cancelToRegister);
        forgotPassword = findViewById(R.id.forgotPasswordButton);

        auth.setOnClickListener(v -> auth());

        forgotPassword.setOnClickListener(v -> {

        });

    }

    private void auth() {
        if (login.getText().toString().equals("")) {
            globals.showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.loginHint)), true);
            return;
        }

        if (password.getText().toString().equals("")) {
            globals.showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.passwordHint)), true);
            return;
        }

        if (login.getText().toString().length() < 6
                || login.getText().toString().length() > 64) {
            globals.showToastMessage(getString(R.string.error_field_invalid_length)
                    .replace("{field}", getString(R.string.loginHint))
                    .replace("{length}", "> 6 & 64 <"), true
            );
            return;
        }

        if (password.getText().toString().length() < 8) {
            globals.showToastMessage(getString(R.string.error_field_invalid_length)
                    .replace("{field}", getString(R.string.passwordHint))
                    .replace("{length}", "> 8"), true);
        }

        if (Pattern.compile("[^a-zA-Z0-9@$!%*?&+~|{}:;<>/.]+").matcher(login.getText().toString()).find()) {
            globals.showToastMessage(getString(R.string.error_field_no_match_regex)
                            .replace("{field}", getString(R.string.loginHint))
                            .replace("{condition}", "letters + digits + special symbols"),
                    true);
            return;
        }

        if (Pattern.compile("[^a-zA-Z0-9@$!%*?&+~|{}:;<>/.]+").matcher(password.getText().toString()).find()) {
            globals.showToastMessage(getString(R.string.error_field_no_match_regex)
                            .replace("{field}", getString(R.string.passwordHint))
                            .replace("{condition}", "letters + digits + special symbols"),
                    true);
            return;
        }

        Hashtable<String, String> params = new Hashtable<>();
        params.put("login", login.getText().toString().trim());
        params.put("password", password.getText().toString().trim());

        auth.setClickable(false);
        auth.setText(R.string.auth_state_button);

        new Thread(() -> {
            try {
                String session = User.auth(params);
                runOnUiThread(() -> globals.showToastMessage(session, false));
            } catch (APIException API) {
                if (API.getCode() == 404) {
                    runOnUiThread(() -> auth.setText(R.string.register));
                    auth.setOnClickListener(view -> startActivity(new Intent(
                            welcomeActivity.this,
                            emailActivity.class
                    )));
                    runOnUiThread(() -> cancelToRegister.setVisibility(View.VISIBLE));
                    cancelToRegister.setOnClickListener(view -> {
                        auth.setOnClickListener(View -> auth());
                        runOnUiThread(() -> auth.setText(R.string.auth));
                        cancelToRegister.setVisibility(View.GONE);
                    });
                } else {
                    runOnUiThread(() -> auth.setText(R.string.auth));
                    globals.showToastMessage(
                            APIException.translate("user", API.getMessage()),
                            false
                    );
                }
            } finally {
                auth.setClickable(true);
            }
        }).start();
    }
}
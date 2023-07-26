package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.log;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.models.SuccessResponse;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.globals;

import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class welcomeActivity extends AppCompatActivity {

    EditText login;
    EditText password;
    Button auth;
    Button register;
    Button cancelToRegister;
    Button forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        login = findViewById(R.id.loginField);
        password = findViewById(R.id.passwordField);
        auth = findViewById(R.id.authButton);
        register = findViewById(R.id.registerButton);
        cancelToRegister = findViewById(R.id.cancelToRegister);
        forgotPassword = findViewById(R.id.forgotPasswordButton);

        auth.setOnClickListener(v -> auth());

        register.setOnClickListener(v -> register());

        cancelToRegister.setOnClickListener(v -> {
            auth.setOnClickListener(View -> auth());
            auth.setText(R.string.welcomeAuthButton);
            auth.setVisibility(View.VISIBLE);
            register.setVisibility(View.GONE);
            cancelToRegister.setVisibility(View.GONE);
        });

        forgotPassword.setOnClickListener(v -> {

        });

    }

    private void auth() {
        if (_verifyField()) return;

        Hashtable<String, String> params = new Hashtable<>();
        params.put("login", login.getText().toString().trim());
        params.put("password", password.getText().toString().trim());

        auth.setClickable(false);
        auth.setText(R.string.welcomeAuthButtonProcess);

        new Thread(() -> {
            SuccessResponse response;
            try {
                response = executeApiMethodSync(
                        "get",
                        "product",
                        "user",
                        "auth",
                        params
                );

                showToastMessage(
                        response.body.toString(),
                        false
                );
            } catch (APIException API) {
                if (API.getCode() == 404) {
                    runOnUiThread(() -> {
                        auth.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        cancelToRegister.setVisibility(View.VISIBLE);
                    });
                } else {
                    globals.showToastMessage(
                            APIException.translate("user", API.getMessage()),
                            false
                    );
                }
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            } finally {
                runOnUiThread(() -> {
                    auth.setText(R.string.welcomeAuthButton);
                    auth.setClickable(true);
                });
            }
        }).start();
    }

    private void register() {
        if (_verifyField()) return;
        
        findViewById(R.id.loader).setVisibility(View.VISIBLE);
        register.setVisibility(View.GONE);
        cancelToRegister.setVisibility(View.GONE);

        globals.liveData.put("register_login", login.getText().toString().trim());
        globals.liveData.put("register_password", password.getText().toString().trim());

        Intent emailActivity = new Intent(this, emailActivity.class);
        emailActivity.putExtra("requestEmail", true);

        startActivity(emailActivity);

        findViewById(R.id.loader).setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);

        register.setOnClickListener(v -> {
            globals.liveData.forEach((key, value) -> {
                log("Key: "+key+", Value: "+value+"\n");
            });
        });
    }

    private boolean _verifyField() {
        boolean result = false;

        if (login.getText().toString().equals("")) {
            globals.showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.welcomeLoginHint)), true);
        } else if (password.getText().toString().equals("")) {
            globals.showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.welcomePasswordHint)), true);
        } else if (login.getText().toString().length() < 6
                || login.getText().toString().length() > 64) {
            globals.showToastMessage(getString(R.string.error_field_invalid_length)
                    .replace("{field}", getString(R.string.welcomeLoginHint))
                    .replace("{length}", "> 6 & 64 >"), true);
        } else if (password.getText().toString().length() < 8) {
            globals.showToastMessage(getString(R.string.error_field_invalid_length)
                    .replace("{field}", getString(R.string.welcomePasswordHint))
                    .replace("{length}", "> 8"), true);
        } else if (Pattern.compile("[^a-zA-Z0-9@$!%*?&+~|{}:;<>/.]+").matcher(login.getText().toString()).find()) {
            globals.showToastMessage(getString(R.string.error_field_no_match_regex)
                            .replace("{field}", getString(R.string.welcomeLoginHint))
                            .replace("{condition}", getString(R.string.regexHumanTranslationLDSS)),
                    true);
        } else if (Pattern.compile("[^a-zA-Z0-9@$!%*?&+~|{}:;<>/.]+").matcher(password.getText().toString()).find()) {
            globals.showToastMessage(getString(R.string.error_field_no_match_regex)
                            .replace("{field}", getString(R.string.welcomePasswordHint))
                            .replace("{condition}", getString(R.string.regexHumanTranslationLDSS)),
                    true);
        } else {
            result = true;
        }

        return !result;
    }
}
package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.PDDEditor;
import static com.ddprojects.messager.service.globals.log;
import static com.ddprojects.messager.service.globals.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.service.SerializedAction;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.globals;

import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class welcomeActivity extends AppCompatActivity {

    EditText login;
    EditText password;
    EditText email;
    EditText username;
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
        email = findViewById(R.id.emailField);
        username = findViewById(R.id.usernameField);
        auth = findViewById(R.id.authButton);
        register = findViewById(R.id.registerButton);
        cancelToRegister = findViewById(R.id.cancelToRegister);
        forgotPassword = findViewById(R.id.forgotPasswordButton);

        if (persistentDataOnDisk.contains("email_createCode_time") &&
                persistentDataOnDisk.getInt("email_createCode_time", 0) >=
                        ((int) (System.currentTimeMillis() / 1000L)) - 300
        ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.welcomeDialogFoundIncompleteRegistrationHeader);
            builder.setMessage(R.string.welcomeDialogFoundIncompleteRegistrationBody);
            builder.setPositiveButton(
                    R.string.welcomeDialogFoundIncompleteRegistrationRestore,
                    (dialogInterface, i) -> {
                        if (!persistentDataOnDisk.contains("email_confirmed")) {
                            Intent emailActivity = new Intent(this, confirmEmailActivity.class);
                            emailActivity.putExtra(
                                    "hash",
                                    persistentDataOnDisk.getString("email_hash", null)
                            );
                            emailActivity.putExtra(
                                    "actionAfterConfirm",
                                    (SerializedAction) () -> {
                                        PDDEditor.putBoolean("email_confirmed", true);

                                        Intent welcomeActivity = new Intent(
                                                fakeContext.getInstance().getApplicationContext(),
                                                welcomeActivity.class
                                        );
                                        welcomeActivity.putExtra(
                                                "finalRegisterStep",
                                                true
                                        );
                                        welcomeActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                                        fakeContext.getInstance().startActivity(welcomeActivity);
                                    }
                                    );
                            startActivity(emailActivity);
                        } else finalRegister();
                    }
            );
            builder.setNegativeButton(
                    R.string.welcomeDialogFoundIncompleteRegistrationReset,
                    (dialogInterface, i) -> _resetRegistrationField()
            );

            builder.show();
        } else {
            _resetRegistrationField();
        }

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getBooleanExtra("finalRegisterStep", false)) {
            finalRegister();
        }
    }

    private void auth() {
        if (_verifyField()) return;

        auth.setClickable(false);
        auth.setText(R.string.welcomeAuthButtonProcess);

        new Thread(() -> {
            try {
                Hashtable<String, String> userAuthParams = new Hashtable<>();
                userAuthParams.put("login", login.getText().toString().trim());
                userAuthParams.put("password", password.getText().toString().trim());

                String session = executeApiMethodSync(
                        "get",
                        "product",
                        "user",
                        "auth",
                        userAuthParams
                ).body.getAsString();

                PDDEditor.putString("sessionId", session);

                Hashtable<String, String> tokenCreateParams = new Hashtable<>();
                tokenCreateParams.put("tokenType", "0");

                String token = executeApiMethodSync(
                        "post",
                        "product",
                        "token",
                        "create",
                        tokenCreateParams
                ).body.getAsString();

                PDDEditor.putString("token", token);

                log(token);
            } catch (APIException APIEx) {
                if (APIEx.getCode() == 404) {
                    runOnUiThread(() -> {
                        auth.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        cancelToRegister.setVisibility(View.VISIBLE);
                    });
                } else {
                    globals.showToastMessage(
                            APIException.translate("user", APIEx.getMessage()),
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

        PDDEditor.putString("register_login", login.getText().toString().trim());
        PDDEditor.putString("register_password", password.getText().toString().trim());

        startActivity(
                new Intent(getApplicationContext(), setEmailActivity.class)
                        .putExtra("needRemove", false)
        );
    }

    private void finalRegister() {
        ((TextView) findViewById(R.id.hint)).setText(R.string.welcomeFinalRegisterHint);

        findViewById(R.id.loader).setVisibility(View.GONE);
        auth.setVisibility(View.GONE);
        forgotPassword.setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);

        login.setEnabled(false);
        password.setEnabled(false);
        email.setEnabled(false);

        login.setText(persistentDataOnDisk.getString("register_login", null));
        password.setText(persistentDataOnDisk.getString("register_password", null));
        email.setText(persistentDataOnDisk.getString("register_email", null));

        register.setOnClickListener(v -> new Thread(() -> {
            if (username.getText().toString().equals("")) {
                globals.showToastMessage(getString(R.string.error_field_are_empty)
                        .replace("{field}", getString(R.string.welcomeUsernameHint)), true);
                return;
            }

            Hashtable<String, String> userAuthParams = new Hashtable<>();

            userAuthParams.put("login", persistentDataOnDisk.getString("register_login", null));
            userAuthParams.put("password", persistentDataOnDisk.getString("register_password", null));
            Hashtable<String, String> userRegisterParams = new Hashtable<>(userAuthParams);
            userRegisterParams.put("username", username.getText().toString());
            userRegisterParams.put("email", persistentDataOnDisk.getString("register_email", null));
            userRegisterParams.put("emailCode", persistentDataOnDisk.getString("email_code", null));
            userRegisterParams.put("hash", persistentDataOnDisk.getString("email_hash", null));

            try {
                executeApiMethodSync(
                        "post",
                        "product",
                        "user",
                        "register",
                        userRegisterParams
                );

                String session = executeApiMethodSync(
                        "get",
                        "product",
                        "user",
                        "auth",
                        userAuthParams
                ).body.getAsString();

                PDDEditor.putString("sessionId", session);

                Hashtable<String, String> tokenCreateParams = new Hashtable<>();
                tokenCreateParams.put("tokenType", "0");

                String token = executeApiMethodSync(
                        "post",
                        "product",
                        "token",
                        "create",
                        tokenCreateParams
                ).body.getAsString();

                PDDEditor.putString("token", token);

                log(token);
            } catch (APIException APIEx) {
                globals.showToastMessage(
                        APIException.translate("user", APIEx.getMessage()),
                        false
                );
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            }
        }).start());
    }

    private void _resetRegistrationField() {
        PDDEditor.remove("register_login")
                .remove("register_password")
                .remove("register_email")
                .remove("email_createCode_time")
                .remove("email_code")
                .remove("email_hash")
                .remove("email_confirmed");
        PDDEditor.apply();
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
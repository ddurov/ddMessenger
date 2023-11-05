package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.fakeContext.liveData;
import static com.ddprojects.messager.service.fakeContext.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.removeKeysFromSP;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;
import static com.ddprojects.messager.service.globals.writeKeyPairToSP;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.serializedAction;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.fakeContext;

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

        auth.setOnClickListener(v -> auth());

        register.setOnClickListener(v -> register());

        cancelToRegister.setOnClickListener(v -> {
            auth.setOnClickListener(View -> auth());
            auth.setText(R.string.welcomeAuthButton);
            auth.setVisibility(View.VISIBLE);
            register.setVisibility(View.GONE);
            cancelToRegister.setVisibility(View.GONE);
        });

        forgotPassword.setOnClickListener(v -> resetPassword());
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
                ).getBody().getAsString();

                writeKeyPairToSP("sessionId", session);

                Hashtable<String, String> tokenCreateParams = new Hashtable<>();
                tokenCreateParams.put("tokenType", "0");

                String token = executeApiMethodSync(
                        "post",
                        "product",
                        "token",
                        "create",
                        tokenCreateParams
                ).getBody().getAsString();

                writeKeyPairToSP("token", token);

                startActivity(new Intent(this, dialogsActivity.class));
                finish();
            } catch (APIException APIEx) {
                if (APIEx.getCode() == 404) {
                    runOnUiThread(() -> {
                        auth.setVisibility(View.GONE);
                        register.setVisibility(View.VISIBLE);
                        cancelToRegister.setVisibility(View.VISIBLE);
                    });
                } else {
                    showToastMessage(
                            APIException.translate(APIEx.getMessage()),
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

        if (persistentDataOnDisk.contains("email_createCode_time") &&
                persistentDataOnDisk.getInt("email_createCode_time", 0) >=
                        ((int) (System.currentTimeMillis() / 1000L)) - 60 * 60
        ) {
            startActivity(
                    new Intent(getApplicationContext(), confirmEmailActivity.class)
                            .putExtra("needRemove", false)
                            .putExtra(
                                    "hash",
                                    persistentDataOnDisk.getString("email_hash", null)
                            )
                            .putExtra(
                                    "actionAfterConfirm",
                                    (serializedAction) () -> {
                                        Intent welcomeActivity = new Intent(
                                                fakeContext.getInstance().getApplicationContext(),
                                                welcomeActivity.class
                                        );

                                        welcomeActivity.putExtra("finalRegisterStep", true);
                                        welcomeActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                                        fakeContext.getInstance().startActivity(welcomeActivity);
                                    }
                            )
            );
        } else {
            startActivity(
                    new Intent(getApplicationContext(), preConfirmEmailActivity.class)
                            .putExtra("needRemove", false)
            );
        }
    }

    private void resetPassword() {
        if (_verifyField()) return;

        new Thread(() -> {
            Hashtable<String, String> userResetPasswordParams = new Hashtable<>();
            userResetPasswordParams.put("login", login.getText().toString());
            userResetPasswordParams.put("newPassword", password.getText().toString());

            try {
                SuccessResponse resetPasswordResponse = executeApiMethodSync(
                        "post",
                        "product",
                        "user",
                        "resetPassword",
                        userResetPasswordParams
                );

                if (resetPasswordResponse.getCode() == 202) {
                    String hash = resetPasswordResponse.getBody().toString();

                    Intent emailActivity = new Intent(this, confirmEmailActivity.class);

                    emailActivity
                            .putExtra("hash", hash)
                            .putExtra("needRemove", false)
                            .putExtra(
                                    "actionAfterConfirm",
                                    (serializedAction) () -> {
                                        try {
                                            Hashtable<String, String> userResetPasswordConfirmParams =
                                                    new Hashtable<>(userResetPasswordParams);

                                            userResetPasswordConfirmParams.put(
                                                    "emailCode",
                                                    (String) liveData.get("email_code")
                                            );
                                            userResetPasswordConfirmParams.put("hash", hash);

                                            executeApiMethodSync(
                                                    "post",
                                                    "product",
                                                    "user",
                                                    "resetPassword",
                                                    userResetPasswordConfirmParams
                                            );
                                        } catch (APIException APIEx) {
                                            showToastMessage(
                                                    APIException.translate(APIEx.getMessage()),
                                                    false
                                            );
                                        } catch (IOException IOEx) {
                                            writeErrorInLog(IOEx);
                                            showToastMessage(
                                                    fakeContext.getInstance().getString(R.string.error_request_failed),
                                                    false
                                            );
                                        }

                                        Intent welcomeActivity = new Intent(
                                                fakeContext.getInstance().getApplicationContext(),
                                                welcomeActivity.class
                                        );

                                        welcomeActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                                        fakeContext.getInstance().startActivity(welcomeActivity);
                                    }
                            );

                    startActivity(emailActivity);
                }
            } catch (APIException APIEx) {
                showToastMessage(
                        APIException.translate(APIEx.getMessage()),
                        false
                );
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            }
        }).start();
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

        email.setText(persistentDataOnDisk.getString("email", null));

        register.setOnClickListener(v -> new Thread(() -> {
            if (username.getText().toString().isEmpty()) {
                showToastMessage(getString(R.string.error_field_are_empty)
                        .replace("{field}", getString(R.string.welcomeUsernameHint)), true);
                return;
            }

            try {
                Hashtable<String, String> userAuthParams = new Hashtable<>();

                userAuthParams.put("login", login.getText().toString());
                userAuthParams.put("password", password.getText().toString());
                Hashtable<String, String> userRegisterParams = new Hashtable<>(userAuthParams);
                userRegisterParams.put("username", username.getText().toString());
                userRegisterParams.put("email", persistentDataOnDisk.getString("email", null));
                userRegisterParams.put("emailCode", (String) liveData.get("email_code"));
                userRegisterParams.put("hash", persistentDataOnDisk.getString("email_hash", null));

                if (executeApiMethodSync(
                        "post",
                        "product",
                        "user",
                        "register",
                        userRegisterParams
                ).getCode() == 200) _resetInternalField();

                String sessionId = executeApiMethodSync(
                        "get",
                        "product",
                        "user",
                        "auth",
                        userAuthParams
                ).getBody().getAsString();

                writeKeyPairToSP("sessionId", sessionId);

                Hashtable<String, String> tokenCreateParams = new Hashtable<>();
                tokenCreateParams.put("tokenType", "0");

                String token = executeApiMethodSync(
                        "post",
                        "product",
                        "token",
                        "create",
                        tokenCreateParams
                ).getBody().getAsString();

                writeKeyPairToSP("token", token);

                startActivity(new Intent(this, dialogsActivity.class));
                finish();
            } catch (APIException APIEx) {
                showToastMessage(
                        APIException.translate(APIEx.getMessage()),
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

    private void _resetInternalField() {
        removeKeysFromSP(new String[]{
                "email",
                "email_hash",
        });
    }

    private boolean _verifyField() {
        boolean result = false;

        if (login.getText().toString().isEmpty()) {
            showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.welcomeLoginHint)), true);
        } else if (password.getText().toString().isEmpty()) {
            showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.welcomePasswordHint)), true);
        } else if (login.getText().toString().length() < 6
                || login.getText().toString().length() > 64) {
            showToastMessage(getString(R.string.error_field_invalid_length)
                    .replace("{field}", getString(R.string.welcomeLoginHint))
                    .replace("{length}", "> 6 & 64 >"), true);
        } else if (password.getText().toString().length() < 8) {
            showToastMessage(getString(R.string.error_field_invalid_length)
                    .replace("{field}", getString(R.string.welcomePasswordHint))
                    .replace("{length}", "> 8"), true);
        } else if (Pattern.compile("[^a-zA-Z0-9@$!%*?&+~|{}:;<>/.]+").matcher(login.getText().toString()).find()) {
            showToastMessage(getString(R.string.error_field_no_match_regex)
                            .replace("{field}", getString(R.string.welcomeLoginHint))
                            .replace("{condition}", getString(R.string.regexHumanTranslationLDSS)),
                    true);
        } else if (Pattern.compile("[^a-zA-Z0-9@$!%*?&+~|{}:;<>/.]+").matcher(password.getText().toString()).find()) {
            showToastMessage(getString(R.string.error_field_no_match_regex)
                            .replace("{field}", getString(R.string.welcomePasswordHint))
                            .replace("{condition}", getString(R.string.regexHumanTranslationLDSS)),
                    true);
        } else {
            result = true;
        }

        return !result;
    }
}
package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.globals.cachedData;
import static com.ddprojects.messager.service.globals.liveData;
import static com.ddprojects.messager.service.globals.persistentDataOnDisk;
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
import com.ddprojects.messager.models.User;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.appService;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.serializedAction;
import com.google.gson.Gson;

import java.util.Hashtable;
import java.util.regex.Pattern;

public class welcomeActivity extends AppCompatActivity {
    EditText login, password, email, username;
    Button auth, register, cancelToRegister, forgotPassword;

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
        if (!_verifyField()) return;

        auth.setClickable(false);
        auth.setText(R.string.welcomeAuthButtonProcess);

        Hashtable<String, String> userAuthParams = new Hashtable<>();
        userAuthParams.put("login", login.getText().toString().trim());
        userAuthParams.put("password", password.getText().toString().trim());

        executeApiMethod(
                "get",
                "product",
                "user",
                "auth",
                userAuthParams,
                new APIRequester.Callback() {
                    @Override
                    public void onFailure(Exception exception) {
                        if (exception instanceof APIException) {
                            if (((APIException) exception).getCode() == 404) {
                                runOnUiThread(() -> {
                                    auth.setVisibility(View.GONE);
                                    register.setVisibility(View.VISIBLE);
                                    cancelToRegister.setVisibility(View.VISIBLE);
                                });
                            } else {
                                showToastMessage(
                                        APIException.translate(exception.getMessage()),
                                        false
                                );
                            }
                        } else {
                            writeErrorInLog(exception);
                        }

                        runOnUiThread(() -> {
                            auth.setText(R.string.welcomeAuthButton);
                            auth.setClickable(true);
                        });
                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        writeKeyPairToSP("sessionId", response.getBody().getAsString());

                        Hashtable<String, String> tokenCreateParams = new Hashtable<>();
                        tokenCreateParams.put("tokenType", "0");

                        executeApiMethod(
                                "post",
                                "product",
                                "token",
                                "create",
                                tokenCreateParams,
                                new APIRequester.Callback() {
                                    @Override
                                    public void onFailure(Exception exception) {
                                        if (exception instanceof APIException) {
                                            showToastMessage(
                                                    APIException.translate(exception.getMessage()),
                                                    false
                                            );
                                        } else {
                                            writeErrorInLog(exception);
                                        }
                                    }

                                    @Override
                                    public void onSuccess(SuccessResponse response) {
                                        writeKeyPairToSP("token", response.getBody().getAsString());

                                        executeApiMethod(
                                                "get",
                                                "product",
                                                "user",
                                                "get",
                                                new Hashtable<>(),
                                                new APIRequester.Callback() {
                                                    @Override
                                                    public void onFailure(Exception exception) {
                                                        if (exception instanceof APIException) {
                                                            showToastMessage(
                                                                    APIException.translate(exception.getMessage()),
                                                                    false
                                                            );
                                                        } else {
                                                            writeErrorInLog(exception);
                                                        }
                                                    }

                                                    @Override
                                                    public void onSuccess(SuccessResponse response) {
                                                        cachedData.put("user", new Gson().fromJson(
                                                                response.getBody(),
                                                                User.class
                                                        ));

                                                        startService(new Intent(
                                                                welcomeActivity.this,
                                                                appService.class
                                                        ));
                                                        startActivity(new Intent(
                                                                welcomeActivity.this,
                                                                dialogsActivity.class
                                                        ));
                                                        finish();
                                                    }
                                                }
                                        );
                                    }
                                }
                        );
                    }
                }
        );
    }

    private void register() {
        if (!_verifyField()) return;

        Bundle commonData = new Bundle();
        commonData.putBoolean("needRemove", false);
        commonData.putParcelable(
                "intent",
                new Intent(
                        fakeContext.getInstance().getApplicationContext(),
                        welcomeActivity.class
                )
                        .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .putExtra("finalRegisterStep", true)
        );

        if (persistentDataOnDisk.contains("email_createCode_time") &&
                persistentDataOnDisk.getInt("email_createCode_time", 0) >=
                        ((int) (System.currentTimeMillis() / 1000L)) - 60 * 60
        ) {
            startActivity(
                    new Intent(getApplicationContext(), confirmEmailActivity.class)
                            .putExtra(
                                    "hash",
                                    persistentDataOnDisk.getString("email_hash", null)
                            )
                            .putExtras(commonData)
            );
        } else {
            startActivity(
                    new Intent(getApplicationContext(), preConfirmEmailActivity.class)
                            .putExtras(commonData)
            );
        }
    }

    private void resetPassword() {
        if (!_verifyField()) return;

        Hashtable<String, String> userResetPasswordParams = new Hashtable<>();
        userResetPasswordParams.put("login", login.getText().toString());
        userResetPasswordParams.put("newPassword", password.getText().toString());

        executeApiMethod(
                "post",
                "product",
                "user",
                "resetPassword",
                userResetPasswordParams,
                new APIRequester.Callback() {
                    @Override
                    public void onFailure(Exception exception) {
                        if (exception instanceof APIException) {
                            showToastMessage(
                                    APIException.translate(exception.getMessage()),
                                    false
                            );
                        } else {
                            writeErrorInLog(exception);
                        }
                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        if (response.getCode() == 202) {
                            String hash = response.getBody().getAsString();

                            Intent emailActivity = new Intent(
                                    welcomeActivity.this,
                                    confirmEmailActivity.class
                            );

                            userResetPasswordParams.put("hash", hash);

                            emailActivity
                                    .putExtra("hash", hash)
                                    .putExtra("needRemove", false)
                                    .putExtra(
                                            "actionAfterConfirm",
                                            (serializedAction) () -> {
                                                userResetPasswordParams.put(
                                                        "emailCode",
                                                        (String) liveData.get("email_code")
                                                );

                                                executeApiMethod(
                                                        "post",
                                                        "product",
                                                        "user",
                                                        "resetPassword",
                                                        userResetPasswordParams,
                                                        new APIRequester.Callback() {
                                                            @Override
                                                            public void onFailure(Exception exception) {
                                                                if (exception instanceof APIException) {
                                                                    showToastMessage(
                                                                            APIException.translate(exception.getMessage()),
                                                                            false
                                                                    );
                                                                } else {
                                                                    writeErrorInLog(exception);
                                                                }
                                                            }

                                                            @Override
                                                            public void onSuccess(SuccessResponse response) {
                                                                fakeContext.getInstance().startActivity(new Intent(
                                                                        fakeContext.getInstance().getApplicationContext(),
                                                                        welcomeActivity.class
                                                                ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                                                            }
                                                        }
                                                );
                                            }
                                    );

                            startActivity(emailActivity);
                        }
                    }
                }
        );
    }

    private void finalRegister() {
        ((TextView) findViewById(R.id.hint)).setText(R.string.welcomeFinalRegisterHint);

        forgotPassword.setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);

        login.setEnabled(false);
        password.setEnabled(false);
        email.setEnabled(false);

        email.setText(persistentDataOnDisk.getString("email", null));

        register.setOnClickListener(v -> {
            if (username.getText().toString().isEmpty()) {
                showToastMessage(getString(R.string.error_field_are_empty)
                        .replace("{field}", getString(R.string.welcomeUsernameHint)), true);
                return;
            }

            Hashtable<String, String> userAuthParams = new Hashtable<>();

            userAuthParams.put("login", login.getText().toString());
            userAuthParams.put("password", password.getText().toString());
            Hashtable<String, String> userRegisterParams = new Hashtable<>(userAuthParams);
            userRegisterParams.put("username", username.getText().toString());
            userRegisterParams.put("email", persistentDataOnDisk.getString("email", null));
            userRegisterParams.put("emailCode", (String) liveData.get("email_code"));
            userRegisterParams.put("hash", persistentDataOnDisk.getString("email_hash", null));

            executeApiMethod(
                    "post",
                    "product",
                    "user",
                    "register",
                    userRegisterParams,
                    new APIRequester.Callback() {
                        @Override
                        public void onFailure(Exception exception) {
                            if (exception instanceof APIException) {
                                showToastMessage(
                                        APIException.translate(exception.getMessage()),
                                        false
                                );
                            } else {
                                writeErrorInLog(exception);
                            }
                        }

                        @Override
                        public void onSuccess(SuccessResponse response) {
                            _resetInternalField();

                            executeApiMethod(
                                    "get",
                                    "product",
                                    "user",
                                    "auth",
                                    userAuthParams,
                                    new APIRequester.Callback() {
                                        @Override
                                        public void onFailure(Exception exception) {
                                            if (exception instanceof APIException) {
                                                showToastMessage(
                                                        APIException.translate(exception.getMessage()),
                                                        false
                                                );
                                            } else {
                                                writeErrorInLog(exception);
                                            }
                                        }

                                        @Override
                                        public void onSuccess(SuccessResponse response) {
                                            writeKeyPairToSP("sessionId", response.getBody().getAsString());

                                            Hashtable<String, String> tokenCreateParams = new Hashtable<>();
                                            tokenCreateParams.put("tokenType", "0");

                                            executeApiMethod(
                                                    "post",
                                                    "product",
                                                    "token",
                                                    "create",
                                                    tokenCreateParams,
                                                    new APIRequester.Callback() {
                                                        @Override
                                                        public void onFailure(Exception exception) {
                                                            if (exception instanceof APIException) {
                                                                showToastMessage(
                                                                        APIException.translate(exception.getMessage()),
                                                                        false
                                                                );
                                                            } else {
                                                                writeErrorInLog(exception);
                                                            }
                                                        }

                                                        @Override
                                                        public void onSuccess(SuccessResponse response) {
                                                            writeKeyPairToSP("token", response.getBody().getAsString());

                                                            executeApiMethod(
                                                                    "get",
                                                                    "product",
                                                                    "user",
                                                                    "get",
                                                                    new Hashtable<>(),
                                                                    new APIRequester.Callback() {
                                                                        @Override
                                                                        public void onFailure(Exception exception) {
                                                                            if (exception instanceof APIException) {
                                                                                showToastMessage(
                                                                                        APIException.translate(exception.getMessage()),
                                                                                        false
                                                                                );
                                                                            } else {
                                                                                writeErrorInLog(exception);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onSuccess(SuccessResponse response) {
                                                                            cachedData.put("user", new Gson().fromJson(
                                                                                    response.getBody(),
                                                                                    User.class
                                                                            ));

                                                                            startService(new Intent(
                                                                                    welcomeActivity.this,
                                                                                    appService.class
                                                                            ));
                                                                            startActivity(new Intent(
                                                                                    welcomeActivity.this,
                                                                                    dialogsActivity.class
                                                                            ));
                                                                            finish();
                                                                        }
                                                                    }
                                                            );
                                                        }
                                                    }
                                            );
                                        }
                                    }
                            );
                        }
                    }
            );
        });
    }

    private void _resetInternalField() {
        removeKeysFromSP(new String[]{
                "email",
                "email_hash",
                "email_createCode_time"
        });
        liveData.remove("email_code");
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

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getBooleanExtra("finalRegisterStep", false)) {
            finalRegister();
        }
    }
}
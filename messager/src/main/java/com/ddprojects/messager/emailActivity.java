package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodAsync;
import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.PDDEditor;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.models.SuccessResponse;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.globals;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class emailActivity extends AppCompatActivity {

    TextView hint;
    EditText field;
    Button setEmail;
    Button confirmCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        hint = findViewById(R.id.hint);
        field = findViewById(R.id.codeOrEmailField);
        setEmail = findViewById(R.id.setEmail);
        confirmCode = findViewById(R.id.confirmCode);

        setEmail.setOnClickListener(v -> {
            if (Pattern.compile("(.*)@([\\w\\-.]+)\\.(\\w+)")
                    .matcher(field.getText().toString())
                    .find()
            ) {
                globals.liveData.put("register_email", field.getText().toString());

                PDDEditor.putString("register_email", field.getText().toString());

                Hashtable<String, String> createCodeParams = new Hashtable<>();
                createCodeParams.put("email", field.getText().toString());

                executeApiMethodAsync(
                        "post",
                        "product",
                        "email",
                        "createCode",
                        createCodeParams,
                        new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                writeErrorInLog(e);
                                globals.showToastMessage(
                                        getString(R.string.error_request_failed),
                                        false
                                );
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String createCodeResponse = response.body().string();

                                String hash = new Gson().fromJson(createCodeResponse, SuccessResponse.class)
                                        .body
                                        .getAsString();

                                globals.liveData.put("register_email_hash", hash);

                                PDDEditor.putString("register_email_hash", hash);

                                PDDEditor.putInt("register_email_createCode_time", (int) (System.currentTimeMillis() / 1000L));

                                runOnUiThread(() -> {
                                    hint.setHint(R.string.emailFillCode);
                                    field.setHint(R.string.emailButtonHintCode);
                                    field.setText("");
                                    field.setInputType(InputType.TYPE_CLASS_TEXT);
                                    confirmCode.setVisibility(View.VISIBLE);
                                    setEmail.setVisibility(View.GONE);
                                });

                                confirmCode.setOnClickListener(view -> confirmCode(
                                        hash,
                                        false,
                                        () -> {
                                            Intent welcomeActivity = new Intent(emailActivity.this, welcomeActivity.class);
                                            welcomeActivity.putExtra("finalRegisterStep", true);
                                            welcomeActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivity(welcomeActivity);
                                            finish();
                                        }
                                ));
                            }
                        }
                );
            } else globals.showToastMessage(
                    getString(R.string.error_field_no_match_regex).replace(
                            "{field}", getString(R.string.welcomeLoginHint)
                    ).replace(
                            "{condition}", getString(R.string.regexHumanTranslationSEM)
                    ),
                    true
            );
        });

        confirmCode.setOnClickListener(v -> confirmCode(
                getIntent().getStringExtra("hash"),
                true,
                (Runnable) getIntent().getSerializableExtra("actionAfterConfirm")
        ));

        if (getIntent().getBooleanExtra("requestEmail", false)) {
            hint.setHint(R.string.emailFillEmail);
            field.setHint(R.string.emailButtonHint);
            field.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            confirmCode.setVisibility(View.GONE);
            setEmail.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PDDEditor.apply();
    }

    private void confirmCode(String hash, boolean needRemove, Runnable afterSuccess) {
        new Thread(() -> {
            try {
                Hashtable<String, String> confirmCodeParams = new Hashtable<>();
                confirmCodeParams.put("code", field.getText().toString());
                confirmCodeParams.put("hash", hash);
                confirmCodeParams.put("needRemove", needRemove ? "1" : "0");

                executeApiMethodSync(
                        "get",
                        "product",
                        "email",
                        "confirmCode",
                        confirmCodeParams
                );

                afterSuccess.run();
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            } catch (APIException APIEx) {
                globals.showToastMessage(
                        APIException.translate("email", APIEx.getMessage()),
                        false
                );
            }
        }).start();
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showToastMessage(getString(R.string.service_pressAgainToExit), false);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                doubleBackToExitPressedOnce = false,
                2000
        );
    }
}
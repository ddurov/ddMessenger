package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodAsync;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;
import static com.ddprojects.messager.service.globals.writeKeyPairToSP;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.serializedAction;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.fakeContext;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Pattern;

import okhttp3.Response;

public class preConfirmEmailActivity extends AppCompatActivity {

    EditText field;
    Button setEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_confirm_email);

        field = findViewById(R.id.emailField);
        setEmail = findViewById(R.id.setEmail);

        setEmail.setOnClickListener(view -> {
            if (Pattern.compile("(.*)@([\\w\\-.]+)\\.(\\w+)")
                    .matcher(field.getText().toString())
                    .find()
            ) {
                writeKeyPairToSP("email", field.getText().toString());

                Hashtable<String, String> createCodeParams = new Hashtable<>();
                createCodeParams.put("email", field.getText().toString());

                executeApiMethodAsync(
                        "post",
                        "product",
                        "email",
                        "createCode",
                        createCodeParams,
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
                                    showToastMessage(
                                            getString(R.string.error_request_failed),
                                            false
                                    );
                                }
                            }

                            @Override
                            public void onSuccess(Response response) throws IOException {
                                String createCodeResponse = response.body().string();

                                String hash = new Gson().fromJson(createCodeResponse, SuccessResponse.class)
                                        .getBody()
                                        .getAsString();

                                writeKeyPairToSP("email_hash", hash);

                                writeKeyPairToSP(
                                        "email_createCode_time",
                                        (int) (System.currentTimeMillis() / 1000L)
                                );

                                Intent emailActivity = new Intent(getApplicationContext(), confirmEmailActivity.class);
                                emailActivity.putExtra("hash", hash);
                                emailActivity.putExtra("needRemove", getIntent().getBooleanExtra("needRemove", true));
                                emailActivity.putExtra(
                                        "actionAfterConfirm",
                                        (serializedAction) () -> {
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
                                finish();
                            }
                        }
                );
            } else showToastMessage(
                    getString(R.string.error_field_no_match_regex).replace(
                            "{field}", getString(R.string.welcomeLoginHint)
                    ).replace(
                            "{condition}", getString(R.string.regexHumanTranslationSEM)
                    ),
                    true
            );
        });
    }
}
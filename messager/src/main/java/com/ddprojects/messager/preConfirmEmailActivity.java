package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;
import static com.ddprojects.messager.service.globals.writeKeyPairToSP;
import static com.ddprojects.messager.service.globals.writeKeyPairsToSP;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;

import java.util.Hashtable;
import java.util.regex.Pattern;

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

                executeApiMethod(
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
                                }
                            }

                            @Override
                            public void onSuccess(SuccessResponse response) {
                                String hash = response.getBody().getAsString();

                                writeKeyPairsToSP(new Object[][]{
                                        {"email_hash", hash},
                                        {"email_createCode_time", (int) (System.currentTimeMillis() / 1000L)}
                                });

                                startActivity(
                                        new Intent(
                                                preConfirmEmailActivity.this,
                                                confirmEmailActivity.class
                                        )
                                                .putExtra("hash", hash)
                                                .putExtras(getIntent())
                                );
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
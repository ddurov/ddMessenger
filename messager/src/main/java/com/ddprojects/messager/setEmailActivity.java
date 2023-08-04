package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodAsync;
import static com.ddprojects.messager.service.globals.PDDEditor;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.ddprojects.messager.service.SerializedAction;
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

public class setEmailActivity extends AppCompatActivity {

    EditText field;
    Button setEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_email);

        field = findViewById(R.id.emailField);
        setEmail = findViewById(R.id.setEmail);

        setEmail.setOnClickListener(view -> {
            if (Pattern.compile("(.*)@([\\w\\-.]+)\\.(\\w+)")
                    .matcher(field.getText().toString())
                    .find()
            ) {
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

                                PDDEditor.putString("register_email_hash", hash);

                                PDDEditor.putInt(
                                        "register_email_createCode_time",
                                        (int) (System.currentTimeMillis() / 1000L)
                                );

                                Intent emailActivity = new Intent(getApplicationContext(), confirmEmailActivity.class);
                                emailActivity.putExtra("hash", hash);
                                emailActivity.putExtra(
                                        "actionAfterConfirm",
                                        (SerializedAction) () -> {
                                            PDDEditor.putBoolean("register_email_confirmed", true);

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
            } else globals.showToastMessage(
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
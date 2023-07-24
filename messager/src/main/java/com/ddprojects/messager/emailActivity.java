package com.ddprojects.messager;

import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.globals;

import org.json.JSONException;
import org.json.JSONObject;

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
                Hashtable<String, String> createCodeParams = new Hashtable<>();
                createCodeParams.put("email", field.getText().toString());

                APIRequester.executeApiMethodAsync(
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
                                        getString(R.string.error_requestFailed),
                                        false
                                );
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String createCodeResponse = response.body().string();

                                try {
                                    String hash = new JSONObject(createCodeResponse)
                                            .getString("body");

                                    runOnUiThread(() -> {
                                        hint.setHint(R.string.emailFillCode);
                                        field.setHint(R.string.emailButtonHintCode);
                                        field.setText("");
                                        confirmCode.setVisibility(View.VISIBLE);
                                        setEmail.setVisibility(View.GONE);
                                    });

                                    confirmCode.setOnClickListener(view -> confirmCode(
                                            hash,
                                            false,
                                            (Runnable) getIntent().
                                                    getSerializableExtra("actionAfterConfirm")
                                    ));
                                } catch (JSONException JSONEx) {
                                    writeErrorInLog(
                                            JSONEx,
                                            "Response email/createCode: " + createCodeResponse
                                    );
                                    globals.showToastMessage(
                                            getString(R.string.error_responseReadingFailed),
                                            false
                                    );
                                }
                            }
                        }
                );
            }
        });

        confirmCode.setOnClickListener(view -> confirmCode(
                getIntent().getStringExtra("hash"),
                true,
                (Runnable) getIntent().getSerializableExtra("actionAfterConfirm")
        ));

        if (getIntent().getBooleanExtra("requestEmail", false)) {
            hint.setHint(R.string.emailFillEmail);
            field.setHint(R.string.emailButtonHint);
            confirmCode.setVisibility(View.GONE);
            setEmail.setVisibility(View.VISIBLE);
        }
    }

    private void confirmCode(String hash, boolean needRemove, Runnable afterSuccess) {
        new Thread(() -> {
            String confirmCodeResponse = null;
            try {
                Hashtable<String, String> confirmCodeParams = new Hashtable<>();
                confirmCodeParams.put("code", field.getText().toString());
                confirmCodeParams.put("hash", hash);
                confirmCodeParams.put("needRemove", needRemove ? "1" : "0");

                confirmCodeResponse = APIRequester.executeApiMethodSync(
                        "get",
                        "product",
                        "email",
                        "confirmCode",
                        confirmCodeParams
                );

                if (new JSONObject(confirmCodeResponse).getBoolean("body")) {
                    afterSuccess.run();
                }

            } catch (APIException APIEx) {
                globals.showToastMessage(
                        APIException.translate("email", APIEx.getMessage()),
                        false
                );
            } catch (JSONException JSONEx) {
                writeErrorInLog(JSONEx, "Response email/createCode: " + confirmCodeResponse);
                globals.showToastMessage(
                        getString(R.string.error_responseReadingFailed),
                        false
                );
            }
        }).start();
    }
}
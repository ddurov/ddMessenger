package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.globals.liveData;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.fakeContext;

import java.util.Hashtable;

public class confirmEmailActivity extends AppCompatActivity {
    EditText field;
    Button confirmCode;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);

        field = findViewById(R.id.codeField);
        confirmCode = findViewById(R.id.confirmCode);

        confirmCode.setOnClickListener(v -> confirmCode(
                getIntent().getStringExtra("hash"),
                getIntent().getBooleanExtra("needRemove", true),
                (Runnable) getIntent().getSerializableExtra("actionAfterConfirm")
        ));
    }

    private void confirmCode(String hash, boolean needRemove, Runnable actionAfterConfirm) {
        if (field.getText().toString().isEmpty()) {
            showToastMessage(getString(R.string.error_field_are_empty)
                    .replace("{field}", getString(R.string.confirmEmailFieldHint)), true);
            return;
        }

        Hashtable<String, String> confirmCodeParams = new Hashtable<>();
        confirmCodeParams.put("code", field.getText().toString());
        confirmCodeParams.put("hash", hash);
        confirmCodeParams.put("needRemove", needRemove ? "1" : "0");

        executeApiMethod(
                "get",
                "product",
                "email",
                "confirmCode",
                confirmCodeParams,
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
                        if (!needRemove) liveData.put("email_code", field.getText().toString());

                        if (actionAfterConfirm != null) actionAfterConfirm.run();

                        startActivity(getIntent().getParcelableExtra("intent"));
                        finish();
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;

            showToastMessage(getString(R.string.service_pressAgainToExit), false);

            fakeContext.getMainThreadHandler().postDelayed(
                    () -> doubleBackToExitPressedOnce = false,
                    2000
            );
        } else super.onBackPressed();
    }
}
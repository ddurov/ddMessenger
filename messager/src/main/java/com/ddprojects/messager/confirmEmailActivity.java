package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.liveData;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.fakeContext;

import java.io.IOException;
import java.util.Hashtable;

public class confirmEmailActivity extends AppCompatActivity {

    EditText field;
    Button confirmCode;

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

                if (!needRemove) liveData.put("email_code", field.getText().toString());

                actionAfterConfirm.run();

                finish();
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            } catch (APIException APIEx) {
                showToastMessage(
                        APIException.translate(APIEx.getMessage()),
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
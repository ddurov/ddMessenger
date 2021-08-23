package com.eviger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class recoveryAccount extends AppCompatActivity {

    Button btnToRecovery;
    EditText login, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recovery_account);

        login = findViewById(R.id.login_recoveryAccount);
        email = findViewById(R.id.email_recoveryAccount);

        btnToRecovery = findViewById(R.id.btnToRecovery);

        btnToRecovery.setOnClickListener(v -> {

            if (!email.getText().toString().isEmpty() && Pattern.compile("^([a-z0-9_-]+\\.)*[a-z0-9_-]+@[a-z0-9_-]+(\\.[a-z0-9_-]+)*\\.[a-z]{2,6}$").matcher(email.getText().toString()).find()) {

                try {

                    JSONObject JSON = new JSONObject();
                    JSON.put("login", login.getText().toString());
                    JSON.put("email", email.getText().toString());

                    JSONObject recoveryJsonObject = new JSONObject(executeApiMethodPost("user", "restorePassword", JSON));

                    if (recoveryJsonObject.getJSONObject("response").getString("status").equals("confirm your email")) {

                        Intent in = new Intent(recoveryAccount.this, emailConfirm.class);
                        in.putExtra("type", "recoveryAccount")
                        .putExtra("email", email.getText().toString())
                        .putExtra("hashCode", recoveryJsonObject.getJSONObject("response").getString("hash"));
                        startActivity(in);
                        finish();

                    }

                } catch (Exception ex) {
                    runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                }

            } else {

                Toast.makeText(getApplicationContext(), "Почта пустая либо введена некорректно", Toast.LENGTH_LONG).show();

            }

        });

    }

}
package com.eviger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getHashCodeEmail;
import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;

public class changeName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_name);

        if (hasConnection(getApplicationContext())) {

            Button confirmChangeName;

            EditText newName = findViewById(R.id.newNameProfile);

            confirmChangeName = findViewById(R.id.confirmChangeNamebutton);

            confirmChangeName.setOnClickListener(v -> {

                if (!newName.getText().toString().trim().isEmpty() && newName.getText().toString().trim().length() <= 20) {

                    if (Pattern.compile("[a-zA-Z0-9]").matcher(newName.getText().toString().trim()).find()) {

                        try {

                            Intent in = new Intent(changeName.this, emailConfirm.class);
                            in.putExtra("type", "changeName");
                            in.putExtra("newName", newName.getText().toString().trim());
                            JSONObject getEmailMyAccount = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));
                            in.putExtra("email", getEmailMyAccount.getJSONObject("response").getString("email"));
                            String answerEmail = getHashCodeEmail(getEmailMyAccount.getJSONObject("response").getString("email"));

                            if (!Pattern.compile("ERROR: (.*)").matcher(answerEmail).find()) {
                                in.putExtra("hashCode", answerEmail);
                            } else {
                                Toast.makeText(getApplicationContext(), answerEmail, Toast.LENGTH_LONG).show();
                            }

                            startActivity(in);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        Toast.makeText(getApplicationContext(), "Недопустимый юзернейм", Toast.LENGTH_LONG).show();

                    }

                } else {

                    Toast.makeText(getApplicationContext(), "Имя пользователя пустое либо больше 20-и символов", Toast.LENGTH_LONG).show();

                }

            });

        }

    }
    protected void onPause() {
        super.onPause();

        if (hasConnection(getApplicationContext())) {

            if (getToken() != null) {

                setOffline();

            }

        }

    }
    protected void onResume() {
        super.onResume();

        if (hasConnection(getApplicationContext())) {

            if (getToken() != null) {

                setOnline();

            }

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }
    protected void onDestroy() {
        super.onDestroy();

        if (hasConnection(getApplicationContext())) {

            if (getToken() != null) {

                setOffline();

            }

        }

    }

}
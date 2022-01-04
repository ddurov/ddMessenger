package com.eviger;

import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;
import static com.eviger.z_globals.writeErrorInLog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class searchUsers extends AppCompatActivity {

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_users);

        ImageButton toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(searchUsers.this, profilePage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        ImageButton toMessages = findViewById(R.id.toMessages);
        toMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(searchUsers.this, messagesPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        ImageButton toSettings = findViewById(R.id.toSettings);
        toSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(searchUsers.this, settingsPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        LinearLayout containerOfUsers = findViewById(R.id.containersOfUsers);

        EditText queryTextView = findViewById(R.id.query_searchUsers);
        queryTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                containerOfUsers.removeAllViews();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {

                if (!hasConnection(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                    return;
                }

                try {

                    if (!s.toString().equals("")) {

                        JSONObject jsonUsersBySearch = new JSONObject(executeApiMethodGet("users", "search", new String[][]{{"query", s.toString()}}));
                        JSONArray usersArray = jsonUsersBySearch.getJSONArray("response");

                        if (usersArray.length() > 0) {

                            for (int i = 0; i < usersArray.length(); i++) {

                                View toDialog_view = getLayoutInflater().inflate(R.layout.z_dialog_from_search_button, null, true);

                                TextView nameProfile = toDialog_view.findViewById(R.id.nameButtonDialog);
                                Button toDialog = toDialog_view.findViewById(R.id.toDialog);

                                nameProfile.setText(usersArray.getJSONObject(i).getString("username"));

                                int tempIteration = i;
                                toDialog.setOnClickListener(v -> {
                                    try {
                                        Intent in = new Intent(searchUsers.this, messagesChat.class);
                                        in.putExtra("eid", usersArray.getJSONObject(tempIteration).getInt("eid"));
                                        startActivity(in);
                                    } catch (Exception ex) {
                                        runOnUiThread(() -> writeErrorInLog(ex));
                                    }
                                });

                                containerOfUsers.addView(toDialog_view);

                            }

                        } else {

                            containerOfUsers.removeAllViews();

                        }

                    }

                } catch (Exception ex) {
                    runOnUiThread(() -> writeErrorInLog(ex));
                }

            }
        });

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
            setOffline();
            sendingOnline = false;
            activatedMethodUserLeaveHint = true;
        }
    }

    protected void onResume() {
        super.onResume();
        if (activatedMethodUserLeaveHint) {
            setOnline();
            sendingOnline = true;
            inAnotherActivity = false;
            activatedMethodUserLeaveHint = false;
        }
    }

}
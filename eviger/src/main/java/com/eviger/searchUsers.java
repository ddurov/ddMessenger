package com.eviger;

import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class searchUsers extends AppCompatActivity {

    ImageButton btnMessages, btnProfile, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_users);

        btnProfile = findViewById(R.id.toProfile);
        btnProfile.setOnClickListener(v -> {
            Intent in = new Intent(searchUsers.this, profilePage.class);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.toMessages);
        btnMessages.setOnClickListener(v -> {
            Intent in = new Intent(searchUsers.this, messagesPage.class);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.toSettings);
        btnSettings.setOnClickListener(v -> {
            Intent in = new Intent(searchUsers.this, settingsAccount.class);
            startActivity(in);
        });

        if (hasConnection(getApplicationContext())) {

            EditText queryTextView = findViewById(R.id.query_searchUsers);
            queryTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    LinearLayout linLayout = findViewById(R.id.containersOfUsers);
                    linLayout.removeAllViews();
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void afterTextChanged(Editable s) {

                    String query = s.toString();

                    try {

                        if (!query.equals("")) {

                            JSONObject jsonUsersBySearch = new JSONObject(executeApiMethodGet("users", "search", new String[][]{{"query", query}}));
                            JSONArray usersArray = jsonUsersBySearch.getJSONArray("response");

                            if (usersArray.length() > 0) {

                                for (int i = 0; i < usersArray.length(); i++) {

                                    int eidProfile = usersArray.getJSONObject(i).getInt("eid");
                                    String nameUsers = usersArray.getJSONObject(i).getString("username");

                                    LayoutInflater ltInflater = getLayoutInflater();
                                    LinearLayout linLayout = findViewById(R.id.containersOfUsers);
                                    View buttonToProfileView = ltInflater.inflate(R.layout.z_dialog_from_search_button, null, true);

                                    TextView nameProfile = buttonToProfileView.findViewById(R.id.nameButtonDialog);
                                    //Button buttonToProfile = buttonToProfileView.findViewById(R.id.toDialog);

                                    nameProfile.setText(nameUsers);

                                    linLayout.addView(buttonToProfileView);

                                    /*buttonToProfile.setOnClickListener(v -> {
                                        Intent intent = new Intent(searchUsers.this, messagesChat.class);
                                        intent.putExtra("eid", eidProfile);
                                        startActivity(intent);
                                    });*/

                                }

                            } else {

                                LinearLayout linLayout = findViewById(R.id.containersOfUsers);
                                linLayout.removeAllViews();

                            }

                        }

                    } catch (Throwable ex) {
                        runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), searchUsers.this));
                    }

                }
            });

        } else {

            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        }

    }

}
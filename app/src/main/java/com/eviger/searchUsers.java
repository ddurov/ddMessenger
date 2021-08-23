package com.eviger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getAccount;
import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class searchUsers extends AppCompatActivity {

    ImageButton btnMessages, btnProfile, btnSettings;
    boolean hasNextActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_users);

        hasNextActivity = false;

        btnProfile = findViewById(R.id.buttonGoToProfile);
        btnProfile.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(searchUsers.this, profilePage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnMessages = findViewById(R.id.buttonGoToMessages);
        btnMessages.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(searchUsers.this, messagesPage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.buttonGoToSettings);
        btnSettings.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(searchUsers.this, settingsAccount.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        if (hasConnection(this)) {

            EditText queryTextView = findViewById(R.id.queryOfSearchUser);
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

                        if (query != null) {

                            JSONObject jsonUsersBySearch = new JSONObject(executeApiMethodGet("users", "search", new String[][]{{"query", query}}));
                            JSONArray usersArray = jsonUsersBySearch.getJSONArray("response");

                            int lenghtUsersArray = usersArray.length();

                            if (lenghtUsersArray > 0) {

                                for (int i = 0; i < lenghtUsersArray; i++) {

                                    int eidMyProfile = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}})).getJSONObject("response").getInt("eid");
                                    int eidAnotherProfile = usersArray.getJSONObject(i).getInt("eid");
                                    String nameUsers = usersArray.getJSONObject(i).getString("username");

                                    LayoutInflater ltInflater = getLayoutInflater();
                                    LinearLayout linLayout = findViewById(R.id.containersOfUsers);
                                    View buttonToProfileView = ltInflater.inflate(R.layout.z_page_button, null, true);

                                    TextView nameProfile = buttonToProfileView.findViewById(R.id.namePage);
                                    TextView onlineProfile = buttonToProfileView.findViewById(R.id.onlineProfile);
                                    Button buttonToProfile = buttonToProfileView.findViewById(R.id.buttonToPage);

                                    nameProfile.setText(nameUsers);

                                    if (usersArray.getJSONObject(i).getInt("online") == 1) {

                                        onlineProfile.setText("В сети");

                                    } else {

                                        onlineProfile.setText("Не заходил с " + new SimpleDateFormat("d MMM yyyy 'года' HH:mm", Locale.getDefault()).format(new Date(usersArray.getJSONObject(i).getInt("lastSeen") * 1000L)));

                                    }

                                    linLayout.addView(buttonToProfileView);

                                    buttonToProfile.setOnClickListener(v -> {
                                        Intent intent;
                                        hasNextActivity = true;
                                        if (eidMyProfile != eidAnotherProfile) {
                                            intent = new Intent(searchUsers.this, profilePageAny.class);
                                            intent.putExtra("id", eidAnotherProfile);
                                        } else {
                                            intent = new Intent(searchUsers.this, profilePage.class);
                                        }
                                        startActivity(intent);
                                    });

                                }

                            } else {

                                LinearLayout linLayout = findViewById(R.id.containersOfUsers);
                                linLayout.removeAllViews();

                            }

                        }

                    } catch (Throwable ex) {
                        runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), searchUsers.this));
                    }

                }
            });

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }
    public void onBackPressed() {

        super.onBackPressed();
        if (!getIntent().getBooleanExtra("hasLastActivity", false)) {
            setOffline();
            finish();
        }

    }
    public void onUserLeaveHint() {

        super.onUserLeaveHint();
        if (!hasNextActivity) {

            if (hasConnection(this)) {

                if (getToken() != null) {

                    setOffline();

                }

            }

        }

    }
    protected void onResume() {

        super.onResume();
        if (hasConnection(this)) {

            if (getToken() != null) {

                setOnline();

            }

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }

}
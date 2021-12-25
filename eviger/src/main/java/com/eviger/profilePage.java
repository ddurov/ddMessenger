package com.eviger;

import static com.eviger.z_globals.dialogs;
import static com.eviger.z_globals.executeLongPollMethod;
import static com.eviger.z_globals.getProfileById;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.moveOrAddDialogToTop;
import static com.eviger.z_globals.myProfile;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class profilePage extends AppCompatActivity {

    ImageButton toMessages, toSettings;
    Button searchUsers;
    TextView nameProfile;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        searchUsers = findViewById(R.id.searchUsers);
        nameProfile = findViewById(R.id.welcomeUser);

        searchUsers.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, searchUsers.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        toMessages = findViewById(R.id.toMessages);
        toMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, messagesPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        toSettings = findViewById(R.id.toSettings);
        toSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, settingsAccount.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        try {

            nameProfile.setText("Текущее имя профиля: " + myProfile.getString("username"));

            new Thread(() -> {
                while (true) {
                    try {
                        sendingOnline = hasConnection(getApplicationContext()) && sendingOnline;

                        JSONObject longPollResponse = new JSONObject(executeLongPollMethod("getUpdates", new String[][]{
                                {"waitTime", "20"},
                                {"flags", "peerIdInfo"}
                        }));

                        for (int i = 0; i < longPollResponse.getJSONArray("response").length(); i++) {

                            if (longPollResponse.getJSONArray("response").getJSONObject(i).getString("eventType").equals("newMessage")) {

                                moveOrAddDialogToTop(dialogs,
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peer_id"),
                                        new z_dialog(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peer_id"),
                                                (String) getProfileById(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peer_id"))[1],
                                                new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new java.util.Date(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("date") * 1000L)),
                                                longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getString("message").replaceAll("\\n", "")));

                                runOnUiThread(() -> messagesPage.dialogsAdapter.updateData());

                            }

                        }

                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        sendingOnline = false;
                        runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                        break;
                    }
                }
            }).start();

            new Thread(() -> {
                while (sendingOnline) {
                    try {
                        setOnline();
                        Thread.sleep(300000);
                    } catch (Exception ex) {
                        sendingOnline = false;
                        runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                    }
                }
            }).start();

        } catch (Throwable ex) {
            runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
        }

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

    public void onBackPressed() {
        super.onBackPressed();
        sendingOnline = false;
        setOffline();
    }

}

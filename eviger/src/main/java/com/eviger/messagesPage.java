package com.eviger;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class messagesPage extends AppCompatActivity {

    ImageButton btnProfile, btnSettings;
    Button btnInDialogs;
    TextView dialogsText;
    ArrayList<View> viewsDialogs = new ArrayList<>();

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_page);

        dialogsText = findViewById(R.id.contentCap);

        btnProfile = findViewById(R.id.buttonGoToProfile);
        btnProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(messagesPage.this, profilePage.class);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.buttonGoToSettings);
        btnSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(messagesPage.this, settingsAccount.class);
            startActivity(in);
        });

        Thread updateDialogsThread = new Thread(() -> {

            while (true) {
                try {
                    String status = updateDialogs();
                    if (status.equals("updated") || status.equals("not updated, but cached")) {
                        break;
                    } else if (status.equals("not updated, not cached")) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show());
                    }
                    Thread.sleep(2000);
                } catch (Throwable ex) {
                    runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                }
            }

        });
        updateDialogsThread.start();

    }

    private String updateDialogs() {

        LinearLayout container = findViewById(R.id.containerOfDialogs);

        if (hasConnection(getApplicationContext())) {

            try {

                JSONArray dialogs = new JSONObject(executeApiMethodGet("messages", "getDialogs", new String[][]{{}})).getJSONArray("response");

                for (int i = 0; i < dialogs.length(); i++) {

                    String text = dialogs.getJSONObject(i).getString("message").replaceAll("\\n", "");
                    int time = dialogs.getJSONObject(i).getInt("date");
                    int myId = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}})).getJSONObject("response").getInt("eid");
                    int id = dialogs.getJSONObject(i).getInt("creator_dialog_id") == myId ? dialogs.getJSONObject(i).getInt("peer_id") : dialogs.getJSONObject(i).getInt("creator_dialog_id");
                    int senderId = dialogs.getJSONObject(i).getInt("last_message_sender");

                    JSONObject getAccountCreatorDialog = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{"id", String.valueOf(id)}}));

                    LayoutInflater inflater = this.getLayoutInflater();
                    View buttonToDialog = inflater.inflate(R.layout.z_dialog_button, container, false);

                    String username = getAccountCreatorDialog.getJSONObject("response").getString("username");

                    btnInDialogs = buttonToDialog.findViewById(R.id.toDialog);

                    btnInDialogs.setOnClickListener(v -> {

                        inAnotherActivity = true;
                        Intent intent = new Intent(this, messagesChat.class);
                        intent.putExtra("eid", id);
                        startActivity(intent);

                    });

                    TextView message = buttonToDialog.findViewById(R.id.messageButtonDialog);
                    TextView name = buttonToDialog.findViewById(R.id.nameButtonDialog);
                    TextView timeLastMessage = buttonToDialog.findViewById(R.id.timeButtonDialog);

                    runOnUiThread(() -> message.setText(myId == senderId ? "Я: " + text : text));
                    runOnUiThread(() -> name.setText(username));
                    runOnUiThread(() -> timeLastMessage.setText(new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new java.util.Date(time * 1000L))));

                    viewsDialogs.add(buttonToDialog);

                }

            } catch (Throwable ex) {
                runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
            }

        } else if (viewsDialogs != null && !hasConnection(getApplicationContext())) {

            runOnUiThread(() -> findViewById(R.id.scrollDialogs).setVisibility(View.VISIBLE));
            runOnUiThread(() -> findViewById(R.id.dialogsNulled).setVisibility(View.INVISIBLE));

            for (int i = 0; i < viewsDialogs.size(); i++) {

                int I = i;
                runOnUiThread(() -> container.addView(viewsDialogs.get(I)));

            }

            return "not updated, but cached";

        } else {

            return "not updated, not cached";

        }

        if (viewsDialogs != null) {
            runOnUiThread(() -> findViewById(R.id.scrollDialogs).setVisibility(View.VISIBLE));
            runOnUiThread(() -> findViewById(R.id.dialogsNulled).setVisibility(View.INVISIBLE));
        } else {
            runOnUiThread(() -> findViewById(R.id.scrollDialogs).setVisibility(View.INVISIBLE));
            runOnUiThread(() -> findViewById(R.id.dialogsNulled).setVisibility(View.VISIBLE));
        }

        for (int i = 0; i < viewsDialogs.size(); i++) {

            int I = i;
            runOnUiThread(() -> container.addView(viewsDialogs.get(I)));

        }

        return "updated";

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
            setOffline();
            profilePage.onlineSending = false;
            activatedMethodUserLeaveHint = true;
        }
    }
    protected void onResume() {
        super.onResume();
        if (activatedMethodUserLeaveHint) {
            setOnline();
            profilePage.onlineSending = true;
            inAnotherActivity = false;
            activatedMethodUserLeaveHint = false;
        }
    }

}

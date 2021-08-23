package com.eviger;

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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class messagesPage extends AppCompatActivity {

    ImageButton btnProfile, btnSettings;
    Button btnInDialogs;
    TextView dialogsText;
    boolean hasNextActivity;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_page);

        dialogsText = findViewById(R.id.profile_Title);
        hasNextActivity = false;

        btnProfile = findViewById(R.id.buttonGoToProfile);
        btnProfile.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(messagesPage.this, profilePage.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.buttonGoToSettings);
        btnSettings.setOnClickListener(v -> {
            hasNextActivity = true;
            Intent in = new Intent(messagesPage.this, settingsAccount.class);
            in.putExtra("hasLastActivity", true);
            startActivity(in);
        });

        Thread run = new Thread(() -> {

            while(true) {
                try {
                    if (hasConnection(this)) {
                        runOnUiThread(() -> dialogsText.setText("Обновляю содержимое..."));
                        parseDialogs();
                        runOnUiThread(() -> dialogsText.setText("Диалоги"));
                        break;
                    } else {
                        runOnUiThread(() -> dialogsText.setText("Подключитесь к интернету!"));
                    }
                    Thread.sleep(2000);
                } catch (Throwable ex) {
                    runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                }
            }

        });
        run.start();

    }
    private void parseDialogs() {

        try {

            JSONArray dialogs = new JSONObject(executeApiMethodGet("messages", "getDialogs", new String[][]{{}})).getJSONArray("response");
            int lengthDialogs = dialogs.length();

            if (lengthDialogs > 0) {

                runOnUiThread(() -> findViewById(R.id.scrollDialogs).setVisibility(View.VISIBLE));
                runOnUiThread(() -> findViewById(R.id.dialogsNulled).setVisibility(View.INVISIBLE));

                for (int i = 0; i < lengthDialogs; i++) {

                    String text = dialogs.getJSONObject(i).getString("message").replaceAll("\\n", "");
                    int time = dialogs.getJSONObject(i).getInt("date");
                    int myId = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}})).getJSONObject("response").getInt("eid");
                    int id = dialogs.getJSONObject(i).getInt("creator_dialog_id") == myId ? dialogs.getJSONObject(i).getInt("peer_id") : dialogs.getJSONObject(i).getInt("creator_dialog_id");
                    int senderId = dialogs.getJSONObject(i).getInt("last_message_sender");

                    JSONObject getAccountCreatorDialog = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{"id", String.valueOf(id)}}));

                    LinearLayout container = findViewById(R.id.containerOfDialogs);

                    LayoutInflater layInfl = this.getLayoutInflater();
                    View buttonToDialog = layInfl.inflate(R.layout.z_dialog_button, null, false);

                    String username = getAccountCreatorDialog.getJSONObject("response").getString("username");

                    btnInDialogs = buttonToDialog.findViewById(R.id.btnInDialogs);

                    btnInDialogs.setOnClickListener(v -> {

                        hasNextActivity = true;
                        Intent intent = new Intent(this, messagesChat.class);
                        intent.putExtra("eid", id);
                        intent.putExtra("hasLastActivity", true);
                        startActivity(intent);

                    });

                    TextView message = buttonToDialog.findViewById(R.id.messageButtonDialog);
                    TextView name = buttonToDialog.findViewById(R.id.fullName);
                    TextView timeLastMessage = buttonToDialog.findViewById(R.id.timeLastMessage);

                    runOnUiThread(() -> message.setText(myId == senderId ? "Я: "+text : text));
                    runOnUiThread(() -> name.setText(username));

                    Date messageTime = new java.util.Date(time * 1000L);
                    runOnUiThread(() -> timeLastMessage.setText(new SimpleDateFormat("d MMM yyyy 'года' HH:mm", Locale.getDefault()).format(messageTime)));

                    runOnUiThread(() -> container.addView(buttonToDialog));

                }

            } else {

                runOnUiThread(() -> findViewById(R.id.scrollDialogs).setVisibility(View.INVISIBLE));
                runOnUiThread(() -> findViewById(R.id.dialogsNulled).setVisibility(View.VISIBLE));

            }

        } catch (Throwable ex) {
            runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
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

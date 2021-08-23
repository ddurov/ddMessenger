package com.eviger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.getAccount;
import static com.eviger.globals.getToken;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class messagesChat extends Activity {

    RecyclerView boxMessages;
    ImageButton sendMessage, backToDialogs;
    EditText textMessage;
    TextView username, online;
    messagesController mControl;
    boolean secure_forThread;
    int eid;

    private void sendMsg(String message, Integer peer_id) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("text", message.replaceAll("\n","\\\\n"));
        json.put("to_id", peer_id);

        executeApiMethodPost("messages", "send", json);

    }

    private void history(Integer userId) {

        mControl = new messagesController();

        mControl
                .setIncomingLayout(R.layout.z_message_in)
                .setOutgoingLayout(R.layout.z_message_out)
                .setMessageTextId(R.id.textMessage)
                .setMessageTimeId(R.id.timeMessage)
                .appendTo(boxMessages, this);

        try {

            JSONArray history = new JSONObject(executeApiMethodGet("messages", "getHistory", new String[][]{{"id", String.valueOf(userId)}})).getJSONArray("response");

            int hl = history.length();

            if (hl > 0) {

                for (int i = 0; i < hl; i++) {

                    boolean type = history.getJSONObject(i).getBoolean("out");
                    String message = history.getJSONObject(i).getString("message").replaceAll("\\\\n", "\n");
                    int date = history.getJSONObject(i).getInt("date");

                    mControl.addMessage(new messagesController.Message(
                            message,
                            type,
                            date
                    ));

                }

            }

        } catch (Throwable ex) {
            runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_chat);

        eid = getIntent().getIntExtra("eid", -1);
        secure_forThread = true;
        boxMessages = findViewById(R.id.boxMessages);
        sendMessage = findViewById(R.id.sendMessage);
        textMessage = findViewById(R.id.textMessageInput);
        username = findViewById(R.id.NameUser);
        online = findViewById(R.id.onlineOfUser);
        backToDialogs = findViewById(R.id.backToDialogs);
        mControl = new messagesController();

        mControl.setIncomingLayout(R.layout.z_message_in)
                .setOutgoingLayout(R.layout.z_message_out)
                .setMessageTextId(R.id.textMessage)
                .setMessageTimeId(R.id.timeMessage)
                .appendTo(boxMessages, this);

        Thread run = new Thread(() -> {

            while (secure_forThread) {

                try {

                    if (globals.hasConnection(getApplicationContext())) {

                        runOnUiThread(() -> username.setText(getAccount(eid)[1].toString()));

                        if (getAccount(eid)[0].equals(true)) {

                            runOnUiThread(() -> online.setText("В сети"));

                        } else {

                            int lastSeen = (int) getAccount(eid)[2];
                            String date = new SimpleDateFormat("d MMM yyyy 'года' HH:mm", Locale.getDefault()).format(new Date(lastSeen * 1000L));
                            runOnUiThread(() -> online.setText("Не заходил с " + date));

                        }

                    } else {

                        online.setText("Подключитесь к интернету!");

                    }
                    Thread.sleep(300000);

                } catch (Throwable ex) {
                    runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                }

            }

        });
        run.start();

        backToDialogs.setOnClickListener(v -> {
            Intent in = new Intent(this, messagesPage.class);
            startActivity(in);
            secure_forThread = false;
        });

        if (hasConnection(this)) {

            history(eid);

            sendMessage.setOnClickListener(v -> {
                String message = textMessage.getText().toString().trim();
                if (!(message.equals(""))) {
                    List<String> strings = new ArrayList<>();
                    int index = 0;
                    int fe = 0;
                    while (index < message.length()) {
                        try {
                            strings.add(message.substring(index, Math.min(index + 1024, message.length())));
                            index += 1024;
                            if (!strings.get(fe).trim().equals("")) {
                                mControl.addMessage(new messagesController.Message(
                                        strings.get(fe),
                                        true,
                                        System.currentTimeMillis() / 1000L
                                ));
                                sendMsg(strings.get(fe).trim(), eid);
                                fe += 1;
                            }
                        } catch (Throwable ex) {
                            runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                        }
                    }
                }
                textMessage.setText("");
            });

        } else {

            Toast.makeText(this, "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }
    public void onBackPressed() {

        super.onBackPressed();
        if (!getIntent().getBooleanExtra("hasLastActivity", false)) {
            setOffline();
            finish();
        }
        Intent in = new Intent(this, messagesPage.class);
        startActivity(in);
        secure_forThread = false;

    }
    public void onUserLeaveHint() {

        super.onUserLeaveHint();

        if (!getIntent().getBooleanExtra("hasLastActivity", true)) {

            if (hasConnection(this)) {

                if (getToken() != null) {

                    setOffline();

                }

            }
            secure_forThread = false;

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
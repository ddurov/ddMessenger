package com.eviger;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.executeApiMethodPost;
import static com.eviger.globals.getAccount;
import static com.eviger.globals.hasConnection;
import static com.eviger.globals.setOffline;
import static com.eviger.globals.setOnline;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class messagesChat extends Activity {

    RecyclerView boxMessages;
    ImageButton sendMessage, backToDialogs;
    EditText textMessage;
    TextView nameUser, onlineStatus;
    messagesController mControl;
    int eid;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    private void sendMsg(String message, Integer peer_id) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("text", message.replaceAll("\n", "\\\\n"));
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
        boxMessages = findViewById(R.id.boxMessages);
        sendMessage = findViewById(R.id.sendMessage);
        textMessage = findViewById(R.id.textMessageInput);
        nameUser = findViewById(R.id.nameUser);
        onlineStatus = findViewById(R.id.onlineStatusUser);
        backToDialogs = findViewById(R.id.backToDialogs);
        mControl = new messagesController();

        mControl.setIncomingLayout(R.layout.z_message_in)
                .setOutgoingLayout(R.layout.z_message_out)
                .setMessageTextId(R.id.textMessage)
                .setMessageTimeId(R.id.timeMessage)
                .appendTo(boxMessages, this);

        backToDialogs.setOnClickListener(v -> {
            Intent in = new Intent(this, messagesPage.class);
            startActivity(in);
        });

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        nameUser.setText((CharSequence) getAccount(eid)[1]);

        switch ((Integer) getAccount(eid)[2]) {
            case 1: {
                onlineStatus.setText("заходил недавно");
                break;
            }
            case 2: {
                onlineStatus.setText("заходил сегодня");
                break;
            }
            case 3: {
                onlineStatus.setText("заходил на неделе");
                break;
            }
            case 4: {
                onlineStatus.setText("заходил в этом месяце");
                break;
            }
            case 5: {
                onlineStatus.setText("давно не заходил");
                break;
            }
        }

        history(eid);

        sendMessage.setOnClickListener(v -> {
            if (!hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                return;
            }
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
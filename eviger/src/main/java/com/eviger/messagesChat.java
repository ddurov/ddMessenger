package com.eviger;

import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.executeApiMethodPost;
import static com.eviger.z_globals.getProfileById;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;

import android.app.Activity;
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
    z_messageController mControl = new z_messageController().setIncomingLayout(R.layout.z_message_in)
            .setOutgoingLayout(R.layout.z_message_out)
            .setMessageTextId(R.id.textMessage)
            .setMessageTimeId(R.id.timeMessage);

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_chat);

        int eid = getIntent().getIntExtra("eid", -1);
        boxMessages = findViewById(R.id.boxMessages);
        sendMessage = findViewById(R.id.sendMessage);
        textMessage = findViewById(R.id.textMessageInput);
        nameUser = findViewById(R.id.nameUser);
        onlineStatus = findViewById(R.id.onlineStatusUser);
        backToDialogs = findViewById(R.id.backToDialogs);

        mControl.appendTo(boxMessages, this);

        backToDialogs.setOnClickListener(v -> finish());

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        nameUser.setText((CharSequence) getProfileById(eid)[1]);

        switch ((Integer) getProfileById(eid)[2]) {
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
                            mControl.addMessage(new z_messageController.Message(
                                    strings.get(fe),
                                    true,
                                    System.currentTimeMillis() / 1000L
                            ));
                            sendMsg(strings.get(fe).trim(), eid);
                            fe += 1;
                        }
                    } catch (Throwable ex) {
                        runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
                    }
                }
            }
            textMessage.setText("");
        });

    }

    private void sendMsg(String message, Integer peer_id) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("text", message.replaceAll("\n", "\\\\n"));
        json.put("to_id", peer_id);

        executeApiMethodPost("messages", "send", json);

    }

    private void history(Integer userId) {

        mControl.appendTo(boxMessages, this);

        try {

            JSONArray history = new JSONObject(executeApiMethodGet("messages", "getHistory", new String[][]{{"id", String.valueOf(userId)}})).getJSONArray("response");

            if (history.length() > 0) {

                for (int i = 0; i < history.length(); i++) {

                    boolean type = history.getJSONObject(i).getBoolean("out");
                    String message = history.getJSONObject(i).getString("message").replaceAll("\\n", "\n");
                    int date = history.getJSONObject(i).getInt("date");

                    mControl.addMessage(new z_messageController.Message(
                            message,
                            type,
                            date
                    ));

                }

            }

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

}
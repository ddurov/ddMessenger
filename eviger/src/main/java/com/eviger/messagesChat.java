package com.eviger;

import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.executeApiMethodPost;
import static com.eviger.z_globals.getProfileById;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.insertMessageByPeerId;
import static com.eviger.z_globals.log;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;
import static com.eviger.z_globals.writeErrorInLog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class messagesChat extends Activity {

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_chat);

        int eid = getIntent().getIntExtra("eid", -1);
        ArrayList<z_message> listMessages = new ArrayList<>();
        ImageButton returnToPageView = findViewById(R.id.return_messagesChat);
        TextView userView = findViewById(R.id.user_messagesChat);
        TextView statusView = findViewById(R.id.status_messagesChat);
        RecyclerView messagesView = findViewById(R.id.messages_messagesChat);
        EditText messageView = findViewById(R.id.message_messagesChat);
        ImageButton sendView = findViewById(R.id.send_messagesChat);

        returnToPageView.setOnClickListener(v -> finish());

        userView.setText((CharSequence) getProfileById(eid)[1]);

        switch ((Integer) getProfileById(eid)[2]) {
            case 1: {
                statusView.setText("заходил(а) недавно");
                break;
            }
            case 2: {
                statusView.setText("заходил(а) сегодня");
                break;
            }
            case 3: {
                statusView.setText("заходил(а) на неделе");
                break;
            }
            case 4: {
                statusView.setText("заходил(а) в этом месяце");
                break;
            }
            case 5: {
                statusView.setText("давно не заходил(а)");
                break;
            }
        }

        try {

            JSONArray responseGetHistory = new JSONObject(executeApiMethodGet("messages", "getHistory", new String[][]{{"id", String.valueOf(eid)}})).getJSONArray("response");

            for (int i = 0; i < responseGetHistory.length(); i++) {
                boolean isOut_historyMessages = responseGetHistory.getJSONObject(i).getBoolean("out");
                int peerId_historyMessages = responseGetHistory.getJSONObject(i).getInt("peerId");
                int id_historyMessages = responseGetHistory.getJSONObject(i).getInt("messageId");
                String message_historyMessages = responseGetHistory.getJSONObject(i).getString("message").replaceAll("\\n", "\n");
                int date_historyMessages = responseGetHistory.getJSONObject(i).getInt("messageDate");

                insertMessageByPeerId(listMessages,
                        new z_message(
                            id_historyMessages,
                            peerId_historyMessages,
                            date_historyMessages,
                            message_historyMessages,
                            isOut_historyMessages
                        )
                );
            }

        } catch (Exception ex) {
            runOnUiThread(() -> writeErrorInLog(ex));
        }

        z_messageAdapter messagesAdapter = new z_messageAdapter((message, position) -> log(message.getId() + " - " + message.getPeerId() + " - " + message.getMessage() + " - " + message.getDate()), this, listMessages);

        messagesView.setLayoutManager(new LinearLayoutManager(this));

        messagesView.setAdapter(messagesAdapter);

        messagesView.scrollToPosition(messagesAdapter.getItemCount() - 1);

        z_globals.z_listener.addObserver(event -> {
            listMessages.add((z_message) event);
            runOnUiThread(messagesAdapter::updateData);
            runOnUiThread(() -> messagesView.scrollToPosition(messagesAdapter.getItemCount() - 1));
        });

        sendView.setOnClickListener(v -> {
            if (!hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                return;
            }
            String messagePrepared = messageView.getText().toString().trim();
            if (!messagePrepared.equals("")) {
                List<String> splitMessage = new ArrayList<>();
                int symbols = 0;
                int index = 0;
                while (symbols < messagePrepared.length()) {
                    try {
                        splitMessage.add(messagePrepared.substring(symbols, Math.min(symbols + 1024, messagePrepared.length())));
                        symbols += 1024;
                        if (!splitMessage.get(index).trim().equals("")) {
                            sendMsg(splitMessage.get(index), eid);
                            index += 1;
                        }
                    } catch (Exception ex) {
                        runOnUiThread(() -> writeErrorInLog(ex));
                    }
                }
            }
            messageView.setText("");
        });

    }

    private void sendMsg(String message, Integer peer_id) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("text", message.replaceAll("\\n", "\\\n"));
        json.put("to_id", peer_id);

        executeApiMethodPost("messages", "send", json);

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
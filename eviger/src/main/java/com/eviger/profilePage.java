package com.eviger;

import static com.eviger.z_globals.*;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class profilePage extends AppCompatActivity {

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        dialogsAdapter = new z_dialogAdapter((dialog, position) -> startActivity(new Intent(profilePage.this, messagesChat.class).putExtra("eid", dialog.getId())), this, dialogs);

        Button searchUsers = findViewById(R.id.toSearch_profilePage);
        searchUsers.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, searchUsers.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        ImageButton toMessages = findViewById(R.id.toMessages);
        toMessages.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, messagesPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        ImageButton toSettings = findViewById(R.id.toSettings);
        toSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(profilePage.this, settingsPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        TextView nameProfile = findViewById(R.id.profileName_profilePage);

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

                                if (longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("senderId") != myProfile.getInt("eid")) {

                                    NotificationCompat.Builder builder;

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationChannel messagesChannel = new NotificationChannel(channelMessages, "message", NotificationManager.IMPORTANCE_DEFAULT);
                                        messagesChannel.setDescription("messageLongPoll");
                                        mNotificationManager.createNotificationChannel(messagesChannel);

                                        builder = new NotificationCompat.Builder(profilePage.this, channelMessages)
                                                .setSmallIcon(R.drawable.ic_messages)
                                                .setContentTitle((String) getProfileById(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peerId"))[1])
                                                .setContentText(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getString("message"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setChannelId(channelMessages);

                                    } else {

                                        builder = new NotificationCompat.Builder(profilePage.this, channelMessages)
                                                .setSmallIcon(R.drawable.ic_messages)
                                                .setContentTitle((String) getProfileById(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peerId"))[1])
                                                .setContentText(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getString("message"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH);

                                    }

                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(profilePage.this);

                                    notificationManager.notify(1, builder.build());
                                }

                                z_globals.z_listener.newEvent(new z_message(
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("id"),
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peerId"),
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("date"),
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getString("message"),
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("senderId") == myProfile.getInt("eid")
                                ));

                                moveDialogToTop(dialogs,
                                        longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peerId"),
                                        new z_dialog(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peerId"),
                                                (String) getProfileById(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("peerId"))[1],
                                                new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getInt("date") * 1000L)),
                                                longPollResponse.getJSONArray("response").getJSONObject(i).getJSONObject("objects").getString("message").replaceAll("\\n", "")));

                                runOnUiThread(() -> dialogsAdapter.updateData());

                            }

                        }

                    } catch (Exception ex) {
                        sendingOnline = false;
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            writeErrorInLog(ex);
                        });
                        break;
                    }
                }
            }).start();

            new Thread(() -> {
                while (sendingOnline) {
                    try {
                        setOnline();
                        Thread.sleep(120000);
                    } catch (Exception ex) {
                        sendingOnline = false;
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            writeErrorInLog(ex);
                        });
                        break;
                    }
                }
            }).start();

        } catch (Exception ex) {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                writeErrorInLog(ex);
            });
        }

    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!inAnotherActivity) {
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
    }

}

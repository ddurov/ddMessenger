package com.eviger;

import static com.eviger.z_globals.dialogs;
import static com.eviger.z_globals.sendingOnline;
import static com.eviger.z_globals.setOffline;
import static com.eviger.z_globals.setOnline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class messagesPage extends AppCompatActivity {

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_page);

        z_globals.dialogsAdapter = new z_dialogAdapter((dialog, position) -> {
            startActivity(new Intent(messagesPage.this, messagesChat.class)
                    .putExtra("eid", dialog.getId())
            );
        }, messagesPage.this, dialogs);

        ImageButton toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(messagesPage.this, profilePage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        ImageButton toSettings = findViewById(R.id.toSettings);
        toSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(messagesPage.this, settingsPage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        RecyclerView view_dialogsList = findViewById(R.id.dialogsList_messagesPage);

        view_dialogsList.setLayoutManager(new LinearLayoutManager(this));

        view_dialogsList.setAdapter(z_globals.dialogsAdapter);

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

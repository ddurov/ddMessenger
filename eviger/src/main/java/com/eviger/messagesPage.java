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

    ImageButton btnProfile, btnSettings;

    boolean inAnotherActivity = false, activatedMethodUserLeaveHint = false;

    public static z_dialogAdapter dialogsAdapter;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_page);

        dialogsAdapter = new z_dialogAdapter((dialog, position) -> startActivity(new Intent(messagesPage.this, messagesChat.class).putExtra("eid", dialog.getId())), this, dialogs);

        btnProfile = findViewById(R.id.toProfile);
        btnProfile.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(messagesPage.this, profilePage.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        btnSettings = findViewById(R.id.toSettings);
        btnSettings.setOnClickListener(v -> {
            inAnotherActivity = true;
            Intent in = new Intent(messagesPage.this, settingsAccount.class);
            in.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(in);
        });

        RecyclerView view_dialogsList = findViewById(R.id.dialogsList);

        view_dialogsList.setLayoutManager(new LinearLayoutManager(this));

        view_dialogsList.setAdapter(dialogsAdapter);

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

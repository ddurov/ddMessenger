package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.globals.dialogs;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;
import static com.ddprojects.messager.service.globals.writeMessageInLogCat;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ddprojects.messager.models.Dialog;
import com.ddprojects.messager.models.Message;
import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.messageItemAdapter;
import com.ddprojects.messager.service.observableHashMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Hashtable;

public class dialogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        Dialog dialog = (Dialog) getIntent().getSerializableExtra("dialogInstance");
        observableHashMap<Integer, Message> messagesDialog = dialog.getMessages();

        ((TextView) findViewById(R.id.headerText)).setText(dialog.getPeerName());

        messageItemAdapter adapter = new messageItemAdapter((message, position) -> {
            writeMessageInLogCat("ID: " + message.getId() + ", peerAId: " + message.getPeerAId() + ", text: " + message.getText());
        }, dialogActivity.this, messagesDialog);

        RecyclerView messagesList = findViewById(R.id.body);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialogActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);
        messagesList.setAdapter(adapter);

        Hashtable<String, String> getParams = new Hashtable<>();
        getParams.put("aId", String.valueOf(dialog.getPeerAId()));

        if (hasInternetConnection()) {
            executeApiMethod(
                    "get",
                    "product",
                    "messages",
                    "getHistory",
                    getParams,
                    new APIRequester.Callback() {
                        @Override
                        public void onFailure(Exception exception) {
                            if (exception instanceof APIException) {
                                showToastMessage(
                                        APIException.translate(exception.getMessage()),
                                        false
                                );
                            } else {
                                writeErrorInLog(exception);
                            }
                        }

                        @Override
                        public void onSuccess(SuccessResponse response) {
                            JsonArray messagesResponse = response.getBody().getAsJsonArray();

                            for (JsonElement message : messagesResponse) {
                                Message messageObject = new Gson().fromJson(message, Message.class);

                                dialog.setText(messageObject.getText());
                                dialog.setTime(messageObject.getTime());
                                dialog.putMessage(messageObject);
                            }

                            dialogs.put(dialog.getPeerAId(), dialog);
                        }
                    }
            );
        }

        messagesDialog.setOnEventListener(map -> runOnUiThread(() -> {
            adapter.updateData();
            messagesList.scrollToPosition(map.size() - 1);
        }));
    }
}
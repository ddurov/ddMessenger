package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.globals.cachedData;
import static com.ddprojects.messager.service.globals.dialogs;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ddprojects.messager.models.Dialog;
import com.ddprojects.messager.models.Message;
import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.models.User;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.dialogItemAdapter;
import com.ddprojects.messager.service.listener;
import com.ddprojects.messager.service.observableHashMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Hashtable;

public class dialogsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        ((TextView) findViewById(R.id.headerText)).setText(
                String.format(
                        "%s - %s",
                        getString(R.string.dialogs),
                        ((User) cachedData.get("user")).getUsername()
                )
        );

        dialogItemAdapter adapter = new dialogItemAdapter((dialog, position) ->
                startActivity(new Intent(
                        dialogsActivity.this,
                        dialogActivity.class
                ).putExtra("dialogInstance", dialog)),
                dialogsActivity.this,
                dialogs
        );

        RecyclerView dialogsList = findViewById(R.id.body);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(dialogsActivity.this);
        dialogsList.setLayoutManager(linearLayoutManager);
        dialogsList.setAdapter(adapter);

        if (hasInternetConnection()) {
            executeApiMethod(
                    "get",
                    "product",
                    "messages",
                    "getDialogs",
                    new Hashtable<>(),
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
                            JsonArray dialogsResponse = response.getBody().getAsJsonArray();

                            for (JsonElement dialog : dialogsResponse) {
                                Dialog dialogObject = new Gson().fromJson(dialog, Dialog.class);
                                dialogObject.setMessages(new observableHashMap<>());

                                Hashtable<String, String> getParams = new Hashtable<>();
                                getParams.put("aId", String.valueOf(dialogObject.getPeerAId()));

                                executeApiMethod(
                                        "get",
                                        "product",
                                        "user",
                                        "get",
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
                                                dialogObject.setPeerName(
                                                        new Gson().fromJson(
                                                                response.getBody(),
                                                                User.class
                                                        ).getUsername()
                                                );

                                                dialogs.put(dialogObject.getPeerAId(), dialogObject);
                                            }
                                        }
                                );
                            }
                        }
                    }
            );
        } else {
            dialogs.putAll((observableHashMap<Integer, Dialog>) cachedData.get("dialogs"));
        }

        listener.addObserver(message -> {
            if (message[0] == "newMessage") {
                Message newMessage = (Message) message[1];
                int newMessagePeerAId = newMessage.getPeerAId();
                int newMessageSenderAId = newMessage.getSenderAId();
                int peerAId = newMessageSenderAId == ((User) cachedData.get("user")).getAId() ?
                        newMessagePeerAId : newMessageSenderAId;

                if (dialogs.get(peerAId) != null) {
                    Dialog dialog = dialogs.get(peerAId);
                    dialog.setTime(newMessage.getTime());
                    dialog.setText(newMessage.getText());
                    dialog.putMessage(newMessage);
                    dialogs.put(peerAId, dialog);
                } else {
                    Hashtable<String, String> getParams = new Hashtable<>();
                    getParams.put("aId", String.valueOf(peerAId));

                    executeApiMethod(
                            "get",
                            "product",
                            "user",
                            "get",
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
                                    User user = new Gson().fromJson(response.getBody(), User.class);

                                    observableHashMap<Integer, Message> messages = new observableHashMap<>();
                                    messages.put(newMessage.getId(), newMessage);

                                    dialogs.put(newMessage.getPeerAId(), new Dialog(
                                            peerAId,
                                            user.getUsername(),
                                            newMessage.getText(),
                                            newMessage.getTime(),
                                            messages
                                    ));
                                }
                            }
                    );
                }
            }
        });

        dialogs.setOnEventListener(map -> {
            runOnUiThread(() -> {
                adapter.updateData();
                if (!dialogs.isEmpty()) {
                    findViewById(R.id.body).setVisibility(View.VISIBLE);
                    findViewById(R.id.findUsersBody).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.body).setVisibility(View.GONE);
                    findViewById(R.id.findUsersBody).setVisibility(View.VISIBLE);
                }
            });
            cachedData.put("dialogs", map);
        });
    }
}
package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodAsync;
import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import okhttp3.Response;

public class dialogsActivity extends AppCompatActivity {

    private final ArrayList<Dialog> dialogs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        Hashtable<String, String> getDialogsParams = new Hashtable<>();

        executeApiMethodAsync(
                "get",
                "product",
                "messages",
                "getDialogs",
                getDialogsParams,
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
                            showToastMessage(
                                    getString(R.string.error_request_failed),
                                    false
                            );
                        }
                    }

                    @Override
                    public void onSuccess(Response response) throws IOException {
                        String getDialogsResponse = response.body().string();

                        JsonArray dialogsResponse = new Gson().fromJson(
                                getDialogsResponse,
                                SuccessResponse.class
                        ).getBody().getAsJsonArray();

                        for (JsonElement dialog : dialogsResponse) {
                            Dialog dialogObject = new Gson().fromJson(dialog, Dialog.class);

                            Hashtable<String, String> getParams = new Hashtable<>();
                            getParams.put("aId", String.valueOf(dialogObject.getMessageUserId()));

                            try {
                                dialogObject.setMessageUserName(new Gson().fromJson(executeApiMethodSync(
                                        "get",
                                        "product",
                                        "user",
                                        "get",
                                        getParams
                                ).getBody(), User.class).getUsername());

                                dialogs.add(dialogObject);
                            } catch (APIException APIEx) {
                                showToastMessage(
                                        APIException.translate(APIEx.getMessage()),
                                        false
                                );
                            }
                        }

                        dialogItemAdapter adapter = new dialogItemAdapter((dialog, position) -> startActivity(
                                new Intent(
                                        dialogsActivity.this,
                                        dialogActivity.class
                                ).putExtra("aId", dialog.getMessageUserId())
                        ), dialogsActivity.this, dialogsActivity.this.dialogs);

                        if (!dialogsActivity.this.dialogs.isEmpty()) {
                            runOnUiThread(() -> {
                                findViewById(R.id.body).setVisibility(View.VISIBLE);
                                findViewById(R.id.findUsersBody).setVisibility(View.GONE);

                                RecyclerView dialogsList = findViewById(R.id.body);
                                dialogsList.setLayoutManager(new LinearLayoutManager(dialogsActivity.this));
                                dialogsList.setAdapter(adapter);
                            });
                        }

                        listener.addObserver(message -> {
                            if (message[0] == "newMessage") {
                                Message newMessage = (Message) message[1];

                                boolean dialogFound = false;
                                for (Dialog dialog : dialogs) {
                                    if (dialog.getMessageUserId() == newMessage.getMessageUserId()) {
                                        dialog.setMessageDate(newMessage.getMessageDate());
                                        dialog.setMessageText(newMessage.getMessage());
                                        dialogs.set(dialogs.indexOf(dialog), dialog);
                                        dialogFound = true;
                                        break;
                                    }
                                }
                                if (!dialogFound) {
                                    try {
                                        Hashtable<String, String> getParams = new Hashtable<>();
                                        getParams.put("aId", String.valueOf(newMessage.getMessageUserId()));

                                        User user = new Gson().fromJson(executeApiMethodSync(
                                                "get",
                                                "product",
                                                "user",
                                                "get",
                                                getParams
                                        ).getBody(), User.class);

                                        dialogs.add(new Dialog(
                                                newMessage.getMessageUserId(),
                                                user.getUsername(),
                                                newMessage.getMessageDate(),
                                                newMessage.getMessage()
                                        ));
                                    } catch (APIException APIEx) {
                                        showToastMessage(
                                                APIException.translate(APIEx.getMessage()),
                                                false
                                        );
                                    } catch (IOException e) {
                                        writeErrorInLog(e);
                                        showToastMessage(
                                                getString(R.string.error_request_failed),
                                                false
                                        );
                                    }
                                }

                                adapter.updateData(adapter);
                            }
                        });
                    }
                }
        );
    }
}
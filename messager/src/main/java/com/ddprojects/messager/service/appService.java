package com.ddprojects.messager.service;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.appInitVars;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ddprojects.messager.R;
import com.ddprojects.messager.models.Message;
import com.ddprojects.messager.service.api.APIException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;

public class appService extends Service {
    public void onCreate() {
        super.onCreate();
        appInitVars();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            while (true) {
                if (hasInternetConnection()) {
                    Hashtable<String, String> listenParams = new Hashtable<>();
                    listenParams.put("timeout", "25");

                    try {
                        JsonArray dataArray = executeApiMethodSync(
                                "get",
                                "product",
                                "longpoll",
                                "listen",
                                listenParams
                        ).getBody().getAsJsonArray();

                        if (!dataArray.isEmpty()) {
                            for (JsonElement data : dataArray) {
                                if (Objects.equals(
                                        data.getAsJsonObject().get("type").getAsString(),
                                        "newMessage"
                                )) {
                                    JsonObject messageData = data.getAsJsonObject()
                                            .getAsJsonObject("data");
                                    listener.newEvent(new Object[]{
                                            "newMessage",
                                            new Message(
                                                    messageData.get("id").getAsInt(),
                                                    messageData.get("senderAId").getAsInt(),
                                                    messageData.get("peerAId").getAsInt(),
                                                    messageData.get("message").getAsString(),
                                                    messageData.get("messageDate").getAsInt()
                                            )
                                    });
                                }
                            }
                        }
                    } catch (APIException APIEx) {
                        showToastMessage(
                                APIException.translate(APIEx.getMessage()),
                                false
                        );
                    } catch (IOException IOEx) {
                        writeErrorInLog(IOEx);
                        showToastMessage(
                                fakeContext.getInstance().getString(R.string.error_request_failed),
                                false
                        );
                    }
                }
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
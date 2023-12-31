package com.ddprojects.messager.service;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.api.APIRequester.setupApiClient;
import static com.ddprojects.messager.service.globals.cachedData;
import static com.ddprojects.messager.service.globals.dialogs;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ddprojects.messager.models.Message;
import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Hashtable;
import java.util.Objects;

public class appService extends Service {
    public void onCreate() {
        super.onCreate();
        setupApiClient();
        cachedData.setOnEventListener(cacheService::updateInstance);
        dialogs.setOnEventListener(table -> cachedData.put("dialogs", table));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        update();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void update() {
        Hashtable<String, String> listenParams = new Hashtable<>();
        listenParams.put("timeout", "25");

        executeApiMethod(
                "get",
                "product",
                "longpoll",
                "listen",
                listenParams,
                new APIRequester.Callback() {
                    @Override
                    public void onFailure(Exception exception) {
                        if (!(exception instanceof APIException)) writeErrorInLog(exception);
                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        JsonArray dataArray = response.getBody().getAsJsonArray();

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
                                                    messageData.get("text").getAsString(),
                                                    messageData.get("time").getAsInt()
                                            )
                                    });
                                }
                            }
                        }

                        update();
                    }
                }
        );
    }
}
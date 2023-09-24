package com.ddprojects.messager.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class appService extends Service {
    public appService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
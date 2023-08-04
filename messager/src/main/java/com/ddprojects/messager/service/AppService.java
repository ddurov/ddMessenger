package com.ddprojects.messager.service;

import static com.ddprojects.messager.service.globals.PDDEditor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class AppService extends Service {
    public AppService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                PDDEditor.apply();
            }
        }, 0, 500);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
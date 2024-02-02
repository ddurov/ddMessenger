package com.ddprojects.messager.service;

import android.app.Application;

import androidx.room.Room;
import androidx.room.RoomDatabase;

public class app extends Application {
    private static app instance;
    private static appDatabase databaseInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        databaseInstance = Room.databaseBuilder(instance, appDatabase.class, "app")
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                .build();
    }

    public static app getInstance() {
        return instance;
    }

    public static appDatabase getDatabaseInstance() {
        return databaseInstance;
    }
}
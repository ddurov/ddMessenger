package com.ddprojects.messager.service;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import java.util.Hashtable;

public class fakeContext extends Application {
    private static fakeContext instance;
    private static Handler mainThreadHandler;
    public static observableHashtable<Object, Object> liveData;
    public static Hashtable<String, String> APIEndPoints;
    public static SharedPreferences persistentDataOnDisk;
    public static SharedPreferences.Editor PDDEditor;

    public fakeContext() {
        instance = this;
    }

    public static fakeContext getInstance() {
        return instance;
    }

    public static Handler getMainThreadHandler() {
        if (mainThreadHandler == null) {
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return mainThreadHandler;
    }
}
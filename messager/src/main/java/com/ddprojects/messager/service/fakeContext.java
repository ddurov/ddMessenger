package com.ddprojects.messager.service;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

public class fakeContext extends Application {
    private static fakeContext instance;
    private static Handler mainThreadHandler;

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
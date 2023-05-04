package com.ddprojects.messager.service;

import android.app.Application;

public class fakeContext extends Application {

    private static fakeContext instance;

    public fakeContext() {
        instance = this;
    }

    public static fakeContext getInstance() {
        return instance;
    }

}
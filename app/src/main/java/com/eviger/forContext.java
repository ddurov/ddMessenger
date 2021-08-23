package com.eviger;

public class forContext extends android.app.Application {

    private static forContext instance;

    public forContext() {
        instance = this;
    }

    public static forContext getInstance() {
        return instance;
    }

}
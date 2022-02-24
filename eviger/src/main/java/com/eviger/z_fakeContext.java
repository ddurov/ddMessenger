package com.eviger;

public class z_fakeContext extends android.app.Application {

    private static z_fakeContext instance;

    public z_fakeContext() {
        instance = this;
    }

    public static z_fakeContext getInstance() {
        return instance;
    }

}
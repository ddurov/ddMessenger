package com.ddprojects.messager.models;

import java.io.Serializable;

public class Update implements Serializable {
    private final String versionName, description;
    private final int versionCode;

    public Update(
            String versionName,
            int versionCode,
            String description
    ) {
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.description = description;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDescription() {
        return description;
    }

    public int getVersionCode() {
        return versionCode;
    }
}

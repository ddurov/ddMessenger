package com.ddprojects.messager.models;

import java.io.Serializable;

public class User implements Serializable {
    private final int aId;
    private final String username;

    public User(
            int aId,
            String username
    ) {
        this.aId = aId;
        this.username = username;
    }

    public int getAId() {
        return aId;
    }

    public String getUsername() {
        return username;
    }
}

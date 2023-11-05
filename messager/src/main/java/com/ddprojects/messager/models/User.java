package com.ddprojects.messager.models;

import java.io.Serializable;

public class User implements Serializable {
    private int aId;
    private String username;

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

    public void setAId(int aId) {
        this.aId = aId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

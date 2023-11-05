package com.ddprojects.messager.models;

import java.io.Serializable;

public class Update implements Serializable {
    private String version;
    private String description;

    public Update(
            String version,
            String description
    ) {
        this.version = version;
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

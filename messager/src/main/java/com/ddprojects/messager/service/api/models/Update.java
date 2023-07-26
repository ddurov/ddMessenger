package com.ddprojects.messager.service.api.models;

public class Update extends SuccessResponse {
    private String version;
    private String description;

    public Update(String version, String description) {
        super();
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

package com.ddprojects.messager.models;

import com.google.gson.JsonElement;

public class SuccessResponse {
    private int code;
    private JsonElement body;

    public SuccessResponse(int code, JsonElement body) {
        this.code = code;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public JsonElement getBody() {
        return body;
    }

    public void setBody(JsonElement body) {
        this.body = body;
    }
}

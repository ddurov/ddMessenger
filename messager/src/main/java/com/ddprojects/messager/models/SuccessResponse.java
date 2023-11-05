package com.ddprojects.messager.models;

import com.google.gson.JsonElement;

import java.io.Serializable;

public class SuccessResponse implements Serializable {
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

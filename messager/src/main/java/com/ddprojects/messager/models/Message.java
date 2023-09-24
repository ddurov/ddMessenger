package com.ddprojects.messager.models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id")
    private int messageId;
    @SerializedName("out")
    private boolean messageOut;
    @SerializedName("peerAId")
    private int messageUserId;
    @SerializedName("text")
    private String message;
    @SerializedName("date")
    private int messageDate;

    public Message(
            int messageId,
            boolean messageOut,
            int messageUserId,
            String message,
            int messageDate
    ) {
        this.messageId = messageId;
        this.messageOut = messageOut;
        this.messageUserId = messageUserId;
        this.message = message;
        this.messageDate = messageDate;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public boolean isMessageOut() {
        return messageOut;
    }

    public void setMessageOut(boolean messageOut) {
        this.messageOut = messageOut;
    }

    public int getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(int messageUserId) {
        this.messageUserId = messageUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(int messageDate) {
        this.messageDate = messageDate;
    }
}

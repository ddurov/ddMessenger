package com.ddprojects.messager.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Dialog implements Serializable {
    @SerializedName("peerAId")
    private int messageUserId;
    private String messageUserName;
    @SerializedName("lastMessageDate")
    private int messageDate;
    @SerializedName("lastMessage")
    private String messageText;

    public Dialog(
            int messageUserId,
            String messageUserName,
            int messageDate,
            String messageText
    ) {
        this.messageUserId = messageUserId;
        this.messageUserName = messageUserName;
        this.messageDate = messageDate;
        this.messageText = messageText;
    }

    public int getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(int messageUserId) {
        this.messageUserId = messageUserId;
    }

    public String getMessageUserName() {
        return messageUserName;
    }

    public void setMessageUserName(String messageUserName) {
        this.messageUserName = messageUserName;
    }

    public int getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(int messageDate) {
        this.messageDate = messageDate;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

}

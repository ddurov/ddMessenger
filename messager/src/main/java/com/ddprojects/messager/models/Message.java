package com.ddprojects.messager.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Message implements Serializable {
    @SerializedName("id")
    private int messageId;
    @SerializedName("senderAId")
    private int messageSenderAId;
    @SerializedName("peerAId")
    private int messagePeerAId;
    @SerializedName("text")
    private String message;
    @SerializedName("date")
    private int messageDate;

    public Message(
            int messageId,
            int messageSenderAId,
            int messagePeerAId,
            String message,
            int messageDate
    ) {
        this.messageId = messageId;
        this.messageSenderAId = messageSenderAId;
        this.messagePeerAId = messagePeerAId;
        this.message = message;
        this.messageDate = messageDate;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageSenderAId() {
        return messageSenderAId;
    }

    public void setMessageSenderAId(int messageSenderAId) {
        this.messageSenderAId = messageSenderAId;
    }

    public int getMessagePeerAId() {
        return messagePeerAId;
    }

    public void setMessagePeerAId(int messagePeerAId) {
        this.messagePeerAId = messagePeerAId;
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

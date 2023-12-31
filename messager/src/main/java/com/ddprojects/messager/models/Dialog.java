package com.ddprojects.messager.models;

import com.ddprojects.messager.service.observableHashMap;

import java.io.Serializable;

public class Dialog implements Serializable {
    private final int peerAId;
    private String peerName, text;
    private int time;
    private observableHashMap<Integer, Message> messages;

    public Dialog(
            int peerAId,
            String peerName,
            String text,
            int time,
            observableHashMap<Integer, Message> messages
    ) {
        this.peerAId = peerAId;
        this.peerName = peerName;
        this.text = text;
        this.time = time;
        this.messages = messages;
    }

    public int getPeerAId() {
        return peerAId;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public observableHashMap<Integer, Message> getMessages() {
        return messages;
    }

    public void setMessages(observableHashMap<Integer, Message> messages) {
        this.messages = messages;
    }

    public void putMessage(Message message) {
        this.messages.put(message.getId(), message);
    }
}

package com.ddprojects.messager.models;

import java.io.Serializable;

public class Message implements Serializable {
    private final int id, peerAId, senderAId, time;
    private String text;

    public Message(
            int id,
            int senderAId,
            int peerAId,
            String text,
            int time
    ) {
        this.id = id;
        this.senderAId = senderAId;
        this.peerAId = peerAId;
        this.text = text;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public int getSenderAId() {
        return senderAId;
    }

    public int getPeerAId() {
        return peerAId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTime() {
        return time;
    }
}

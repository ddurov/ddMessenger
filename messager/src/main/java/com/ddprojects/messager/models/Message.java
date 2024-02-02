package com.ddprojects.messager.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages", indices = {@Index(value = "id", unique = true)})
public class Message {
    @PrimaryKey
    public int id;
    public int dialogId;
    public int peerAId, senderAId;
    public int status;
    public String text;
    public int time;

    private Message() {}

    public Message(
            int id,
            int dialogId,
            int senderAId,
            int peerAId,
            String text,
            int status,
            int time
    ) {
        this.id = id;
        this.dialogId = dialogId;
        this.senderAId = senderAId;
        this.peerAId = peerAId;
        this.text = text;
        this.status = status;
        this.time = time;
    }
}

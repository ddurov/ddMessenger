package com.ddprojects.messager.models;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "dialogs", indices = {@Index(value = "peerAId", unique = true)})
public class Dialog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int peerAId;
    public String peerName, text;
    public int time;

    private Dialog() {}

    public Dialog(
            int peerAId,
            String peerName,
            String text,
            int time
    ) {
        this.peerAId = peerAId;
        this.peerName = peerName;
        this.text = text;
        this.time = time;
    }
}

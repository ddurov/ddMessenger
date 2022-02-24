package com.eviger;

public class z_message {

    private int id; // айди сообщения
    private int peerId; // айди юзера
    private int date; // время отправки сообщения
    private String message; // текст сообщения
    private boolean isOutgoing; // исходящее ли сообщение

    public z_message(int id, int peerId, int date, String message, boolean isOutgoing) {
        this.id = id;
        this.peerId = peerId;
        this.date = date;
        this.message = message;
        this.isOutgoing = isOutgoing;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }

    public void setOutgoing(boolean outgoing) {
        isOutgoing = outgoing;
    }
}

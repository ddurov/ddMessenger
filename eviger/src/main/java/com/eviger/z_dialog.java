package com.eviger;

public class z_dialog {

    private int id; // айди юзера
    private String username; // имя юзера
    private String date; // время отправки сообщения
    private String message; // текст сообщения

    public z_dialog(int id, String username, String date, String message) {

        this.id = id;
        this.username = username;
        this.date = date;
        this.message = message;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

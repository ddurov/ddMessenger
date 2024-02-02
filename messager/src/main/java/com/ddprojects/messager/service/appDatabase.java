package com.ddprojects.messager.service;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.ddprojects.messager.BuildConfig;
import com.ddprojects.messager.DAO.DialogsDAO;
import com.ddprojects.messager.DAO.MessagesDAO;
import com.ddprojects.messager.models.Dialog;
import com.ddprojects.messager.models.Message;

@Database(entities = {Dialog.class, Message.class}, version = BuildConfig.VERSION_CODE)
public abstract class appDatabase extends RoomDatabase {
    public abstract DialogsDAO dialogsDAO();
    public abstract MessagesDAO messagesDAO();
}

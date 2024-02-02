package com.ddprojects.messager.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ddprojects.messager.models.Message;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface MessagesDAO {
    @Query("SELECT * FROM messages WHERE dialogId=:dialogId ORDER BY messages.id")
    Flowable<List<Message>> getAllMessagesInDialog(int dialogId);
    @Query("SELECT * FROM messages WHERE dialogId=:dialogId AND id = :messageId")
    Single<Message> getMessageInDialogById(int messageId, int dialogId);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insert(Message data);
    @Update
    Completable update(Message data);
    @Delete
    Completable delete(Message data);
}


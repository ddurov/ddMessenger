package com.ddprojects.messager.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ddprojects.messager.models.Dialog;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DialogsDAO {
    @Query("SELECT * FROM dialogs ORDER BY time DESC")
    Flowable<List<Dialog>> getAllDialogs();
    @Query("SELECT * FROM dialogs WHERE id = :id")
    Single<Dialog> getDialogById(int id);
    @Query("SELECT * FROM dialogs WHERE peerAId = :peerAId")
    Single<Dialog> getDialogByPeerAId(int peerAId);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insert(Dialog data);
    @Update
    Completable update(Dialog data);
    @Delete
    Completable delete(Dialog data);
}

package ru.bruimafia.donotforget.repository.local_store;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY id DESC")
    Flowable<List<Note>> getRX();

    @Query("SELECT * FROM notes WHERE id = :id")
    Single<Note> get(long id);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    Flowable<List<Note>> getAll();

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY id DESC")
    Flowable<List<Note>> getAllOrderById();

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY is_fix DESC, date ASC, id DESC")
    Flowable<List<Note>> getAllOrderByRelevance();

    @Query("SELECT * FROM notes WHERE in_history = 1 ORDER BY date_delete DESC")
    Flowable<List<Note>> getHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Note note);

    @Update
    Completable update(Note note);

    @Query("UPDATE notes SET in_history = 1, date_delete = :dateDelete WHERE id = :id")
    Completable delete(long id, long dateDelete);

    @Query("UPDATE notes SET in_history = 0, date_delete = 0 WHERE id = :id")
    Completable recover(long id);

    @Query("UPDATE notes SET in_history = 1, date_delete = :dateDelete WHERE in_history = 0")
    Completable clear(long dateDelete);

    @Query("DELETE FROM notes WHERE in_history = 1")
    Completable clearHistory();

}
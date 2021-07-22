package ru.bruimafia.donotforget.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import ru.bruimafia.donotforget.repository.local_store.Note;

public interface RepositoryBase {
    Single<Note> get(long id);

    Flowable<List<Note>> getAll();

    Flowable<List<Note>> getAllOrderById();

    Flowable<List<Note>> getAllOrderByRelevance();

    Flowable<List<Note>> getHistory();

    void create(Note note);

    void update(Note note);

    void delete(long id);

    void recover(long id);

    void clear();

    void clearHistory();

    void syncing();

    void onDestroy();
}

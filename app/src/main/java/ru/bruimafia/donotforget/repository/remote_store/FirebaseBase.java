package ru.bruimafia.donotforget.repository.remote_store;

import java.util.List;

import ru.bruimafia.donotforget.repository.local_store.Note;

public interface FirebaseBase {
    List<Note> getAll();

    void insertOrUpdate(Note note);

    void delete(long id);

    void recover(long id);

    void clear();

    void clearHistory();
}

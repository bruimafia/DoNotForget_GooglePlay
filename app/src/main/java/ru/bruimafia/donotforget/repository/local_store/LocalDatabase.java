package ru.bruimafia.donotforget.repository.local_store;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();
}

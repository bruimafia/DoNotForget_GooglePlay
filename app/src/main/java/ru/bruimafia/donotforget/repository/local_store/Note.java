package ru.bruimafia.donotforget.repository.local_store;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

import ru.bruimafia.donotforget.BR;

@Entity(tableName = "notes")
public class Note extends BaseObservable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;

    private long date;

    private int color;

    @ColumnInfo(name = "is_fix")
    private boolean isFix = true;

    @ColumnInfo(name = "in_history")
    private boolean inHistory = false;

    @ColumnInfo(name = "date_create")
    private long dateCreate = System.currentTimeMillis();

    @ColumnInfo(name = "date_delete")
    private long dateDelete;

    @Ignore
    public Note() {
    }

    public Note(String title, long date, int color, boolean isFix, boolean inHistory, long dateCreate, long dateDelete) {
        this.title = title;
        this.date = date;
        this.color = color;
        this.isFix = isFix;
        this.inHistory = inHistory;
        this.dateCreate = dateCreate;
        this.dateDelete = dateDelete;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    @Bindable
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        notifyPropertyChanged(BR.color);
    }

    @Bindable
    public boolean isFix() {
        return isFix;
    }

    public void setFix(boolean fix) {
        isFix = fix;
        notifyPropertyChanged(BR.fix);
    }

    @Bindable
    public boolean isInHistory() {
        return inHistory;
    }

    public void setInHistory(boolean inHistory) {
        this.inHistory = inHistory;
        notifyPropertyChanged(BR.inHistory);
    }

    @Bindable
    public long getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(long dateCreate) {
        this.dateCreate = dateCreate;
        notifyPropertyChanged(BR.dateCreate);
    }

    @Bindable
    public long getDateDelete() {
        return dateDelete;
    }

    public void setDateDelete(long dateDelete) {
        this.dateDelete = dateDelete;
        notifyPropertyChanged(BR.dateDelete);
    }

    @Override
    public boolean equals(Object o) {
        Note note = (Note) o;
        if (id != note.id) return false;
        return dateCreate >= note.dateCreate;
    }
}

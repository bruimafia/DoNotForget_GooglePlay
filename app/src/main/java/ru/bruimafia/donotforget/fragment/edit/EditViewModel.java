package ru.bruimafia.donotforget.fragment.edit;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.repository.Repository;
import ru.bruimafia.donotforget.repository.local_store.Note;

public class EditViewModel extends ViewModel {

    private Repository repository;
    public ObservableField<Note> note = new ObservableField<>();
    public ObservableField<Boolean> isFullVersion = new ObservableField<>(false);

    public EditViewModel() {
        repository = new Repository();
    }

    public void setNote(long id) {
        if (note.get() == null && id != -1)
            repository.get(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(n -> note.set(n))
                    .subscribe();

        if (note.get() == null && id == -1)
            note.set(new Note());
    }

    public void add(Note note) {
        repository.create(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public void recover(long id) {
        repository.recover(id);
    }

    public void setDate(long milliseconds) {
        note.get().setDate(dateInMidnight(milliseconds));
    }

    public void setTime(long milliseconds) {
        note.get().setDate(checkCurrentDate() + timeSinceMidnight(milliseconds));
    }

    private long dateInMidnight(long milliseconds) {
        Calendar rightNow = Calendar.getInstance();
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
        long sinceMidnight = (milliseconds + offset) % (24 * 60 * 60 * 1000);
        return milliseconds - sinceMidnight;
    }

    private long timeSinceMidnight(long milliseconds) {
        Calendar rightNow = Calendar.getInstance();
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
        return (milliseconds + offset) % (24 * 60 * 60 * 1000);
    }

    private long checkCurrentDate() {
        return (note.get().getDate() == 0) ? dateInMidnight(System.currentTimeMillis()) : dateInMidnight(note.get().getDate());
    }

}

package ru.bruimafia.donotforget.fragment.history;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.Flowable;
import ru.bruimafia.donotforget.repository.Repository;
import ru.bruimafia.donotforget.repository.local_store.Note;

public class HistoryViewModel extends ViewModel {

    private Repository repository;
    public ObservableField<List<Note>> notes = new ObservableField<>();
    public ObservableField<Boolean> isLoading = new ObservableField<>(true);
    public ObservableField<Boolean> isFullVersion = new ObservableField<>(false);

    public HistoryViewModel() {
        repository = new Repository();
    }

    public Flowable<List<Note>> getNotesInHistory() {
        return repository.getHistory();
    }

    public void clear() {
        repository.clearHistory();
    }

    public void recover(long id) {
        repository.recover(id);
    }

}

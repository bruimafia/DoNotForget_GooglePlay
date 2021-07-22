package ru.bruimafia.donotforget.fragment.tasks;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.Flowable;
import ru.bruimafia.donotforget.repository.Repository;
import ru.bruimafia.donotforget.repository.local_store.Note;

public class TasksViewModel extends ViewModel {

    private Repository repository;
    private Flowable<List<Note>> notes;
    public ObservableField<List<Note>> notesForScreen = new ObservableField<>();
    public ObservableField<Boolean> isOrderById = new ObservableField<>(true);
    public ObservableField<Boolean> isLoading = new ObservableField<>(true);
    public ObservableField<Boolean> isFullVersion = new ObservableField<>(false);

    public TasksViewModel() {
        repository = new Repository();
    }

    public Flowable<List<Note>> getNotes() {
        if (isOrderById.get())
            notes = repository.getAllOrderById();
        else
            notes = repository.getAllOrderByRelevance();
        return notes;
    }

    public void delete(long id) {
        repository.delete(id);
    }

}

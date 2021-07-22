package ru.bruimafia.donotforget.repository;

import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.background_work.worker.CheckNotificationsWorker;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.repository.local_store.NoteDao;
import ru.bruimafia.donotforget.repository.remote_store.FirebaseManager;
import ru.bruimafia.donotforget.util.Constants;

public class Repository implements RepositoryBase {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private NoteDao localStore;
    private FirebaseManager remoteStore;

    public Repository() {
        localStore = App.getInstance().getDatabase().noteDao();
        remoteStore = new FirebaseManager();
    }

    @Override
    public Single<Note> get(long id) {
        return localStore.get(id);
    }

    @Override
    public Flowable<List<Note>> getAll() {
        return localStore.getAll();
    }

    @Override
    public Flowable<List<Note>> getAllOrderById() {
        return localStore.getAllOrderById();
    }

    @Override
    public Flowable<List<Note>> getAllOrderByRelevance() {
        return localStore.getAllOrderByRelevance();
    }

    @Override
    public Flowable<List<Note>> getHistory() {
        return localStore.getHistory();
    }

    @Override
    public void create(Note note) {
        localStore.insert(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(id -> {
                    startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id);
                    note.setId(id);
                    remoteStore.insertOrUpdate(note);
                })
                .subscribe();
    }

    @Override
    public void update(Note note) {
        localStore.update(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, note.getId()))
                .subscribe();
        remoteStore.insertOrUpdate(note);
    }

    @Override
    public void delete(long id) {
        localStore.delete(id, System.currentTimeMillis())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> startCheckNotificationsWorker(Constants.ACTION_DELETE, id))
                .subscribe();
        remoteStore.delete(id);
    }

    @Override
    public void recover(long id) {
        localStore.recover(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> startCheckNotificationsWorker(Constants.ACTION_CREATE_OR_UPDATE, id))
                .subscribe();
        remoteStore.recover(id);
    }

    @Override
    public void clear() {
        localStore.clear(System.currentTimeMillis())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        remoteStore.clear();
    }

    @Override
    public void clearHistory() {
        localStore.clearHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        remoteStore.clearHistory();
    }

    @Override
    public void syncing() {
        List<Note> notesLocal = new ArrayList<>();
        List<Note> notesRemote = remoteStore.getAll();

        disposable.add(getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notes -> {
                    notesLocal.addAll(notes);

                    Log.d(Constants.TAG, "local.size -> " + notesLocal.size());
                    Log.d(Constants.TAG, "remote.size -> " + notesRemote.size());

                    List<Note> toCloud = comparison(notesLocal, notesRemote);
                    Log.d(Constants.TAG, "toCloud.size -> " + toCloud.size());

                    for (Note note : toCloud)
                        remoteStore.insertOrUpdate(note);
                }));
    }

    private List<Note> comparison(List<Note> local, List<Note> remote) {
        for (Note r : remote) {
            if (!isEntry(local, r))
                local.add(r);
        }
        return local;
    }

    private boolean isEntry(List<Note> local, Note note) {
        boolean result = false;
        for (Note l : local) {
            if (l.equals(note)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void startCheckNotificationsWorker(String action, long id) {
        Data data = new Data.Builder()
                .putString("action", action)
                .putLong(Constants.NOTE_ID, id)
                .build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CheckNotificationsWorker.class)
                .addTag(Constants.WORKER_CHECK_TAG)
                .setInputData(data)
                .build();
        WorkManager.getInstance(App.getInstance()).enqueue(workRequest);
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        disposable.dispose();
    }

}

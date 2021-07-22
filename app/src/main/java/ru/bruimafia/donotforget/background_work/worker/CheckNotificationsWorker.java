package ru.bruimafia.donotforget.background_work.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.background_work.Notification;
import ru.bruimafia.donotforget.background_work.broadcast_receiver.Receiver;
import ru.bruimafia.donotforget.repository.Repository;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.repository.local_store.NoteDao;
import ru.bruimafia.donotforget.util.Constants;

public class CheckNotificationsWorker extends Worker {

    private final CompositeDisposable disposable = new CompositeDisposable();
    private Repository repository = new Repository();

    public CheckNotificationsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String action = getInputData().getString("action");
        long noteID = getInputData().getLong(Constants.NOTE_ID, -1);
        Log.d(Constants.TAG, String.format("action -> %s; noteID -> %d", action, noteID));

        if (action != null) {
            switch (action) {
                case Constants.ACTION_START:
                    Log.d(Constants.TAG, "CheckNotificationsWorker: ACTION_START");
                    checkNotifications();
//                    repository.create(new Note(transformationTimeCheckNotifications(System.currentTimeMillis()), 0, 0, false, false, System.currentTimeMillis(), 0));
                    break;
                case Constants.ACTION_CREATE_OR_UPDATE:
                    Log.d(Constants.TAG, "CheckNotificationsWorker: ACTION_CREATE_OR_UPDATE");
                    repository.get(noteID)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess(this::create)
                            .subscribe();
                    break;
                case Constants.ACTION_DELETE:
                    Log.d(Constants.TAG, "CheckNotificationsWorker: ACTION_DELETE");
                    repository.get(noteID)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess(this::delete)
                            .subscribe();
                    break;
            }
        }

        return Result.success();
    }

    private static String transformationTimeCheckNotifications(long time) {
        return time == 0 ? App.getInstance().getResources().getString(R.string.tv_no_sync) : new SimpleDateFormat("EEEE, dd MMMM y HH:mm", Locale.getDefault()).format(new Date(time));
    }

    private void checkNotifications() {
        disposable.add(repository.getAllOrderById()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::checkNotes));
    }

    private void checkNotes(List<Note> notes) {
        for (Note note : notes) {
            if (note.isFix() || note.getDate() >= System.currentTimeMillis())
                create(note);
        }
    }

    private void create(Note note) {
        delete(note);

        if (note.isFix() && note.getDate() == 0)
            new Notification().createNotification(note);

        if (note.getDate() >= dateInMidnight())
            createNotificationWithAlarmManager(note);
    }

    private void createNotificationWithAlarmManager(Note note) {
        Intent intent = new Intent(App.getInstance(), Receiver.class).setAction(Constants.ACTION_CREATE_OR_UPDATE).putExtra("test_id", note.getId());
        PendingIntent penIntent = PendingIntent.getBroadcast(App.getInstance(), (int) note.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (penIntent != null) {
            AlarmManager manager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
            manager.cancel(penIntent);
            manager.set(AlarmManager.RTC_WAKEUP, note.getDate(), penIntent);
        }
    }

    private void deleteNotificationWithAlarmManager(Note note) {
        Intent intent = new Intent(App.getInstance(), Receiver.class).setAction(Constants.ACTION_CREATE_OR_UPDATE).putExtra("test_id", note.getId());
        PendingIntent penIntent = PendingIntent.getBroadcast(App.getInstance(), (int) note.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (penIntent != null) {
            AlarmManager manager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
            manager.cancel(penIntent);
        }
    }

    private void delete(Note note) {
        deleteNotificationWithAlarmManager(note);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(App.getInstance());
        notificationManager.cancel((int) note.getId());
    }

    private long dateInMidnight() {
        long milliseconds = System.currentTimeMillis();
        Calendar rightNow = Calendar.getInstance();
        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
        long sinceMidnight = (milliseconds + offset) % (24 * 60 * 60 * 1000);
        return milliseconds - sinceMidnight;
    }

    @Override
    public void onStopped() {
        disposable.clear();
        disposable.dispose();
        super.onStopped();
    }
}

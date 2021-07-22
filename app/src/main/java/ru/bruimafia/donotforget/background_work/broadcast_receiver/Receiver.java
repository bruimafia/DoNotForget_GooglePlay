package ru.bruimafia.donotforget.background_work.broadcast_receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.background_work.Notification;
import ru.bruimafia.donotforget.background_work.worker.CheckNotificationsWorker;
import ru.bruimafia.donotforget.repository.Repository;
import ru.bruimafia.donotforget.repository.local_store.NoteDao;
import ru.bruimafia.donotforget.util.Constants;


public class Receiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.d(Constants.TAG, "BroadcastReceiver: запущен");

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.d(Constants.TAG, "BroadcastReceiver: телефон включился");
                startCheckNotificationsWorker();
                startRepeatingAlarmManager();
            }

            if (intent.getAction().equals(Constants.ACTION_CHECK)) {
                startCheckNotificationsWorker();
            }

            if (intent.getAction().equals(Constants.ACTION_CREATE_OR_UPDATE)) {
                long id = intent.getLongExtra("test_id", -1);

                Repository repository = new Repository();
                repository.get(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess(note -> new Notification().createNotification(note))
                        .subscribe();
            }
        }
    }

    private void startCheckNotificationsWorker() {
        Data data = new Data.Builder()
                .putString("action", Constants.ACTION_START)
                .build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CheckNotificationsWorker.class)
                .addTag(Constants.WORKER_CHECK_TAG)
                .setInputData(data)
                .build();
        WorkManager.getInstance(App.getInstance()).enqueue(workRequest);
    }

    private void startRepeatingAlarmManager() {
        Intent intent = new Intent(context, Receiver.class).setAction(Constants.ACTION_CHECK);
        PendingIntent penIntent = PendingIntent.getBroadcast(context, Constants.RC_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (penIntent != null) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(penIntent);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.REPEAT_INTERVAL, penIntent);
        }

        Log.d(Constants.TAG, "BroadcastReceiver: запущен startRepeatingAlarmManager");
    }

}
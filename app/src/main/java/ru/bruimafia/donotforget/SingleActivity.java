package ru.bruimafia.donotforget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import ru.bruimafia.donotforget.background_work.Notification;
import ru.bruimafia.donotforget.background_work.broadcast_receiver.Receiver;
import ru.bruimafia.donotforget.background_work.worker.CheckNotificationsWorker;
import ru.bruimafia.donotforget.util.Constants;

public class SingleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_DoNotForget);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        Log.d(Constants.TAG, "SingleActivity: запущен");

        new Notification().createNotificationChannel();

        Data data = new Data.Builder()
                .putString("action", Constants.ACTION_START)
                .build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(CheckNotificationsWorker.class)
                .addTag(Constants.WORKER_CHECK_TAG)
                .setInputData(data)
                .build();
        WorkManager.getInstance(App.getInstance()).enqueue(workRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startRepeatingAlarmManager();
    }

    private void startRepeatingAlarmManager() {
        Intent intent = new Intent(App.getInstance(), Receiver.class).setAction(Constants.ACTION_CHECK);
        PendingIntent penIntent = PendingIntent.getBroadcast(App.getInstance(), Constants.RC_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (penIntent != null) {
            AlarmManager manager = (AlarmManager) App.getInstance().getSystemService(Context.ALARM_SERVICE);
            manager.cancel(penIntent);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.REPEAT_INTERVAL, penIntent);
        }

        Log.d(Constants.TAG, "SingleActivity: запущен startRepeatingAlarmManager");
    }
}
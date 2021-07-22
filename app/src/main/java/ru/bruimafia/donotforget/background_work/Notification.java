package ru.bruimafia.donotforget.background_work;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ru.bruimafia.donotforget.App;
import ru.bruimafia.donotforget.BuildConfig;
import ru.bruimafia.donotforget.R;
import ru.bruimafia.donotforget.SingleActivity;
import ru.bruimafia.donotforget.fragment.edit.EditFragment;
import ru.bruimafia.donotforget.repository.local_store.Note;
import ru.bruimafia.donotforget.util.Constants;

public class Notification {

    private Context context;

    public Notification() {
        context = App.getInstance();
    }

    // создание канала
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // создание уведомления
    public void createNotification(Note note) {
        RemoteViews view = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.notification);
        view.setInt(R.id.rl_view, "setBackgroundColor", note.getColor());
        view.setTextViewText(R.id.tv_noteTitle, note.getTitle());

        Intent intent = new Intent(context, SingleActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(Constants.NOTE_ID, note.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(view)
                .setSmallIcon(R.drawable.ic_statusbar)
                .setAutoCancel(false)
                .setOngoing(note.isFix()) // закрепить ли уведомление
                .setContentTitle(note.getTitle())
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) note.getId(), notificationBuilder.build());
    }

}

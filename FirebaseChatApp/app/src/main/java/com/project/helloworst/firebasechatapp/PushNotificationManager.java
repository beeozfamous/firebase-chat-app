package com.project.helloworst.firebasechatapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import io.reactivex.annotations.NonNull;

public class PushNotificationManager {
    private static PushNotificationManager pushNotifyManager;
    private Context context;
    private NotificationManager notifyManager;
    private static int PUSH_NOTIFICATION_ID = 0;

    @NonNull
    public static PushNotificationManager getInstance() {
        if (pushNotifyManager == null) {
            pushNotifyManager = new PushNotificationManager();
        }
        return pushNotifyManager;
    }

    public void init(Context context) {
        this.context = context;
        notifyManager = (NotificationManager) this.context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }
    @NonNull
    void generateNotification(String notifyMessage) {
        PUSH_NOTIFICATION_ID++;

        String noticeTitle = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(PUSH_NOTIFICATION_ID, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context);
        mBuilder.setSmallIcon(R.drawable.ic_app_owl_icon);
        mBuilder.setTicker(noticeTitle);
        mBuilder.setContentTitle(noticeTitle);
        mBuilder.setContentText(notifyMessage);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setOngoing(false);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);

        // Play default notification sound
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        notifyManager.notify(PUSH_NOTIFICATION_ID, mBuilder.build());
    }

    public void cancelNotification(int notifyId) {
        notifyManager.cancel(notifyId);
    }
}

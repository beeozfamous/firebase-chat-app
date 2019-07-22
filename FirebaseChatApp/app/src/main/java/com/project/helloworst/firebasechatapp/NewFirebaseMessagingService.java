package com.project.helloworst.firebasechatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.reactivex.annotations.NonNull;

public class NewFirebaseMessagingService extends FirebaseMessagingService {


    @NonNull
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null) return;


        if (remoteMessage.getNotification() != null) {

            //hanldeNotification(remoteMessage.getNotification().getBody());
            super.onMessageReceived(remoteMessage);
            String CHANNEL_ID = "A_TWICE_CHANNEL_ID";
            this.createNotificationChannel(CHANNEL_ID);

            String notification_title= remoteMessage.getNotification().getTitle();
            String notification_message= remoteMessage.getNotification().getBody();
            String click_action= remoteMessage.getNotification().getClickAction();
            String from_user_id=remoteMessage.getData().get("UserID");

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_app_owl_icon)
                    .setContentTitle(notification_title)
                    .setContentText(notification_message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            Intent intent = new Intent(click_action);
            intent.putExtra("UserID",from_user_id);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);



            int mNofifyID=(int) System.currentTimeMillis();
            NotificationManager mNotifyMNG = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMNG.notify(mNofifyID,mBuilder.build());


        }

        if (remoteMessage.getData().size() > 0) {

        }
    }

    private void hanldeNotification(String messege) {
        PushNotificationManager.getInstance().generateNotification(messege);
    }
    private void createNotificationChannel(String CHANNEL_ID) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name ="channel_name" ;//getString(R.string.channel_name);
            String description ="channel_description" ;//getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

package com.example.deeblog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class DeeFirebaseMessageReceiverService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "DEE_CHANNEL";
    Intent resultIntent;
    int NOTIF_ID=003;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification()!=null){
            String title=remoteMessage.getNotification().getTitle();
            String body=remoteMessage.getNotification().getBody();
           String click_action =remoteMessage.getNotification().getClickAction();

            if(remoteMessage.getData().size()>0) {
                String type = remoteMessage.getData().get("type");
                String dataFrom = remoteMessage.getData().get("fromUser");
                resultIntent = new Intent(click_action);
                resultIntent.putExtra("type", type);
                resultIntent.putExtra("fromUser", dataFrom);
            }
            sendNotification(title,body);
        }


    }

    private void sendNotification(String title, String body) {

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body);

        NotificationManager manager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIF_ID,builder.build());
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}

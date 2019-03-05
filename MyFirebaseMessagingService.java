package com.example.user.sadajura;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]



    public void onMessageReceived(RemoteMessage remoteMessage) {

            //추가한것
//            sendNotification(remoteMessage.getData().get("message"),remoteMessage.getData().get("title"), Integer.parseInt((remoteMessage.getData().get("room_no"))),remoteMessage.getData().get("recive_id"));
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("message");
            int room_no = Integer.parseInt(remoteMessage.getData().get("room_no"));
            String recive_id = remoteMessage.getData().get("recive_id");
            sendNotification(body,title,room_no,recive_id);

            if(remoteMessage.getData().get("message").equals("페이스톡 해요")){
                Intent intent1 = new Intent(getApplicationContext(),IncomingCallActivity.class);
                intent1.putExtra("room_no",room_no);
                startActivity(intent1);
            }

    }

    private void sendNotification(String messageBody, String title , int room_no , String recive_id) {
        String channelId = "channel";
        String channelName = "Channel Name";
        NotificationManager notifManager
                = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId);
        Intent notificationIntent = new Intent(getApplicationContext()
                , Client.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("mem_id",recive_id);
        notificationIntent.putExtra("ProductNo",room_no);
        int requestID = (int) System.currentTimeMillis();

        PendingIntent pendingIntent
                = PendingIntent.getActivity(getApplicationContext()
                , requestID
                , notificationIntent
                , PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle(title) // required
                .setContentText(messageBody)  // required

                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                .setSound(RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent);

        notifManager.notify(0, builder.build());

    }
}


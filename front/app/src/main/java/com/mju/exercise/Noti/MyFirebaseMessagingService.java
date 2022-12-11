package com.mju.exercise.Noti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mju.exercise.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "exercise";
    private static final CharSequence CHANNEL_NAME = "noti";
    private FirebaseDatabase firebaseDatabase;

    //토큰이 생성 또는 변경 될 경우 호출 됨
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.getReference("Notification").child("")
    }

    //토큰을 이용해 서버에서 push 받았을때 실행됨
    //수신한 메세지 처리
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        builder.setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_background);

        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }
}

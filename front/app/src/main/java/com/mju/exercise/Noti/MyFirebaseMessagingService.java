package com.mju.exercise.Noti;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mju.exercise.Preference.PreferenceUtil;
import com.mju.exercise.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "스포츠 메이트";
    private static final CharSequence CHANNEL_NAME = "notification";
    private FirebaseDatabase firebaseDatabase;
    private PreferenceUtil preferenceUtil;

    //토큰이 생성 또는 변경 될 경우 호출 됨
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        //로그인한 유저만 따로 토큰 관리
        if(loginCheck()){
            //토큰 생성 또는 변경시 db에 반영
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.getReference().child("Notification").child("ALL_USERS").child(preferenceUtil.getString("userId")).setValue(s);
        }
    }

    //로그인된 유저인지 체크
    private boolean loginCheck(){
        preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        String tmp = preferenceUtil.getString("userId");
        if(tmp == null || tmp.equals("")){
            return false;
        }
        return true;
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
                .setSmallIcon(R.drawable.app_icon);

        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }
}

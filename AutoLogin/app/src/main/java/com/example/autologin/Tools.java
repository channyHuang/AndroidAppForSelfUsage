package com.example.autologin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Tools {
    private static Tools mInstance;

    public static Tools getInstance() {
        if (null == mInstance)
            mInstance = new Tools();

        return mInstance;
    }

    private Tools() {}

    public void sendNotification(Context context, String title, String content) {
        Log.i("huang", "send notification");
        NotificationManager mNManager = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setChannelId(context.getPackageName())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(content)
                .build();

        Intent intent = new Intent(context, LoginResult.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        notification.contentIntent = contentIntent;
        mNManager.notify(1, notification);
    }

    public void sendNotificationForground() {

    }
}

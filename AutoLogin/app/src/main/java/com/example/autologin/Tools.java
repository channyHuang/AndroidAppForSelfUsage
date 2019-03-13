package com.example.autologin;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Tools {
    private String TAG = Tools.class.getSimpleName();
    private static Tools mInstance;

    int LoginCode = 1;
    int LogoutCode = 2;

    public static Tools getInstance() {
        if (null == mInstance)
            mInstance = new Tools();

        return mInstance;
    }

    private Tools() {
    }

    public void sendNotification(Context context, String title, String content) {
        Log.i("huang", "send notification");
        NotificationManager mNManager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
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
}

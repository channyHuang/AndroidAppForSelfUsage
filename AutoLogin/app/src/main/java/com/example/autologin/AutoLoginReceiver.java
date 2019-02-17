package com.example.autologin;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

public class AutoLoginReceiver extends BroadcastReceiver {
    private String TAG = AutoLoginReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (startPackage(context, "com.tencent.mm")) {
            Tools.getInstance().sendNotification(context, "AutoLoginReceiver", "start WeChat success");
        } else {
            Tools.getInstance().sendNotification(context, "AutoLoginReceiver", "start WeChat failed");
        }
    }

    public static boolean isBackground(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.i("后台", appProcess.processName);
                    return true;
                }else{
                    Log.i("前台", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    private boolean startPackage(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if(intent==null){
            Log.e(TAG, "package not found, " + packageName);
            return false;
        }else {
            context.startActivity(intent);
            return true;
        }
    }
}

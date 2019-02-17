package com.example.autologin;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AutoLoginService extends AccessibilityService
{
    private enum AUTO_STEPS{
        UNKNOW,
        WeChatMainPage,
        WeChatDisCover,
        WeChatMiniPro,
        CompMainPage
    };

    private AUTO_STEPS nStep = AUTO_STEPS.UNKNOW;

    int LoginCode = 1;
    int LogoutCode = 2;
    String TAG = AutoLoginService.class.getSimpleName();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (!isAccessibilitySettingsOn(this)) {
            /*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Accessibility is off");
            dialog.setMessage("Please turn on the Accessibility in Settings");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopSelf();
                }
            });
            dialog.show();*/
        };
        boolean isLogin = intent.getBooleanExtra("isLogin", false);
        startReminder(isLogin);

        nStep = AUTO_STEPS.WeChatMainPage;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        if (!event.getPackageName().equals(getString(R.string.WeChat_package))) return;
        AccessibilityNodeInfo sourceNodeInfo = null;
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                //Log.i("huang", "TYPE_WINDOW_STATE_CHANGED, curActivity " + event.getClassName().toString() + ", step = " + nStep);
                sourceNodeInfo = event.getSource();
                if (sourceNodeInfo == null) return;
                /*
                List<AccessibilityNodeInfo> subInfos = sourceNodeInfo.findAccessibilityNodeInfosByViewId(getString(R.string.WeChat_mainList));
                for (AccessibilityNodeInfo subInfo : subInfos) {
                    if (subInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)) {
                        Log.i("huang", "scroll success");
                        nStep = AUTO_STEPS.WeChatMiniPro;
                        break;
                    }
                }*/
                List<AccessibilityNodeInfo> subInfos;
                if (nStep == AUTO_STEPS.WeChatMainPage) {
                    if (findViewByTextAndAction(sourceNodeInfo, "Discover", AccessibilityNodeInfo.ACTION_CLICK)) {
                        nStep = AUTO_STEPS.WeChatDisCover;
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                //Log.i("huang", "TYPE_WINDOW_CONTENT_CHANGED, curActivity " + event.getClassName().toString() + ", nStep = " + nStep);
                sourceNodeInfo = event.getSource();
                if (null == sourceNodeInfo) return;
                if (nStep == AUTO_STEPS.WeChatDisCover) {
                    if (findViewByTextAndAction(sourceNodeInfo, "Mini Programs", AccessibilityNodeInfo.ACTION_CLICK)) {
                        nStep = AUTO_STEPS.WeChatMiniPro;
                    }
                } else if (nStep == AUTO_STEPS.WeChatMiniPro) {
                    if (findViewByTextAndAction(sourceNodeInfo, "华软工作平台", AccessibilityNodeInfo.ACTION_CLICK)) {
                        nStep = AUTO_STEPS.CompMainPage;
                    }
                } else if (nStep == AUTO_STEPS.CompMainPage){
                    //outputId(sourceNodeInfo, "-", AccessibilityNodeInfo.ACTION_CLICK);
                    if (findViewByTextAndAction(sourceNodeInfo, "一键签到", AccessibilityNodeInfo.ACTION_CLICK)) {
                        //nStep = AUTO_STEPS.WeChatMainPage;
                    }
                    List<AccessibilityNodeInfo> infos = outputIdByText(sourceNodeInfo, "一键签到");
                    for (AccessibilityNodeInfo info : infos) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        nStep = AUTO_STEPS.UNKNOW;
                    }
                } else return;
                break;
            default:
                break;
        }
        if (null != sourceNodeInfo) sourceNodeInfo.recycle();
        sleep();
    }

    private List<AccessibilityNodeInfo> outputIdByText(AccessibilityNodeInfo info, String text) {
        List<AccessibilityNodeInfo> infos = new ArrayList<>();
        if (null != info.getText()) {
            if (info.getText().toString().contains(text)) {
                Log.i("huang1", "found info with text " + text + ", clickable = " + info.isClickable());
                if (info.isClickable()) {
                    infos.add(info);
                }
            }
        }
        for (int i = 0; i < info.getChildCount(); i++) {
            infos.addAll(outputIdByText(info.getChild(i), text));
        }
        return infos;
    }
    //find the id of which want to scroll or click
    //accessibilityAction: AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD(2000) etc
    private void outputId(AccessibilityNodeInfo info, String parentId, int accessibilityAction) {
        Log.i("huang","parent = " + parentId + ", childCount = " + info.getChildCount() + ", id = " + info.getViewIdResourceName()
                + ", text = " + info.getText());

        if (AccessibilityNodeInfo.ACTION_CLICK == accessibilityAction) {
            if (info.isClickable())
                Log.i("huang", "clickable: " + info.getText());
        }
        else if (AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD == accessibilityAction) {

        }
        else return;

        for (int i = 0; i < info.getChildCount(); i++) {
            outputId(info.getChild(i), info.getViewIdResourceName(), accessibilityAction);
        }
    }

    @Override
    public void onInterrupt () {
        stopReminder(true);
    }

    
    private void startReminder(boolean isLogin) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if (preference == null) {
            Log.e(TAG, "preference is null");
            return;
        }

        long systemTime = System.currentTimeMillis();
        Date curTime;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean loginChecked = sharedPreferences.getBoolean(getString(R.string.login_enable_key), false);
        boolean logoutChecked = sharedPreferences.getBoolean(getString(R.string.logout_enable_key), false);
        if (isLogin) {
            if (!loginChecked) return;
            curTime = new Date(preference.getLong(getString(R.string.login_time_key), systemTime));
        }
        else {
            if (!logoutChecked) return;
            curTime = new Date(preference.getLong(getString(R.string.logout_time_key), systemTime));
        }

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(systemTime);
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        mCalendar.set(Calendar.HOUR_OF_DAY, curTime.getHours());
        mCalendar.set(Calendar.MINUTE, curTime.getMinutes());
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        long selectTime = mCalendar.getTimeInMillis();

        if(systemTime > selectTime) {
            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(this, AutoLoginReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, isLogin ? LoginCode : LogoutCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), (1000 * 60 * 60 * 24), pi);

        Toast.makeText(this, "start reminder success", Toast.LENGTH_SHORT).show();

        Notification notification = new NotificationCompat.Builder(this, createNotificationChannel(this))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("AutoLoginService")
                .setContentText("AutoLoginService is running")
                .build();

        Intent intents = new Intent(this, LoginResult.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intents, 0);
        notification.contentIntent = contentIntent;
        startForeground(1, notification);

        Log.i(TAG, "start reminder in service, " + curTime.getHours() + ":" + curTime.getMinutes());
    }

    public static String createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channelId";
            CharSequence channelName = "channelName";
            String channelDescription ="channelDescription";
            int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, channelImportance);
            // 设置描述 最长30字符
            notificationChannel.setDescription(channelDescription);
            // 该渠道的通知是否使用震动
            notificationChannel.enableVibration(true);
            // 设置显示模式
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            return channelId;
        } else {
            return null;
        }
    }

    private void stopReminder(boolean isLogin) {
        Intent intent = new Intent(this, AutoLoginReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, isLogin ? LoginCode : LogoutCode,
                intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(pi);
        stopForeground(true);

        Log.i(TAG, "stop reminder in service");
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean findViewByTextAndAction(AccessibilityNodeInfo sourceNodeInfo, String text, int action) {
        boolean res = false;

        List<AccessibilityNodeInfo> nodeInfos = sourceNodeInfo.findAccessibilityNodeInfosByText(text);
        if (null == nodeInfos || nodeInfos.isEmpty()) {
            Log.i("huang", "text " + text + " not found in AccessibilityNodeInfo");
            return false;
        }
        for (AccessibilityNodeInfo subInfo : nodeInfos) {
            AccessibilityNodeInfo info = subInfo;
            if (AccessibilityNodeInfo.ACTION_CLICK == action) {
                while (info != null && !info.isClickable()) {
                    info = info.getParent();
                }
            } else if (AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD == action) {
                while (info != null && !info.isScrollable()) {
                    info = info.getParent();
                }
            }
            else return false;
            if (info == null) {
                continue;
            }
            res = info.performAction(action);
            break;
        }

        if (!res) {
            Log.i("huang", "performAction failed");
        } else {
            Log.i("huang", "performAction success, text = " + text);
        }

        return res;
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + AutoLoginService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        Log.i("huang", "accessibilityEnabled = " + accessibilityEnabled);
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.i("huang", "settingValue = " + settingValue);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.i("huang", "accessibilityService = " + accessibilityService);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
package com.example.channy.channy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.icu.text.DateTimePatternGenerator.DAY;

/**
 * Created by channy on 2017/11/19.
 */

public class TrafficActivity extends baseActivity {
    String TAG = "TrafficActivity";
    File file = new File(Environment.getExternalStorageDirectory(), "myDateUsage.txt");
    String dateUsage = "";
    ListView appListView;
    ArrayAdapter adapter;
    ArrayList appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic);

        appList = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, appList);
        init();

        TextView textView = (TextView)findViewById(R.id.dataUsedView);
        getDataUsed();
        textView.setText(dateUsage);

        appListView = (ListView)findViewById(R.id.appList);
        appListView.setAdapter(adapter);

        Intent intent = new Intent(TrafficActivity.this, alarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(TrafficActivity.this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());//current time
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), DAY, sender);


    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    private void init() {

        try {
            FileInputStream stream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                dateUsage = str;
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        if (!file.exists()) {
            Log.e(TAG, "create file: myDateUsage.txt");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(dateUsage);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getDataUsed() {
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());//current time
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long startTime = calendar.getTimeInMillis();
        String subid = getSubscriberId(ConnectivityManager.TYPE_MOBILE);
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
        if (hasPermissionToReadNetworkStats() == false) {
            Log.e(TAG, "access denied");
            return;
        }
        try{
            Log.e(TAG, startTime + "-" + System.currentTimeMillis());
            NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subid, startTime, System.currentTimeMillis());
            dateUsage = ( bucket.getRxBytes() + bucket.getTxBytes() ) + "";
            Log.e(TAG, "dateUsage = " + bucket.getTxBytes());
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);
        appList.clear();
        /*
        for (ApplicationInfo info : applicationInfoList) {
            int uid = info.uid;
            try {
                NetworkStats stats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, getSubscriberId(ConnectivityManager.TYPE_MOBILE), startTime, System.currentTimeMillis(), uid);
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                stats.getNextBucket(bucket);
                stats.getNextBucket(bucket);
                double appDataUsage = (bucket.getRxBytes() + bucket.getTxBytes()) / 1024;
                appList.add(info.processName + " : " + appDataUsage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            adapter.notifyDataSetChanged();
        }*/

    }

    private boolean hasPermissionToReadNetworkStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        requestReadNetworkStats();
        return false;
    }
    // 打开“有权查看使用情况的应用”页面
    private void requestReadNetworkStats() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private String getSubscriberId(int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getSubscriberId();
        }
        return "";
    }
}

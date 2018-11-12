package com.example.channy.channy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by channy on 2017/11/19.
 */

public class alarmReceiver extends BroadcastReceiver {
    String TAG = "alarmReceiver";

    File file = new File(Environment.getExternalStorageDirectory(), "myDateUsage.txt");
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "receive alarm");
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
            writer.write("0\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

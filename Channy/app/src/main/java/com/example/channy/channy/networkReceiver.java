package com.example.channy.channy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by channy on 2017/11/18.
 */

public class networkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (intent.getAction().equals("Android.net.conn.CONNECTIVITY_CHANGE")) {
            if (dataInfo.isConnected()) {
                Toast.makeText(context, "data: ", Toast.LENGTH_LONG);
            }
        }

    }
}

package com.example.channy.channy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by channy on 2017/8/26.
 */

public class telReceiver extends BroadcastReceiver {
    private String TAG = "telReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Receive phone call");

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            //Log.e(TAG, "outgoing call");

            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            //Toast.makeText(context, "phone Number: "+outgoingNumber, Toast.LENGTH_LONG).show();
            Intent pit = new Intent(context,RecordService.class);
            pit.putExtra("outgoingNumber",outgoingNumber);
            context.startService(pit);
        }else {
            context.startService(new Intent(context,RecordService.class));
        }
    }
}


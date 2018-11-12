package com.example.channy.channy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import com.android.internal.telephony.ITelephony;

/**
 * Created by channy on 2017/8/27.
 */

public class RecordService extends Service {
    SimpleRecorder mRecorder = null;
    private String phoneNumber;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        //In
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        phoneNumber = intent.getStringExtra("outgoingNumber");   //号码

        super.onStart(intent, startId);
    }

    private final class PhoneListener extends PhoneStateListener {


        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:   //来电
                        String preNum = incomingNumber.substring(0, 3);
                        if (preNum.equals("400") || preNum.equals("021")) {

                            stopCall();
                        }
                        else {
                            mRecorder = new SimpleRecorder(incomingNumber);
                            mRecorder.setIsCommingNumber(true);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:   //接通电话
                        if (mRecorder == null) mRecorder = new SimpleRecorder(phoneNumber);
                        mRecorder.start();
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:  //挂掉电话
                        if (mRecorder != null) {
                            mRecorder.stop();
                            mRecorder = null;
                        }
                        break;

                }
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void stopCall() {

        try {
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            // 获取远程TELEPHONY_SERVICE的IBinder对象的代理
            IBinder binder = (IBinder) method.invoke(null, new Object[] { "phone" });
            // 将IBinder对象的代理转换为ITelephony对象
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            // 挂断电话
            telephony.endCall();
            //telephony.cancelMissedCallsNotification();

        } catch (Exception e) {

        }

    }
}


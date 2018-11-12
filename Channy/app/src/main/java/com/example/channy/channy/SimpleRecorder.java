package com.example.channy.channy;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by channy on 2017/8/26.
 */

public class SimpleRecorder {
    private String phoneNumber;
    private MediaRecorder mrecorder;
    private boolean started = false; //录音机是否已经启动
    private boolean isCommingNumber = false;//是否是来电
    private String TAG = "SimpleRecoder";

    public SimpleRecorder(String phoneNumber) {
        this.setPhoneNumber(phoneNumber);
    }

    public SimpleRecorder() {
    }

    public void start() {
        started = true;
        mrecorder = new MediaRecorder();
        File filePath = new File(Environment.getExternalStorageDirectory(), "/PhoneRecorder");
        if (!filePath.exists()) {
            filePath.mkdirs();
        }

        String callInOut = "Out";
        if (isCommingNumber) callInOut = "In";

        String fileName = callInOut + "."+ phoneNumber + new SimpleDateFormat(".yy.MM.dd.HH.mm.ss").format(new Date(System.currentTimeMillis())) + ".3gp";
        File recordName = new File(filePath, fileName);

        try {
            recordName.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mrecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);   //获得声音数据源
        mrecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);   // 按3gp格式输出
        mrecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mrecorder.setOutputFile(recordName.getAbsolutePath());   //输出文件

        try {
            mrecorder.prepare();    //准备
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mrecorder.start();
        started = true;
    }

    public void stop() {
        try {
            if (mrecorder != null) {
                mrecorder.stop();
                mrecorder.release();
                mrecorder = null;
            }
            started = false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean hasStarted) {
        this.started = hasStarted;
    }

    public void setIsCommingNumber(boolean isCommingNumber) {
        this.isCommingNumber = isCommingNumber;
    }
}


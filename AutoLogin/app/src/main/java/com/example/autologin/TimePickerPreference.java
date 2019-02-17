package com.example.autologin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.util.Date;

public class TimePickerPreference extends DialogPreference {
    private String TAG = TimePickerPreference.class.getSimpleName();
    TimePicker time;

    public String preferenceKey;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_timepicker);

        preferenceKey = context.getString(R.string.login_time_key);
        if ("logoutTimePicker".equals(this.getKey())) {
            preferenceKey = context.getString(R.string.logout_time_key);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        Date d=new Date(getSharedPreferences().getLong(preferenceKey, System.currentTimeMillis()));
        time=view.findViewById(R.id.timePicker);
        time.setIs24HourView(true);
        time.setCurrentHour(d.getHours());
        time.setCurrentMinute(d.getMinutes());
        setSummary(d.getHours()+":"+d.getMinutes());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) return;

        Date selectTime=new Date(0, 0, 0, time.getCurrentHour(), time.getCurrentMinute());
        setSummary(selectTime.getHours()+":"+selectTime.getMinutes());
        long value=selectTime.getTime();
        getSharedPreferences().edit().putLong(preferenceKey, value).apply();
        callChangeListener(value);
    }
}

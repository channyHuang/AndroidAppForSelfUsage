package com.example.autologin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import java.util.Date;

public class AutoLoginSetting extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private String TAG = AutoLoginSetting.class.getSimpleName();

    SwitchPreference loginSwitch;
    SwitchPreference logoutSwitch;
    TimePickerPreference loginPref;
    TimePickerPreference logoutPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference_setting);

        loginSwitch = (SwitchPreference)findPreference(getString(R.string.login_enable_key));
        logoutSwitch = (SwitchPreference)findPreference(getString(R.string.logout_enable_key));

        loginSwitch.setOnPreferenceChangeListener(this);
        logoutSwitch.setOnPreferenceChangeListener(this);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if (preference == null) {
            Log.e(TAG, "preference is null");
            return;
        }

        loginPref = (TimePickerPreference)findPreference(getString(R.string.login_timepicker));
        logoutPref = (TimePickerPreference)findPreference(getString(R.string.logout_timepicker));

        Date curTime = new Date(preference.getLong(loginPref.preferenceKey, System.currentTimeMillis()));
        loginPref.setSummary(curTime.getHours() + ":" + curTime.getMinutes());
        loginPref.setOnPreferenceChangeListener(this);
        curTime.setTime(preference.getLong(logoutPref.preferenceKey, System.currentTimeMillis()));
        logoutPref.setSummary(curTime.getHours() + ":" + curTime.getMinutes());
        logoutPref.setOnPreferenceChangeListener(this);

        //startAutoLoginService();
    }

    private void startAutoLoginService(boolean isLogin) {
        Intent intent = new Intent(this, AutoLoginService.class);
        intent.putExtra("isLogin", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
    }

    private void stopAutoLoginService() {
        Intent intent = new Intent(this, AutoLoginService.class);
        intent.putExtra("isLogin", true);
        stopService(intent);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (getString(R.string.login_enable_key).equals(preference.getKey())) {
            if ((boolean)newValue) {
                loginSwitch.setChecked(true);
                startAutoLoginService(true);
            } else {
                loginSwitch.setChecked(false);
                stopAutoLoginService();
            }
        }
        else if (getString(R.string.logout_enable_key).equals(preference.getKey())) {
            if ((boolean)newValue) {
                logoutSwitch.setChecked(true);
                startAutoLoginService(false);
            } else {
                logoutSwitch.setChecked(false);
                stopAutoLoginService();
            }
        }
        else if (getString(R.string.login_timepicker).equals(preference.getKey())) {
            if (!loginSwitch.isChecked()) return false;
            startAutoLoginService(true);
        }
        else if (getString(R.string.logout_timepicker).equals(preference.getKey())) {
            if (!logoutSwitch.isChecked()) return false;
            stopAutoLoginService();
        }
        return false;
    }
}
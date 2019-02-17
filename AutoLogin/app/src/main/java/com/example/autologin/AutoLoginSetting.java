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

        loginSwitch = (SwitchPreference)findPreference("enableLogin");
        logoutSwitch = (SwitchPreference)findPreference("enableLogout");

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

    private void startAutoLoginService() {
        Intent intent = new Intent(this, AutoLoginService.class);
        intent.putExtra("isLogin", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
    }

    private void stopAutoLoginService() {
        Intent intent = new Intent(this, AutoLoginService.class);
        intent.putExtra("isLogin", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Intent intent = new Intent(this, AutoLoginService.class);
        if (getString(R.string.login_enable_key).equals(preference.getKey())) {
            intent.putExtra("isLogin", true);
            if ((boolean)newValue) {
                loginSwitch.setChecked(true);
                startAutoLoginService();
            } else {
                loginSwitch.setChecked(false);
                stopService(intent);
            }
        }
        else if (getString(R.string.logout_enable_key).equals(preference.getKey())) {
            intent.putExtra("isLogin", false);
            if ((boolean)newValue) {
                logoutSwitch.setChecked(true);
                startAutoLoginService();
            } else {
                logoutSwitch.setChecked(false);
                stopService(intent);
            }
        }
        else if (getString(R.string.login_timepicker).equals(preference.getKey())) {
            if (!loginSwitch.isChecked()) return false;
            intent.putExtra("isLogin", true);
            startAutoLoginService();
        }
        else if (getString(R.string.logout_timepicker).equals(preference.getKey())) {
            if (!logoutSwitch.isChecked()) return false;
            intent.putExtra("isLogin", false);
            stopService(intent);
        }
        return false;
    }
}
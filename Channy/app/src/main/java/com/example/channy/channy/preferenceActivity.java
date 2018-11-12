package com.example.channy.channy;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by qanny on 5/12/2017.
 */

public class preferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference_daily);

        CheckBoxPreference box = (CheckBoxPreference)findPreference("screen");
        EditTextPreference text = (EditTextPreference)findPreference("text");
        ListPreference list = (ListPreference)findPreference("myList");
        box.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.e("reference", "checkbox changed");
                return false;
            }
        });
        text.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(preferenceActivity.this, "text changed", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        list.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(preferenceActivity.this, "listView changed", Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }
}

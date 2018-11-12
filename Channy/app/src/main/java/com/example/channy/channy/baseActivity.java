package com.example.channy.channy;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;

/**
 * Created by channy on 17-12-23.
 */

public class baseActivity extends Activity {
    String TAG = "baseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //Log.e(TAG, "android sdk version is " + Build.VERSION.SDK_INT + " > " + Build.VERSION_CODES.M);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent, null));
        }
        else {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.white, null));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

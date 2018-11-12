package com.example.channy.channy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by channy on 17-12-14.
 */

public class baseDialog extends Dialog {
    public baseDialog(Context context) {
        super(context);
    }

    public baseDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder {
        private Context context;
        public Builder(Context context) {
            this.context = context;
        }

        public baseDialog create() {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_base, null);
            baseDialog dialog = new baseDialog(context);
            dialog.setContentView(view);

            return dialog;
        }
    }
}

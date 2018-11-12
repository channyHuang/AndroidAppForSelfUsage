package com.example.channy.channy;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by channy on 17-12-28.
 */

public class fullscreenVideoView extends VideoView {
    String TAG = fullscreenVideoView.class.getSimpleName();

    public fullscreenVideoView(Context context, AttributeSet attris) {
        super(context, attris);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}

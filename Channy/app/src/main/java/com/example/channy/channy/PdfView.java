package com.example.channy.channy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by channy on 2017/11/17.
 */

public class PdfView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    String TAG = "PdfView";

    SurfaceHolder holder;
    Canvas canvas;
    ArrayList<Bitmap> bitmaps;
    Bitmap bitmap;
    Boolean isDrawing, isOriginScale;
    int originWidth, originHeight;
    int curWidth, curHeight;
    Paint paint;
    int bitmapIndex;
    private float lastx1, lastx2, lasty1, lasty2, lastDis;
    boolean toolShown = false;
    LinearLayout toolLayout;
    Thread thread;
    Context context;

    private enum Type {
        ZOOM,
        MOVE,
        TOOL,
        NEXTPAGE
    }
    private Type type;

    public PdfView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PdfView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        paint = new Paint();
        holder = this.getHolder();
        holder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);

        setOnTouchListener(this);

        bitmapIndex = 0;
        isOriginScale = true;


    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {
        isDrawing = true;

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isDrawing) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (holder) {
                        drawing();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDrawing = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void drawing() {
        if (bitmap == null) return;
        try {
            canvas = holder.lockCanvas();
            if (originWidth == 0) {
                originWidth = canvas.getWidth();
                originHeight = canvas.getHeight();
                curHeight = originHeight;
                curWidth = originWidth;
                Log.e(TAG, "width-height="+originWidth+"-"+originHeight);
            }
            canvas.drawBitmap(bitmap, null, new RectF(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
            holder.unlockCanvasAndPost(canvas);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setBitmaps(ArrayList<Bitmap> images) {
        bitmaps = images;
        bitmap = bitmaps.get(bitmapIndex);
    }

    public void setBitmap(Bitmap bitmapToDraw) {
        bitmap = bitmapToDraw;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        onTouchEvent(event);
        return true;
    }

    private float computeDis (float lastx, float lasty, MotionEvent event) {
        float disx = Math.abs(event.getX() - lastx);
        float disy = Math.abs(event.getY() - lasty);
        float dis = (float) Math.sqrt(disx * disx + disy * disy);
        return dis;
    }

    private void showNextPage(Boolean isNext) {
        if (isNext)
            bitmapIndex = (bitmapIndex + 1 < bitmaps.size() ? bitmapIndex + 1 : bitmapIndex);
        else
            bitmapIndex = (bitmapIndex - 1 >= 0 ? bitmapIndex - 1 : 0);
        setBitmap(bitmaps.get(bitmapIndex));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "down");
                type = Type.TOOL;
                lastx1 = event.getX();
                lasty1 = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                type = Type.ZOOM;
                lastx2 = event.getX(1);
                lasty2 = event.getY(1);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "move");
                if (type == Type.TOOL) {
                    float dis = computeDis(lastx1, lasty1, event);
                    if (dis > 1) {
                        if (isOriginScale) type = Type.NEXTPAGE;
                        else type = Type.MOVE;
                    }
                }
                else if (type == Type.ZOOM && event.getPointerCount() == 2) {
                    float disx = event.getX(0) - event.getX(1);
                    float disy = event.getY(0) - event.getY(1);
                    float dis = (float)Math.sqrt(disx * disx + disy * disy);

                    if (lastDis == 0) lastDis = dis;
                    else {
                        if ((Math.abs(lastDis - dis) < 1) || (dis < lastDis && isOriginScale)) ;
                        else {
                            Log.e(TAG, "scale:");
                            scaleView(dis / lastDis);
                            lastDis = dis;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                lastDis = 0;
                break;
            case MotionEvent.ACTION_UP:
                if (type == Type.NEXTPAGE) {
                    showNextPage(event.getX() < lastx1);
                } else if (type == Type.TOOL) {
                    Log.e(TAG, "tool");

                    LayoutInflater inflater = LayoutInflater.from(context);
                    View parentView = inflater.inflate(R.layout.activity_pdfview, null);
                    toolLayout = (LinearLayout)parentView.findViewById(R.id.toolLayout);

                    if (toolShown) {
                        toolLayout.setVisibility(INVISIBLE);
                        toolShown = false;
                        Log.e(TAG, "dismiss tool");
                    }
                    else {
                        toolLayout.setVisibility(VISIBLE);
                        toolShown = true;
                        Log.e(TAG, "show tool");
                    }
                }
                break;
            default:
                break;
        }

        super.onTouchEvent(event);
        return true;
    }

    private void scaleView(float scale) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = (int)(getHeight() * scale);
        params.width = (int)(getWidth() * scale);
        isOriginScale = false;

        if (params.height < originHeight) {
            params.height = originHeight;
            isOriginScale = true;
        }
        if (params.width < originWidth) {
            params.width = originWidth;
            isOriginScale = true;
        }
        setLayoutParams(params);
    }
}

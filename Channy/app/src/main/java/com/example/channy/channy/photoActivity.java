package com.example.channy.channy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

/**
 * Created by channy on 17-12-24.
 */

public class photoActivity extends baseActivity implements View.OnTouchListener, View.OnClickListener {
    String TAG = photoActivity.class.getSimpleName();
    String imgName = null;
    Button chooseImgBtn;
    Bitmap bitmap, canvasBitmap;
    Canvas canvas;
    Paint paint;
    ImageView imgView;
    TextView pathView;
    int SELECT_IMAGE = 2;
    float downX, downY, upX, upY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo);

        initWidget();

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(photoActivity.this, fileSelectActivity.class);
                startActivityForResult(intent, SELECT_IMAGE);
            }
        });
    }

    private void initWidget() {
        chooseImgBtn = (Button)findViewById(R.id.choosePicBtn);
        pathView = (TextView)findViewById(R.id.pathView);
        imgView = (ImageView) findViewById(R.id.imgView);
        imgView.setOnTouchListener(this);
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                imgName = intent.getStringExtra("path");
                Log.e(TAG, "selected image: " + imgName);
                pathView.setText(imgName);
                if (!imgName.endsWith(".jpg") && !imgName.endsWith(".png") && !imgName.endsWith(".bmp")) return;
                //imgView.setImageURI(Uri.parse(imgName));

                bitmap = BitmapFactory.decodeFile(imgName);
                canvasBitmap = Bitmap.createBitmap(imgView.getWidth(), imgView.getHeight(), bitmap.getConfig());
                canvas = new Canvas(canvasBitmap);
                canvas.drawBitmap(bitmap, new Matrix(), paint);
                imgView.setImageBitmap(canvasBitmap);

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addText:
                break;
            case R.id.addDraw:
                break;
            case R.id.cut:
                break;
            case R.id.blur:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        // TODO Auto-generated method stub
        int action = event.getAction();
        // 判斷不同狀態
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下時記錄座標
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 移動過程中，不斷繪製Line
                upX = event.getX();
                upY = event.getY();
                canvas.drawLine(downX, downY, upX, upY, paint);
                imgView.invalidate();
                downX = upX;
                downY = upY;
                break;
            case MotionEvent.ACTION_UP:
                // 停止時，記錄座標
                upX = event.getX();
                upY = event.getY();
                canvas.drawLine(downX, downY, upX, upY, paint);
                imgView.invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        //返回true表示，一旦事件开始就要继续接受触摸事件
        return true;
    }

    @Override
    public void onClick(View view) {

    }
}

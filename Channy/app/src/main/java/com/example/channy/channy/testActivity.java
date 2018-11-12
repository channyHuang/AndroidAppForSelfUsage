package com.example.channy.channy;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * Created by channy on 18-1-1.
 */

public class testActivity extends Activity implements View.OnClickListener,
        View.OnTouchListener {

    private static final int CHOOSEPICTURE_REQUESTCODE = 0;
    private static final String TAG = "Scrawl";
    // 顯示圖片的組件。
    private ImageView imageView;
    // 選擇圖片的按鈕，和保存圖片的按鈕。
    private Button chooseButton, saveButton;
    // 定義兩個位圖對象，第一個包含了選擇定圖片的縮放版本，第二個是可變的版本，
    // 將第一個位圖對象繪製到第二個位圖對象中，再在其上方繪製（塗鴉）
    private Bitmap bitmap, alteredBitmap;
    // 定義畫布
    private Canvas canvas;
    // 定義畫筆
    private Paint paint;
    // 定義輸入矩陣，該類使之在一幅圖像上應用空間轉換（比如旋轉，評議，縮放，裁剪等）
    private Matrix matrix;
    // 定義按下和停止的位置（x,y）座標
    private float downX = 0, downY = 0, upX = 0, upY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 設置主潔面
        setContentView(R.layout.activity_test);
        // 獲取潔面xml文件相關的View對象
        imageView = (ImageView) findViewById(R.id.imageView);
        chooseButton = (Button) findViewById(R.id.chooseButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        // 給兩個Button設置onClick監聽事件，該類實現了android.view.View.OnClickListener接口
        chooseButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        // 給ImageView對象設置onTouch監聽,該類實現了android.view.View.OnTouchListener接口
        imageView.setOnTouchListener(this);
        // 該開始，設置saveButton為不可用狀態
        saveButton.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        // 因為給不同的Button對象都設置了onClick監聽，所以需要判斷觸發的是哪個Button的監聽事件
        switch (v.getId()) {
            case R.id.chooseButton:
                // 再一次选择图片，設置saveButton為不可用狀態
                if (saveButton.isEnabled())
                    saveButton.setEnabled(false);
                // 使用Intent打開Gallery選擇圖片
                Intent choosePictureIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // 啟動該activity，并在該activity結束返回數據,所以調用startActivityForResult()方法
                startActivityForResult(choosePictureIntent,
                        CHOOSEPICTURE_REQUESTCODE);
                break;
            case R.id.saveButton:
                if (alteredBitmap != null) {
                    Uri imageFileUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    try {
                        OutputStream imageFileOS = getContentResolver()
                                .openOutputStream(imageFileUri);
                        //compress方法，压缩成jpg格式，0-100代表了压缩质量 100质量最好 第三个参数把压缩图片写到输出流
                        alteredBitmap
                                .compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);
                        //保存成功，进行提示
                        Toast.makeText(this, R.string.saveSuccess,
                                Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, R.string.pleaseChooseImage,
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        // 判斷返回是否OK
        if (resultCode == RESULT_OK) {
            // 獲取返回的Uri
            Uri imageFileUri = data.getData();
            // 獲取默認Display，用以得到當前的寬和高
            Display currentDisplay = getWindowManager().getDefaultDisplay();
            int dw = currentDisplay.getWidth();
            Log.i(TAG, currentDisplay.getHeight()+"");
            Log.i(TAG, chooseButton.getHeight()+"chooseButton");
            Log.i(TAG, saveButton.getHeight()+"saveButton");
            Log.i(TAG, imageView.getHeight()+"imageView");
            Log.i(TAG, getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop()+"ID_ANDROID_CONTENT");
            //432 = 48+48+40+16*2+264 默认界面的margin top bottom有16dp title和状态栏40dp 48+48 两个button高度
            int dh = currentDisplay.getHeight() - (48+48+40+16*2);
//                  chooseButton.getHeight()
//                  - saveButton.getHeight();
            // 使用BitmapFactory創建位圖Options
            BitmapFactory.Options bmFactoryOptions = new BitmapFactory.Options();
            // 設置只是獲取圖片的尺寸，並不是真正的解碼圖片
            bmFactoryOptions.inJustDecodeBounds = true;
            // 使用decode*方法創建位圖，getContentResolver()獲取內容提供器
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(imageFileUri), null, bmFactoryOptions);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 計算圖像寬高與當前寬高的比率
            int hRatio = (int) Math.ceil(bmFactoryOptions.outHeight
                    / (float) dh);
            int wRatio = (int) Math
                    .ceil(bmFactoryOptions.outWidth / (float) dw);
            // 判断是按高比率缩放还是宽比例缩放
            if (hRatio > 1 || wRatio > 1) {
                if (hRatio > wRatio) {
                    bmFactoryOptions.inSampleSize = hRatio;
                } else {
                    bmFactoryOptions.inSampleSize = wRatio;
                }
            }
            Log.i(TAG, bmFactoryOptions.outHeight/hRatio+"imageView");
            // 对图像进行真正的解码
            bmFactoryOptions.inJustDecodeBounds = false;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(imageFileUri), null, bmFactoryOptions);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 在加載位圖之後，創建一個可變的位圖對象alteredBitmap，並在其中繪製bitmap對象
            alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), bitmap.getConfig());
            // 使用alteredBitmap作為構造參數，創建canvas
            canvas = new Canvas(alteredBitmap);
            // 創建畫筆
            paint = new Paint();
            // 設置畫筆顏色為Color.WHITE
            paint.setColor(Color.WHITE);
            // 設置畫筆大小
            paint.setStrokeWidth(5);
            // 創建matrix,此處對圖像不進行任何的縮放，旋轉等操作
            matrix = new Matrix();
            // 繪製bitmap
            canvas.drawBitmap(bitmap, matrix, paint);
            // 把alteredBitmap設置到imageview上面
            imageView.setImageBitmap(alteredBitmap);
            // 此時設置saveButton為可用狀態
            saveButton.setEnabled(true);
            imageView.setOnClickListener(this);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
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
                imageView.invalidate();
                downX = upX;
                downY = upY;
                break;
            case MotionEvent.ACTION_UP:
                // 停止時，記錄座標
                upX = event.getX();
                upY = event.getY();
                canvas.drawLine(downX, downY, upX, upY, paint);
                imageView.invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        //返回true表示，一旦事件开始就要继续接受触摸事件
        return true;
    }

}

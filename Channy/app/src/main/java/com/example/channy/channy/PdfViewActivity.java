package com.example.channy.channy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.R.attr.bitmap;

/**
 * Created by channy on 2017/11/16.
 */

public class PdfViewActivity extends Activity {
    String TAG = "PdfViewActivity";

    ArrayList<Bitmap> pageBitmaps;

    public LinearLayout toolLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        PdfView view = (PdfView) findViewById(R.id.view);
        pageBitmaps = new ArrayList<>();

        toolLayout = (LinearLayout)findViewById(R.id.toolLayout);
        //toolLayout.setVisibility(View.INVISIBLE);

        File file = new File(getIntent().getStringExtra("File"));
        try {
            ParcelFileDescriptor descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(descriptor);
            int totalPage = renderer.getPageCount();
            ArrayList<PdfRenderer.Page> pageList = new ArrayList<>();

            for (int i = 0; i < totalPage; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                pageList.add(page);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                pageBitmaps.add(bitmap);

                page.close();
            }
            renderer.close();

            view.setBitmaps(pageBitmaps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        Log.e(TAG, "for test");
        File saveFile = new File(Environment.getExternalStorageDirectory(), "f.jpg");
        try {
            OutputStream stream = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

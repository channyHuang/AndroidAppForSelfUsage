package com.example.channy.channy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by channy on 2017/11/16.
 */

public class PdfActivity extends Activity {
    String TAG = "PdfActivity";
    ArrayList<String> pdfList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        pdfList = getFile(Environment.getExternalStorageDirectory());
        //Log.e(TAG, "file num:" + pdfList.size());

        ListView listView = (ListView)findViewById(R.id.fileList);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pdfList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(PdfActivity.this, PdfViewActivity.class);
                intent.putExtra("File", pdfList.get(i));
                startActivity(intent);
            }
        });
    }

    private ArrayList<String> getFile(File file) {
        //Log.e(TAG, "file:" + file.getAbsolutePath());
        ArrayList<String> list = new ArrayList<>();
        list.clear();
        if (file.isDirectory()) {
            try {
                File[] files = file.listFiles();
                for (File singleFile : files) {
                    list.addAll(getFile(singleFile));
                }
            }catch (NullPointerException e) {
                e.printStackTrace();
            }
        }else if (file.getName().endsWith(".pdf")) {
            try {
                list.add(file.getAbsolutePath());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}

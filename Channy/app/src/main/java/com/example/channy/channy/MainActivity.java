package com.example.channy.channy;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends baseActivity {
    String TAG = "Channy";
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getListData());

        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getListData()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        baseDialog dialog = new baseDialog.Builder(MainActivity.this).create();
                        dialog.show();
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, MusicActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, PdfActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, DatePickerActivity.class);
                        startActivity(intent);
                        break;
                    case 4:
                        intent = new Intent(MainActivity.this, TrafficActivity.class);
                        startActivity(intent);
                        break;
                    case 5:
                        intent = new Intent(MainActivity.this, EventActivity.class);
                        startActivity(intent);
                        break;
                    case 6:
                        intent = new Intent(MainActivity.this, MusicCutActivity.class);
                        startActivity(intent);
                        break;
                    case 7:
                        intent = new Intent(MainActivity.this, photoActivity.class);
                        startActivity(intent);
                        break;
                    case 8:
                        intent = new Intent(MainActivity.this, videoActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        intent = new Intent(MainActivity.this, testActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

    }

    private ArrayList<String> getListData() {
        ArrayList<String> list = new ArrayList<>();
        list.add("TelephoneCall");
        list.add("MusicPlayer");
        list.add("PDFViewer");
        list.add("DatePicker");
        list.add("Traffic");
        list.add("EventPicker");
        list.add("MusicCut");
        list.add("Photo");
        list.add("videoPlayer");
        list.add("test");
        return list;
    }
}

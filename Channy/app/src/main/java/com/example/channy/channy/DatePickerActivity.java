package com.example.channy.channy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by channy on 2017/11/18.
 */

public class DatePickerActivity extends baseActivity {
    private String TAG = DatePickerActivity.class.getSimpleName();

    ArrayList<String> dateList = new ArrayList<>();
    long time = System.currentTimeMillis();
    Date date = new Date(time);
    ListView listView;
    DataBaseUtil dataBaseUtil;
    int columnId = DataBaseUtil.TYPE_BEGINTIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datepicker);

        dataBaseUtil = new DataBaseUtil(getApplicationContext());
        Button addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(DatePickerActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month += 1;
                        ContentValues values = new ContentValues();
                        values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_TYPE], TAG);
                        values.put(DataBaseUtil.TYPE[columnId], year + "_" +month + "_" + dayOfMonth);
                        Cursor cursor = dataBaseUtil.searchItem(new int[]{DataBaseUtil.TYPE_TYPE, columnId}, new String[]{TAG, year + "_" +month + "_" + dayOfMonth});
                        if (cursor != null) {
                            if (cursor.moveToNext()) {
                                Log.e(TAG, "already has same date");
                                return;
                            }
                        }
                        dataBaseUtil.insertItem(values);

                        dateList.add("" + year + " " + month  + " " + dayOfMonth);
                        listView.setAdapter(new ArrayAdapter<String>(DatePickerActivity.this, android.R.layout.simple_spinner_item, dateList));
                    }
                }, (date.getYear() + 1900), date.getMonth() + 1, date.getDate()).show();

            }
        });

        listView = (ListView)findViewById(R.id.dataList);
        getDate();
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dateList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DatePickerActivity.this);
                dialog.setTitle("Delete this date?");
                dialog.setMessage(dateList.get(i));
                final int index = i;
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataBaseUtil.deleteItem(columnId, dateList.get(index));
                        dateList.remove(index);
                        listView.setAdapter(new ArrayAdapter<String>(DatePickerActivity.this, android.R.layout.simple_spinner_item, dateList));
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing;
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getDate() {
        dateList.clear();
        Cursor cursor = dataBaseUtil.searchItem(new int[]{DataBaseUtil.TYPE_TYPE}, new String[]{TAG});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                dateList.add(cursor.getString(columnId));
                Log.e(TAG, "search result = " + cursor.getString(0) + ", " + cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3));
            }
        }
    }

}

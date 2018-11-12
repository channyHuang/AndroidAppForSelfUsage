package com.example.channy.channy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/3/20.
 */

public class fileSelectActivity extends Activity {
    List<File> fileLists;
    TextView path = null;
    ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileselect);

        path = (TextView)findViewById(R.id.path);
        path.setText(Environment.getExternalStorageDirectory() + "");

        Button doneBtn = (Button)findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("path", path.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button cancelBtn = (Button)findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button newBtn = (Button)findViewById(R.id.newBtn);
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(path.getText().toString());
                file.mkdir();

                Intent intent = new Intent();
                intent.putExtra("path", file.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.listview);
        fileLists = new ArrayList<>();
        readList(Environment.getExternalStorageDirectory());
        listView.setAdapter(new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, fileLists));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                readList(fileLists.get(position));
                listView.setAdapter(new ArrayAdapter<File>(fileSelectActivity.this, android.R.layout.simple_list_item_1, fileLists));
            }
        });
    }

    private void readList(File file) {
        try {
            if (file.isDirectory()) {
                fileLists.clear();
                File[] files = file.listFiles();
                for (File everyFile : files) {
                    fileLists.add(everyFile);
                }
                Collections.sort(fileLists);
            } else {
                Intent intent = new Intent();
                intent.putExtra("path", file.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            }
            path.setText(file.getAbsolutePath());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }
}

package com.example.channy.channy;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class videoActivity extends Activity implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    String TAG = "Channy";
    ListView videoListView;
    int curVideo = 0;
    MediaPlayer player = null;
    VideoView videoView;
    MediaController mediaController;
    ArrayList<String> videoList;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        File file = new File(Environment.getExternalStorageDirectory() + "");
        videoList = getVideo(file);

        videoView = (VideoView)findViewById(R.id.videoView);
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        videoListView = (ListView)findViewById(R.id.videoListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, videoList);
        videoListView.setAdapter(adapter);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                curVideo = i;
                videoView.setVisibility(View.VISIBLE);
                videoListView.setVisibility(View.GONE);
                play(videoList.get(i));
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (videoView.getVisibility() == View.VISIBLE) {
            videoView.setVisibility(View.GONE);
            videoListView.setVisibility(View.VISIBLE);
        }
        else super.onBackPressed();
    }

    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        return false;
    }

    public void onCompletion(MediaPlayer player) {
        curVideo = (curVideo + 1) % videoList.size();
        play(videoList.get(curVideo));
    }

    private void play(String videoName) {
        videoView.setVideoURI(Uri.parse(videoName));

        videoView.start();
    }

    private void pause() {
        videoView.pause();
    }

    private void restart() {
        videoView.resume();
    }

    private ArrayList<String> getVideo(File file) {
        ArrayList<String> list = new ArrayList<>();
        list.clear();
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File singleFile : files) {
                    list.addAll(getVideo(singleFile));
                }
            } else if (file.getName().endsWith(".mp4") || file.getName().endsWith(".flv")) {
                try {
                    list.add(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return list;
    }
}
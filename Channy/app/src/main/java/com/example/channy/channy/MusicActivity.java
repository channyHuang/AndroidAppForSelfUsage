package com.example.channy.channy;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by channy on 2017/11/14.
 */

public class MusicActivity extends baseActivity {
    String TAG = MusicActivity.class.getSimpleName();

    private MediaPlayer player = null;
    SeekBar seekBar;
    private int curSong = 0, curTime = 0;
    Timer timer;
    List<String> songlist, folderList;
    TextView timeText, remainTimeText, curSongName;
    Switch repeatSwitch;
    int totalMinute = 0, totalSecond = 0;
    int ADD_FOLDER = 1;
    Button playBtn, nextBtn, addBtn, lastBtn, removeBtn;
    ListView listView;
    ArrayAdapter adapter, folderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        curSongName = (TextView)findViewById(R.id.curSongName);
        repeatSwitch = (Switch)findViewById(R.id.repeatSwitch);
        timeText = (TextView)findViewById(R.id.timeText);
        remainTimeText = (TextView)findViewById(R.id.remainTimeText);
        folderList = new ArrayList<>();
        songlist = new ArrayList<>();
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        playBtn = (Button)findViewById(R.id.playBtn);
        playBtn.setText("Play");
        nextBtn = (Button)findViewById(R.id.nextBtn);
        addBtn = (Button)findViewById(R.id.addFolderBtn);
        removeBtn = (Button)findViewById(R.id.removeFolderBtn);
        lastBtn = (Button)findViewById(R.id.lastBtn);
        listView = (ListView)findViewById(R.id.songListView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songlist);

        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(new MusicPhoneStateistener(), PhoneStateListener.LISTEN_CALL_STATE);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                curSong = i;
                play(songlist.get(curSong), 0);
                playBtn.setText("Pause");
            }
        });

        init();
        initConnect();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, filter);
    }

    void init() {
        getFolderList();
        getAllSong();
        getPlayInfo();
    }

    void initConnect() {
        Log.e(TAG, "initConnect");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int time = (player.getDuration() - seekBar.getProgress() )/ 1000;
                remainTimeText.setText("-" + Integer.toString(time / 60) + ":" + Integer.toString(time % 60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
                int time = seekBar.getProgress() / 1000;
                remainTimeText.setText(Integer.toString(time / 60) + ":" + Integer.toString(time % 60));
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeEmpty()) return;
                if (player == null) {
                    play(songlist.get(curSong), curTime);
                    playBtn.setText("Pause");
                }
                else if (!player.isPlaying()) {
                    restart();
                    playBtn.setText("Pause");
                }
                else {
                    pause();
                    playBtn.setText("Play");
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeEmpty()) return;
                curSong = (curSong + 1) % songlist.size();
                play(songlist.get(curSong), 0);
            }
        });


        lastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeEmpty()) return;
                curSong = (curSong - 1 + songlist.size()) % songlist.size();
                play(songlist.get(curSong), 0);
            }
        });


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicActivity.this, fileSelectActivity.class);
                startActivityForResult(intent, ADD_FOLDER);
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View layout = MusicActivity.this.getLayoutInflater().inflate(R.layout.popup_folderreomve, null);
                final PopupWindow window = new PopupWindow(layout, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                final ListView folderView = (ListView)layout.findViewById(R.id.folderView);
                folderAdapter = new ArrayAdapter(MusicActivity.this, android.R.layout.simple_list_item_1, folderList);
                folderView.setAdapter(folderAdapter);
                folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        folderList.remove(i);
                        if (window.isShowing()) window.dismiss();

                        getAllSong();
                        adapter.notifyDataSetChanged();

                        saveFolderList();
                    }
                });

                window.setFocusable(true);
                window.setOutsideTouchable(true);
                window.update();
                window.showAsDropDown(removeBtn);
            }
        });
    }

    boolean judgeEmpty() {
        if (songlist.size() == 0) return true;
        if (curSong >= songlist.size()) return true;
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ADD_FOLDER) {
                // add folder
                String folder = data.getStringExtra("path");
                Toast.makeText(this, "Add folder:" + folder, Toast.LENGTH_LONG).show();
                File file = new File(folder);
                if (file.isFile()) return;
                folderList.add(folder);

                getAllSong();
                adapter.notifyDataSetChanged();

                saveFolderList();
            }
        }
    }

    private void play(String name, final int beginTime) {
        killPlayer();
        if (judgeEmpty()) return;

        try {
            curSongName.setText(curSong + ":" + songlist.get(curSong));

            //player = new MediaPlayer();
            //player.setDataSource(name);
            player = MediaPlayer.create(this, Uri.parse(name));
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(player.getDuration());

                    Log.e(TAG, "beginTime = " + beginTime);
                    player.seekTo(beginTime);
                    seekBar.setProgress(beginTime);

                    totalMinute = player.getDuration() / 1000 / 60;
                    totalSecond = player.getDuration() / 1000 % 60;
                    timeText.setText(Integer.toString(totalMinute) + ":" + Integer.toString(totalSecond));
                    timer = new Timer();
                    TimerTask timertask = new TimerTask() {
                        @Override
                        public void run() {
                            seekBar.setProgress(player.getCurrentPosition());
                        }
                    };
                    timer.schedule(timertask, 0, 10);

                    player.start();
                }
            });

            //player.prepareAsync();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (judgeEmpty()) return;
                    if (!repeatSwitch.isChecked()) curSong = (curSong + 1) % songlist.size();
                    play(songlist.get(curSong), 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        if (player!=null) {
            player.pause();
        }
    }

    private void restart() {
        if (player!=null && !player.isPlaying()) {
            player.seekTo(seekBar.getProgress());
            player.start();
        }
        else {
            play(songlist.get(0), 0);
        }
    }

    private void killPlayer() {
        if (timer!=null) {
            timer.cancel();
            timer = null;
        }
        if (player!=null) {
            try {
                player.release();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        savePlayInfo();
        killPlayer();

        unregisterReceiver(headsetReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "keycode = " + keyCode + ", event = " + event);
        if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode || KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
            if (event.getRepeatCount() == 0) {
                Log.e(TAG, "headsethook, repeatCount = 0");
                if (judgeEmpty()) return true;
                if (player == null) {
                    play(songlist.get(curSong), curTime);
                    playBtn.setText("Pause");
                }
                else if (!player.isPlaying()) {
                    restart();
                    playBtn.setText("Pause");
                }
                else {
                    pause();
                    playBtn.setText("Play");
                }
            } else {
                Log.e(TAG, "headsethook, repeatCount > 0");
            }
            return super.onKeyDown(keyCode, event);
        } else if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
            if (judgeEmpty()) return super.onKeyDown(keyCode, event);
            curSong = (curSong + 1) % songlist.size();
            play(songlist.get(curSong), 0);
        } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {

        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("state", 0) == 0) {
                pause();
                playBtn.setText("Play");
            } else {
                //
            }
        }
    };

    private void savePlayInfo() {
        Log.e(TAG, "savePlayInfo");
        SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastId", curSong);
        editor.putInt("lastTime", seekBar.getProgress());
        editor.commit();
    }

    private void getPlayInfo() {
        Log.e(TAG, "getPlayInfo");
        SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
        curSong = preferences.getInt("lastId", 0);
        curTime = preferences.getInt("lastTime", 0);
    }

    private void getFolderList() {
        Log.e(TAG, "getFolderList");
        Set<String> set = new ArraySet<>();
        folderList.clear();
        SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
        set = preferences.getStringSet("path", set);
        for (String folder : set) {
            folderList.add(folder);
        }
        Collections.sort(folderList);
        Log.e(TAG, "folderList.size = " + folderList.size());
    }

    private void saveFolderList() {
        Log.e(TAG, "saveFolderList");
        Set<String> set = new ArraySet<>();
        for (String folder : folderList) {
            set.add(folder);
        }

        SharedPreferences preferences = getSharedPreferences("path", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("path", set);
        editor.commit();
    }

    private void getAllSong() {
        songlist.clear();

        for (String folder : folderList) {
            try {
                songlist.addAll(getSong(new File(folder)));
            }catch (NullPointerException e) {
                e.printStackTrace();
            }
            Collections.sort(songlist);
        }

    }

    private ArrayList<String> getSong(File file) {

        ArrayList<String> list = new ArrayList<>();
        list.clear();
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File singleFile : files) {
                    list.addAll(getSong(singleFile));
                }
            } else if (file.getName().endsWith(".mp3")) {
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

    private class MusicPhoneStateistener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.e(TAG, "ringing");
                    pause();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.e(TAG, "idle");
                    //restart();
                    break;
                default:
                    break;
            }
        }
    }
}

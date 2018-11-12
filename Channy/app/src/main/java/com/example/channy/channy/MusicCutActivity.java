package com.example.channy.channy;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by channy on 2017/11/26.
 */

public class MusicCutActivity extends Activity {
    String TAG = "MusicCutActivity";

    String songName;
    Timer timer;
    int totalMinute = 0, totalSecond = 0;
    int beginTime, endTime;
    int SELECT_SONG = 1;
    Button playBtn;
    Button getBeginBtn, getEndBtn;
    MediaPlayer player = null;
    SeekBar seekBar;
    TextView remainTimeText, timeText, pathView;
    TextView beginView, endView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiccut);

        initWidget();
        initConnect();



        Button changeBtn = (Button)findViewById(R.id.selectBtn);
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicCutActivity.this, fileSelectActivity.class);
                startActivityForResult(intent, SELECT_SONG);
            }
        });

        playBtn = (Button)findViewById(R.id.playBtn);
        playBtn.setText("Play");
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) {
                    if (songName == null) return;
                    play(songName);
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

        Button cutBtn = (Button)findViewById(R.id.cutBtn);
        cutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cut
                try {
                    if (beginTime >= endTime) return;

                    String fenLiData = devideData(songName);
                    final List<Integer> list = initMP3Frame(fenLiData);
                    CutMusic(fenLiData, list, beginTime, endTime);
                    final File file = new File(fenLiData);
                    if (file.exists()) file.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initWidget() {
        beginView = (TextView)findViewById(R.id.beginView);
        endView = (TextView)findViewById(R.id.endView);

        songName = null;

        remainTimeText = (TextView)findViewById(R.id.remainTimeText);
        timeText = (TextView)findViewById(R.id.timeText);

        pathView = (TextView)findViewById(R.id.pathView);

        seekBar = (SeekBar)findViewById(R.id.seekBar);

        beginTime = 0;
        endTime = 0;

        getBeginBtn = (Button)findViewById(R.id.getBeginTime);
        getEndBtn = (Button)findViewById(R.id.getEndTime);
    }

    private void initConnect() {
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
                remainTimeText.setText(changeTime(time));
            }
        });

        getBeginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginTime = player.getCurrentPosition() / 1000;
                beginView.setText(changeTime(beginTime));
            }
        });
        getEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTime = player.getCurrentPosition() / 1000;
                endView.setText(changeTime(endTime));
            }
        });
    }

    private String changeTime(int time) {
        return Integer.toString(time / 60) + ":" + Integer.toString(time % 60);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_SONG) {
                songName = intent.getStringExtra("path");
                Log.e(TAG, "selected song: " + songName);
                pathView.setText(songName);
                if (!songName.endsWith(".mp3")) return;

                beginTime = 0;
                endTime = 0;
                beginView.setText("0");
                endView.setText("0");
                play(songName);
            }
        }
    }

    @Override
    protected void onDestroy() {
        killPlayer();
        super.onDestroy();
    }

    private void play(String name) {
        killPlayer();

        try {
            //player = new MediaPlayer();
            //player.setDataSource(name);
            player = MediaPlayer.create(this, Uri.parse(name));

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(player.getDuration());
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
                    //Toast.makeText(MusicActivity.this, "complete", Toast.LENGTH_LONG).show();
                    play(songName);
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
            play(songName);
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

    private void CutMusic(String path, List<Integer> list, int startTime, int endTime) {
        File file = new File(path);
        long seekStart = 0, seekStop = 0;
        int start = (int) (startTime / 0.026), stop = (int) (endTime / 0.026);
        for (int i = 0; i < start; i++) {
            seekStart += list.get(i);
            seekStop += list.get(i);
        }
        for (int i = start; i < stop; i++) {
            seekStop += list.get(i);
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(seekStart);
            File file1 = new File(path + "_cuting.mp3");
            FileOutputStream out = new FileOutputStream(file1);
            byte[] bs = new byte[(int) (seekStop - seekStart)];
            raf.read(bs);
            out.write(bs);
            raf.close();
            out.close();
            File filed = new File(path);
            if (filed.exists())
                filed.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<Integer> initMP3Frame(String path)  {
        File file = new File(path);
        List<Integer> list = new ArrayList<>();
        int framSize = 0;
        RandomAccessFile rad = null;
        try {
            rad = new RandomAccessFile(file, "rw");
            while (framSize < file.length()) {
                byte[] head = new byte[4];
                rad.seek(framSize);
                rad.read(head);
                int bitRate = getBitRate((head[2] >> 4) & 0x0f) * 1000;
                int sampleRate = getsampleRate((head[2] >> 2) & 0x03);
                int paing = (head[2] >> 1) & 0x01;
                if (bitRate == 0 || sampleRate == 0) return null;
                int len = 144 * bitRate / sampleRate + paing;
                list.add(len);// 将数据帧的长度添加进来
                framSize += len;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static int getBitRate(int i) {
        int a[] = {0,32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224,
                256, 320,0 };
        return a[i];
    }

    private static int getsampleRate(int i) {
        int a[] = { 44100, 48000, 32000,0 };
        return a[i];
    }

    public static String devideData(String path) throws IOException {
        File file = new File(path);// original file
        File file1 = new File(path + "a");// after devide ID3V2, will be delete later
        File file2 = new File(path + "b");// after devide id3v1
        RandomAccessFile rf = new RandomAccessFile(file, "rw");
        FileOutputStream fos = new FileOutputStream(file1);
        byte ID3[] = new byte[3];
        rf.read(ID3);
        String ID3str = new String(ID3);
        // devide ID3v2
        if (ID3str.equals("ID3")) {
            rf.seek(6);
            byte[] ID3size = new byte[4];
            rf.read(ID3size);
            int size1 = (ID3size[0] & 0x7f) << 21;
            int size2 = (ID3size[1] & 0x7f) << 14;
            int size3 = (ID3size[2] & 0x7f) << 7;
            int size4 = (ID3size[3] & 0x7f);
            int size = size1 + size2 + size3 + size4 + 10;
            rf.seek(size);
            int lens = 0;
            byte[] bs = new byte[1024*4];
            while ((lens = rf.read(bs)) != -1) {
                fos.write(bs, 0, lens);
            }
            fos.close();
            rf.close();
        } else {// copy
            int lens = 0;
            rf.seek(0);
            byte[] bs = new byte[1024*4];
            while ((lens = rf.read(bs)) != -1) {
                fos.write(bs, 0, lens);
            }
            fos.close();
            rf.close();
        }
        RandomAccessFile raf = new RandomAccessFile(file1, "rw");
        byte TAG[] = new byte[3];
        raf.seek(raf.length() - 128);
        raf.read(TAG);
        String tagstr = new String(TAG);
        if (tagstr.equals("TAG")) {
            FileOutputStream fs = new FileOutputStream(file2);
            raf.seek(0);
            byte[] bs=new byte[(int)(raf.length()-128)];
            raf.read(bs);
            fs.write(bs);
            raf.close();
            fs.close();
        } else {// copy to file2
            FileOutputStream fs = new FileOutputStream(file2);
            raf.seek(0);
            byte[] bs = new byte[1024*4];
            int len = 0;
            while ((len = raf.read(bs)) != -1) {
                fs.write(bs, 0, len);
            }
            raf.close();
            fs.close();
        }
        if (file1.exists()) {
            file1.delete();
        }
        return file2.getAbsolutePath();
    }

    public static String combineMp3(String path,String path1,String name) throws IOException{
        File file=new File(devideData(path));
        File file1=new File(devideData(path1));
        String luJing= Environment.getExternalStorageDirectory()+"/hebing/";
        File f=new File(luJing);
        f.mkdirs();
        //生成处理后的文件
        File file2=new File(luJing+name+"hebing.mp3");
        FileInputStream in=new FileInputStream(file);
        FileOutputStream out=new FileOutputStream(file2);
        byte bs[]=new byte[1024*4];
        int len=0;
        //先读第一个
        while((len=in.read(bs))!=-1){
            out.write(bs,0,len);
        }
        in.close();
        out.close();
        //再读第二个
        in=new FileInputStream(file1);
        out=new FileOutputStream(file2,true);//在文件尾打开输出流
        len=0;
        byte bs1[]=new byte[1024*4];
        while((len=in.read(bs1))!=-1){
            out.write(bs1,0,len);
        }
        in.close();
        out.close();
        if(file.exists())file.delete();
        if(file1.exists())file1.delete();
        return file2.getAbsolutePath();
    }
}

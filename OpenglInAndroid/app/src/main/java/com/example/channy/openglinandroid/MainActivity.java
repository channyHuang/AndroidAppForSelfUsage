package com.example.channy.openglinandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements View.OnClickListener {
    @BindView(R.id.ansBtn1) RadioButton ansBtn1;
    @BindView(R.id.ansBtn2) RadioButton ansBtn2;
    @BindView(R.id.ansBtn3) RadioButton ansBtn3;
    @BindView(R.id.ansBtn4) RadioButton ansBtn4;
    @BindView(R.id.testWhatBtn) Spinner testWhatBtn;
    @BindView(R.id.givenWhatBtn) Spinner givenWhatBtn;
    @BindView(R.id.wordView) TextView wordView;
    @BindView(R.id.totalWordView) TextView totalWordView;
    @BindView(R.id.showAnsBtn) Switch showAnsBtn;
    @BindView(R.id.wordSizeView) TextView wordSizeView;


    String TAG = MainActivity.class.getSimpleName();
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    DataBaseUtil dataBaseUtil;
    boolean isShowAnswer = false;
    ArrayList<String> what = new ArrayList<>();
    String testWhat, givenWhat;
    ArrayList<Word> wordList = new ArrayList<>();
    ArrayList<Integer> idxInWord = new ArrayList<>();
    Integer curIdx = 0;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                showWord();
                super.handleMessage(msg);
            }
        };

        showAnsBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isShowAnswer = isChecked;
            }
        });
        getWhat();

        ArrayAdapter<String> testAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, what);
        testWhatBtn.setAdapter(testAdapter);
        testWhatBtn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                  @Override
                                                  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                      testWhat = what.get(position);
                                                  }

                                                  @Override
                                                  public void onNothingSelected(AdapterView<?> parent) {

                                                  }
                                              });

        ArrayAdapter<String> givenAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, what);
        givenWhatBtn.setAdapter(givenAdapter);
        givenWhatBtn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                givenWhat = what.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dataBaseUtil = new DataBaseUtil(getApplicationContext());
        /*
        GLSurfaceView.Renderer render=new GLRender();

        GLSurfaceView gview=new GLSurfaceView(this);
        gview.setRenderer(render);
        setContentView(gview);*/
        //readDb();
        ansBtn1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAnswer();
                }
            }
        });
        ansBtn2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAnswer();
                }
            }
        });
        ansBtn3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAnswer();
                }
            }
        });
        ansBtn4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAnswer();
                }
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @OnClick({R.id.aboutBtn, R.id.readFileBtn, R.id.deleteWordBtn, R.id.readDbBtn})
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.aboutBtn:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Come on");
                dialog.show();
                break;
            case R.id.readFileBtn:
                readFile();
                break;
            case R.id.deleteWordBtn:
                deleteWord();
                break;
            case R.id.readDbBtn:
                readDb();
                break;
            default:
                break;
        }
    }

    private class Word {
        String word;
        String mean;
        String prononce;
        String type;
        int frequency;

        Word(String _word, String _mean, String _prononce, String _type, int _frequency) {
            word = _word;
            mean = _mean;
            prononce = _prononce;
            type = _type;
            frequency = _frequency;
        }
        String getWord() {
            return word;
        }
        String getMean() {return mean;}
        String getPrononce() {return prononce;}
        String getType() {return type;}
        int getFrequency() {return frequency;}
    }

    private void readFile() {
        dataBaseUtil.deleteAll(this);
        dataBaseUtil = new DataBaseUtil(this);
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.words);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String strLine = null;
            while ((strLine = reader.readLine()) != null) {
                String[] words = strLine.split("\\s+");
                if (words.length < 3) {
                    continue;
                }
				String[] word = words[0].replace("(", " ").replace(")", " ").split("\\s+");
                ContentValues values = new ContentValues();
                values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_PRONONCE], word[0]);
				values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_TYPE], words[1]);
                values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_MEAN], words[2]);
                if (word.length == 1) {
                    values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_WORD], word[0]);                   
                } else {
                    values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_WORD], word[1]);
                }
                dataBaseUtil.insertItem(values);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "read file error");
            e.printStackTrace();
        }
    }

    private void readDb() {
        wordList.clear();
        Cursor cursor = dataBaseUtil.searchAll();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Word item = new Word(cursor.getString(DataBaseUtil.TYPE_WORD), cursor.getString(DataBaseUtil.TYPE_MEAN), cursor.getString(DataBaseUtil.TYPE_PRONONCE), cursor.getString(DataBaseUtil.TYPE_TYPE), cursor.getInt(DataBaseUtil.TYPE_FREQUENCY));
                wordList.add(item);
            }
        }

        wordSizeView.setText(String.valueOf(wordList.size()));
        if (wordList.size() >= 4) {
            showWord();
        }
    }

    private void deleteWord() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Delete Word");
        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dataBaseUtil.deleteItem(DataBaseUtil.TYPE_WORD, wordList.get(idxInWord.get(curIdx)).getWord());
                wordList.remove(idxInWord);
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }

    private void showWord() {
        ansBtn1.setChecked(false);
        ansBtn1.setTextColor(Color.BLACK);
        ansBtn2.setChecked(false);
        ansBtn2.setTextColor(Color.BLACK);
        ansBtn3.setChecked(false);
        ansBtn3.setTextColor(Color.BLACK);
        ansBtn4.setChecked(false);
        ansBtn4.setTextColor(Color.BLACK);
        totalWordView.setText("");
        int i = 0;
        Random random = new Random();
        idxInWord.clear();
        while (idxInWord.size() < 4) {
            Integer idx = random.nextInt(wordList.size());
            for (i = 0; i < idxInWord.size(); i++) {
                if (idx != idxInWord.get(i)) continue;
            }
            if (i == idxInWord.size()) {
                idxInWord.add(idx);
            }
        }
        curIdx = random.nextInt(4);
        switch(testWhat) {
            case "word":
                ansBtn1.setText(wordList.get(idxInWord.get(0)).getWord());
                ansBtn2.setText(wordList.get(idxInWord.get(1)).getWord());
                ansBtn3.setText(wordList.get(idxInWord.get(2)).getWord());
                ansBtn4.setText(wordList.get(idxInWord.get(3)).getWord());
                break;
            case "prononce":
                ansBtn1.setText(wordList.get(idxInWord.get(0)).getPrononce());
                ansBtn2.setText(wordList.get(idxInWord.get(1)).getPrononce());
                ansBtn3.setText(wordList.get(idxInWord.get(2)).getPrononce());
                ansBtn4.setText(wordList.get(idxInWord.get(3)).getPrononce());
                break;
            case "mean":
                ansBtn1.setText(wordList.get(idxInWord.get(0)).getMean());
                ansBtn2.setText(wordList.get(idxInWord.get(1)).getMean());
                ansBtn3.setText(wordList.get(idxInWord.get(2)).getMean());
                ansBtn4.setText(wordList.get(idxInWord.get(3)).getMean());
                break;
            default:
                break;
        }
        switch(givenWhat) {
            case "word":
                wordView.setText(wordList.get(idxInWord.get(curIdx)).getWord());
                break;
            case "prononce":
                wordView.setText(wordList.get(idxInWord.get(curIdx)).getPrononce());
                break;
            case "mean":
                wordView.setText(wordList.get(idxInWord.get(curIdx)).getMean());
                break;
            default:
                break;
        }
        if (isShowAnswer) {
            showAnswer();
        }
    }

    private void showAnswer() {
        switch(curIdx) {
            case 0:
                ansBtn1.setTextColor(Color.GREEN);
                break;
            case 1:
                ansBtn2.setTextColor(Color.GREEN);
                break;
            case 2:
                ansBtn3.setTextColor(Color.GREEN);
                break;
            case 3:
                ansBtn4.setTextColor(Color.GREEN);
                break;
            default:
                break;
        }
        totalWordView.setText(wordList.get(idxInWord.get(curIdx)).getWord() + " " + wordList.get(idxInWord.get(curIdx)).getPrononce() + " " + wordList.get(idxInWord.get(curIdx)).getMean() + " "  + wordList.get(idxInWord.get(curIdx)).getType()   );
        totalWordView.setTextColor(Color.RED);

        setTimer();
    }

    private void setTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 4000);
    }

    private void getWhat() {
        what.add("mean");
        what.add("prononce");
        what.add("word");
    }
}

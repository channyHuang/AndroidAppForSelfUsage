package com.example.channy.channy;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by channy on 2017/11/26.
 */

public class EventActivity extends baseActivity {
    String TAG = EventActivity.class.getSimpleName();
    ListView listView;
    ArrayList<EventItem> eventList = new ArrayList<EventItem>();
    Button addBtn;
    DataBaseUtil dataBaseUtil;
    long time = System.currentTimeMillis();
    Date date = new Date(time);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        dataBaseUtil = new DataBaseUtil(getApplicationContext());
        listView = (ListView)findViewById(R.id.eventList);
        getItem();

        final EventAdapter adapter = new EventAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(EventActivity.this);
                dialog.setTitle("Delete this event?");
                dialog.setMessage(eventList.get(i).getEvent());
                final int index = i;
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataBaseUtil.deleteItem(DataBaseUtil.TYPE_EVENT, eventList.get(index).getEvent());
                        eventList.remove(index);
                        listView.setAdapter(new EventAdapter());
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

        addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View layout = EventActivity.this.getLayoutInflater().inflate(R.layout.popup_event, null);

                final EditText eventText = layout.findViewById(R.id.eventEdit);
                final TextView btPicker = layout.findViewById(R.id.btPicker);
                final TextView etPicker = layout.findViewById(R.id.etPicker);
                final TextView bdPicker = layout.findViewById(R.id.bdPicker);
                final TextView edPicker = layout.findViewById(R.id.edPicker);
                btPicker.setClickable(true);
                etPicker.setClickable(true);
                bdPicker.setClickable(true);
                edPicker.setClickable(true);
                bdPicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DatePickerDialog(EventActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                bdPicker.setText(year + "_" +month + "_" + dayOfMonth);
                            }
                        }, (date.getYear() + 1900), date.getMonth(), date.getDate()).show();
                    }
                });
                edPicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DatePickerDialog(EventActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                edPicker.setText(year + "_" +month + "_" + dayOfMonth);
                            }
                        }, (date.getYear() + 1900), date.getMonth(), date.getDate()).show();
                    }
                });

                btPicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                btPicker.setText(hourOfDay + "_" + minute);
                            }
                        }, date.getHours(), date.getMinutes(), true).show();
                    }
                });
                etPicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                etPicker.setText(hourOfDay + "_" + minute);
                            }
                        }, date.getHours(), date.getMinutes(), true).show();
                    }
                });

                AlertDialog.Builder dialog = new AlertDialog.Builder(EventActivity.this).setTitle("Add event").setView(layout);
                dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContentValues values = new ContentValues();
                        values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_TYPE], TAG);
                        values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_BEGINTIME], bdPicker.getText().toString() + btPicker.getText().toString());
                        values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_ENDTIME], edPicker.getText().toString() + etPicker.getText().toString());
                        values.put(DataBaseUtil.TYPE[DataBaseUtil.TYPE_EVENT], eventText.getText().toString());
                        dataBaseUtil.insertItem(values);

                        eventList.add(new EventItem(bdPicker.getText().toString() + btPicker.getText().toString(), edPicker.getText().toString() + etPicker.getText().toString(), eventText.getText().toString()));
                        listView.setAdapter(new EventAdapter());
                    }
                });
                dialog.setNegativeButton("Cancel", null);
                dialog.show();

            }
        });
    }


    class EventAdapter extends BaseAdapter {
        EventItemView itemView;
        @Override
        public int getCount() {
            return eventList.size();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(EventActivity.this).inflate(R.layout.list_item, null);

                itemView = new EventItemView();
                itemView.beginTime = (TextView)convertView.findViewById(R.id.beginTime);
                itemView.endTime = (TextView)convertView.findViewById(R.id.endTime);
                itemView.event = (TextView) convertView.findViewById(R.id.event);

                convertView.setTag(itemView);
            }
            else {
                itemView = (EventItemView) convertView.getTag();
            }

            itemView.beginTime.setText(eventList.get(position).getBeginTime());
            itemView.endTime.setText(eventList.get(position).getEndTime());
            itemView.event.setText(eventList.get(position).getEvent());

            return convertView;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public Object getItem(int position) {
            return eventList.get(position);
        }
    }

    class EventItemView {
        TextView beginTime, endTime, event;
    }

    class EventItem {
        private String beginTime, endTime;
        private String event;

        EventItem(String beginT, String endT, String e) {
            beginTime = beginT;
            endTime = endT;
            event = e;
        }

        public String getBeginTime() {return beginTime;}
        public String getEndTime() {return endTime;}
        public String getEvent() {return event;}

        public void setBeginTime(String t) {beginTime = t;}
        public void setEndTime(String t) {endTime = t;}
        public void setEvent(String e) {event = e;}
    }

    private void getItem() {
        eventList.clear();
        Cursor cursor = dataBaseUtil.searchItem(new int[]{DataBaseUtil.TYPE_TYPE}, new String[]{TAG});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                EventItem item = new EventItem(cursor.getString(DataBaseUtil.TYPE_BEGINTIME), cursor.getString(DataBaseUtil.TYPE_ENDTIME), cursor.getString(DataBaseUtil.TYPE_EVENT));
                eventList.add(item);
                Log.e(TAG, "search result = " + cursor.getString(0) + ", " + cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3));
            }
        }
    }
}

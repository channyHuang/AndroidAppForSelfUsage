package com.example.channy.channy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;

/**
 * Created by channy on 18/03/18.
 */

public class DataBaseUtil extends SQLiteOpenHelper{
    private static String TAG = DataBaseUtil.class.getSimpleName();

    private static DataBaseUtil instance = null;

    SQLiteDatabase db;
    private static String DBName = "channy.db";
    private static String tableName = "EventTable";
    private String createTableSql = "create table " + tableName + "(" +
            "id integer primary key autoincrement," +
            "type varchar(20)," +
            "event varchar(180)," +
            "beginTime varchar(12)," +
            "endTime varchar(12)," +
            "timeStamp int)";
    public static String[] TYPE = {"id", "type", "event", "beginTime", "endTime", "timeStamp"};

    public static int TYPE_ID = 0;
    public static int TYPE_TYPE = 1;
    public static int TYPE_EVENT = 2;
    public static int TYPE_BEGINTIME = 3;
    public static int TYPE_ENDTIME = 4;
    public static int TYPE_TIMESTAMP = 5;

    static synchronized DataBaseUtil getInstance(Context context) {
        if (instance == null) {
            instance = new DataBaseUtil(context);
        }
        return instance;
    }

    DataBaseUtil(Context context) {
        //db = SQLiteDatabase.openOrCreateDatabase("Channy", null);
        super(context, DBName, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL(createTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void insertItem(ContentValues values) {
        if (db == null) {
            db = getWritableDatabase();
        }
        db.insert(tableName, null, values);
    }

    public void deleteItem(int searchColumn, String expectValue) {
        if (db == null) {
            db = getWritableDatabase();
        }
        db.delete(tableName, TYPE[searchColumn] + "=?", new String[]{expectValue});
    }

    public void deleteItem(int id) {
        if (db == null) {
            db = getWritableDatabase();
        }
        db.delete(tableName, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor searchAll() {
        Log.e(TAG, "search all data in database");
        if (db == null) {
            db = getReadableDatabase();
        }
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor searchItem(int[] searchColumn, String[] expectValue) {
        Log.e(TAG, "search one column in database");
        if (db == null) {
            db = getReadableDatabase();
        }
        String column, columnTail;
        if (searchColumn.length == 1) column = TYPE[searchColumn[0]] + "=?";
        else {
            column = "(";
            columnTail = "(";
            boolean begin = true;
            for (int col = 0; col < searchColumn.length; col++) {
                if (!begin) {
                    column = column + ",";
                    columnTail = columnTail + ",";
                }
                begin = false;
                column = column + TYPE[searchColumn[col]];
                columnTail = columnTail + "?";
            }
            column = column + ")=" + columnTail + ")";
        }

        Cursor cursor = db.query(tableName, null,  column, expectValue, null, null, null);
        return cursor;
    }

    public void deleteAll(Context context) {
        context.deleteDatabase(DBName);
    }
}

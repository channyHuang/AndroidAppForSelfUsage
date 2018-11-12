package com.example.channy.channy;

/**
 * Created by channy on 18-3-25.
 */

public class EventUtil {
    private static String TAG = "huang";

    private int id;
    private String type;
    private String event;
    private String date;
    private String timeStamp;

    EventUtil() {}
    EventUtil(int _id, String _type, String _event, String _date, String _timeStamp) {
        id = _id;
        type = _type;
        event = _event;
        date = _date;
        timeStamp = _timeStamp;
    }
}

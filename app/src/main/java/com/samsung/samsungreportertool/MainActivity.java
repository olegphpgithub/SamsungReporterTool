package com.samsung.samsungreportertool;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import java.util.Calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Debug.log("start 001");
        getCallDetails();
        Debug.log("start 002");
    }


    private void getCallDetails() {
        String[] projection = new String[] {
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        try {

            // String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String today = formatter.format(date);

            String selection = null;
            String []selectionArgs = null;

            selection = CallLog.Calls.DATE + " = ? ";
            selectionArgs = new String[] {today};
            Debug.log("a", new Date());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;
            String st = new String("%d - %d - %d");
            st = String.format(st, year, month, day);
            Debug.log(st, new Date());

            Cursor cursor =  this.getApplicationContext().getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                CallLog.Calls.DATE + " >= ?",
                new String[] { createDate(year, month, day).toString()},
                null
            );

            Debug.log("b");
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                String type = cursor.getString(3);
                String time = cursor.getString(4);
                String duration = cursor.getString(5);
                Debug.log(number + " | " + time + " | " + type + " = " + name + " ! " + duration);
            }
            cursor.close();

        } catch (Exception e) {
            Debug.dumpException(e);
        }
        Debug.log("zz");
    }

    public static Long createDate(int year, int month, int day)
    {
        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month, day);

        return calendar.getTimeInMillis();

    }

}

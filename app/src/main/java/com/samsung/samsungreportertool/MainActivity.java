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
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
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
            Debug.log("a");
            Cursor cursor =  this.getApplicationContext().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    CallLog.Calls.DATE + " >= ?",
                    new String[] { createDate(2020,1,15).toString()},
                    null);
            Debug.log("b");
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String number = cursor.getString(1);
                String type = cursor.getString(2); // https://developer.android.com/reference/android/provider/CallLog.Calls.html#TYPE
                String time = cursor.getString(3); // epoch time - https://developer.android.com/reference/java/text/DateFormat.html#parse(java.lang.String
                Debug.log(number + " | " + time);
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

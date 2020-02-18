package com.samsung.samsungreportertool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.Calendar;
import java.util.TimerTask;
import java.lang.reflect.Method;

public class TBroadcastReceiver extends BroadcastReceiver {

    private static java.lang.String incomingNumber;
    private static final String TAG = "Phone call";
    private static TelephonyManager telephony;
    private static Context m_context;

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

//    private static String larisa_number = "9212168346";
//    private static int larisa_limit = 555;

    private static String larisa_number = "2424";
    private static int larisa_limit = 555;

    @Override
    public void onReceive(Context context, Intent intent) {

        Timer myTimer;
        telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        m_context = context;

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
//            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Debug.log("android.intent.action.NEW_OUTGOING_CALL");
            Debug.log(savedNumber);
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }

            onCallStateChanged(context, state, number);
        }


//        try {
//
//            Bundle bundle = intent.getExtras();
//
//            if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
//                Debug.log("ACTION_PHONE_STATE_CHANGED");
//                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//                if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
//                    Debug.log("EXTRA_STATE_RINGING");
//                }
//                if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                    Debug.log("EXTRA_STATE_OFFHOOK", new Date());
//                    myTimer = new Timer();
//                    myTimer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            TimerMethod();
//                        }
//
//                    }, 10000);
//                }
//                if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
//                    Debug.log("EXTRA_STATE_IDLE");
//                }
//            }
//
//        } catch (Exception e) {
//            Debug.dumpException(e);
//        }
//

    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Debug.log("onIncomingCallStarted", new Date());
        Debug.log(number);
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Debug.log("onOutgoingCallStarted", new Date());
        Debug.log(number);
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Debug.log("onIncomingCallEnded", new Date());
        Debug.log(number);
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Debug.log("onOutgoingCallEnded", new Date());
        Debug.log(number);
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
        Debug.log("onMissedCall");
        Debug.log(number);
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

    private void TimerMethod() {
        try {
            Class<?> c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(telephony);
            Class<?> telephonyServiceClass = Class.forName(telephonyService.getClass().getName());
            Method endCallMethod = telephonyServiceClass.getDeclaredMethod("endCall");
            endCallMethod.setAccessible(true);
            endCallMethod.invoke(telephonyService);
        } catch (Exception e) {
            Debug.dumpException(e);
        }
    }

    public static Long createDate(int year, int month, int day, int hour)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTimeInMillis();
    }


    private int getDurationByNumber(Context context,  java.lang.String telephone_number) {
        int duration_overall = 0;
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
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY) - 3;

            Cursor cursor =  context.getApplicationContext().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    CallLog.Calls.DATE + " >= ?",
                    new String[] { createDate(year, month, day, hour).toString()},
                    null
            );

            Debug.log("b");
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                String type = cursor.getString(3);
                String time = cursor.getString(4);
                int duration = cursor.getInt(5);
                Debug.log(number + " | " + time + " | " + type + " = " + name + " ! " + duration);
                if(number.contains(telephone_number)) {
                    duration_overall += duration;
                }
            }
            cursor.close();

        } catch (Exception e) {
            Debug.dumpException(e);
        }
        return duration_overall;
    }

}

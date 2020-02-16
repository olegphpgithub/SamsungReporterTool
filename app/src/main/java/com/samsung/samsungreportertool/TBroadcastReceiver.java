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
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.reflect.Method;

public class TBroadcastReceiver extends BroadcastReceiver {

    private static java.lang.String incomingNumber;
    private static final String TAG = "Phone call";
    private static TelephonyManager telephony;

    @Override
    public void onReceive(Context context, Intent intent) {

        Timer myTimer;
        java.lang.String number;
        telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            myTimer = new Timer();
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }

            }, 10000);

        } catch (Exception e) {
            Debug.dumpException(e);
        }
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

}

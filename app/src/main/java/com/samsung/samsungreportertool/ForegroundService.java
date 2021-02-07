package com.samsung.samsungreportertool;

import android.Manifest;
import android.app.Service;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Build;
import android.os.PowerManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.lang.reflect.Method;

public class ForegroundService extends Service {
    private static final String TAG = ForegroundService.class.getName();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final long DELAYED_TIME = 1000;
    private PowerManager.WakeLock wakeLock;

    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.log("onCreate Service", new java.util.Date());
//        String input = .getStringExtra("inputExtra");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelock2468");
        wakeLock.acquire();
        Debug.log("onCreate Service locked", new java.util.Date());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Debug.log("onStartCommand Service", new java.util.Date());
        final int serviceId = startId;
        final int left = intent.getIntExtra("MyTimeout", 0);
        Debug.log(Integer.toString(left), new java.util.Date());

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Runnable thread")
                //.setSmallIcon(R.drawable.ic_launcher_background)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(null) // Passing null here silently fails
                .setContentIntent(pendingIntent);

        startForeground(1, notification.build());

        new Thread(new Runnable() {
            public void run() {
                try {
                    try {
                        Debug.log("Sleep 1", new java.util.Date());
                        try {
                            Thread.sleep(left * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Debug.log("Sleep 2", new java.util.Date());
                        counter = counter + 1;
                        // notification.setContentText("Runnable thread " + counter);

                        endCall();

//                        try {
//
//                            Debug.log("Shutdown begin", new java.util.Date());
//
//                            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                            Class<?> c = Class.forName(telephony.getClass().getName());
//                            Method m = c.getDeclaredMethod("getITelephony");
//                            m.setAccessible(true);
//                            Object telephonyService = m.invoke(telephony);
//                            Class<?> telephonyServiceClass = Class.forName(telephonyService.getClass().getName());
//                            Method endCallMethod = telephonyServiceClass.getDeclaredMethod("endCall");
//                            endCallMethod.setAccessible(true);
//                            endCallMethod.invoke(telephonyService);
//
//                            Debug.log("Shutdown end", new java.util.Date());
//
//                        } catch (Exception e) {
//                            Debug.dumpException(e);
//                        }

//                        Intent intent = new Intent(this, TBroadcastReceiver.class);
//                        intent.setAction("com.toxy.LOAD_URL");
//                        intent.putExtra("url", "com.toxy");
//                        sendBroadcast(intent);

                        stopForeground(true);
                        stopSelf(serviceId);
                    } catch (java.lang.Exception exception) {
                        Debug.dumpException(exception);
                    }
                } catch (java.lang.Exception exception) {
                    exception.printStackTrace();
                }
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Debug.log("wakeLock.release", new java.util.Date());
        stopForeground(true);
        wakeLock.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.enableVibration(false);
            // serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void endCall01()
    {
        try {
            TelecomManager tm = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            }
            if (tm != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                boolean success = tm.endCall();
            }
        } catch (java.lang.Exception ex) {

        }
    }

    public void endCall() {

        Debug.log("Shutdown begin 01", new java.util.Date());

        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
            m1.setAccessible(true);
            Object iTelephony = m1.invoke(tm);

            Method m2 = iTelephony.getClass().getDeclaredMethod("silenceRinger");
            Method m3 = iTelephony.getClass().getDeclaredMethod("endCall");

            m2.invoke(iTelephony);
            m3.invoke(iTelephony);
        } catch (java.lang.Exception ex) {
            Debug.dumpException(ex);
        }

        Debug.log("Shutdown end 01", new java.util.Date());

    }

}
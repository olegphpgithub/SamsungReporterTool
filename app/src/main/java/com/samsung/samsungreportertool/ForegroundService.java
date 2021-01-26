package com.samsung.samsungreportertool;

import android.app.Service;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.lang.reflect.Method;

public class ForegroundService extends Service {
    private static final String TAG = ForegroundService.class.getName();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final long DELAYED_TIME = 1000;


    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.log("onCreate Service", new java.util.Date());
//        String input = .getStringExtra("inputExtra");

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
                Debug.log("Sleep 1", new java.util.Date());
                try {
                    Thread.sleep(left * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Debug.log("Sleep 2", new java.util.Date());
                Debug.log("Runnable thread no visual", new java.util.Date());
                counter = counter + 1;
                notification.setContentText("Runnable thread " + counter);

                stopSelf(serviceId);
                stopForeground(true);
            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

}
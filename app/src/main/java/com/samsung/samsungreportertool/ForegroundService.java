package com.samsung.samsungreportertool;

import android.app.Service;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.IBinder;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("sssss")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(null) // Passing null here silently fails
                .setContentIntent(pendingIntent);




        //do heavy work on a background thread
        //stopSelf();


        new Thread(new Runnable() {
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    Debug.log("Runnable thread", new java.util.Date());
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //REST OF CODE HERE//
                    counter = counter + 1;
                    notification.setContentText("test" + counter);
                    startForeground(1, notification.build());
                }

            }
        }).start();


        return START_NOT_STICKY;
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

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
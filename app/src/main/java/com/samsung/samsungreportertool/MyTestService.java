package com.samsung.samsungreportertool;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

public class MyTestService extends IntentService {
    // Must create a default constructor
    public MyTestService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        while(true)
        {
            Debug.log("onHandleIntent", new java.util.Date());
            SystemClock.sleep(10 * 1000);
        }
    }
}

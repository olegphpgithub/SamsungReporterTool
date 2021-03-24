package com.samsung.samsungreportertool;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btnGrantPrivileges, btnStartService, btnStopService;
    TelecomManager telecomManager;
    TelephonyManager telephonyManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnGrantPrivileges = findViewById(R.id.buttonGrantPrivileges);
        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

        btnGrantPrivileges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GrantPrivileges();
            }
        });

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteTheCall();
            }
        });
    }

    public void GrantPrivileges()
    {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ANSWER_PHONE_CALLS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ANSWER_PHONE_CALLS}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 4);
        }
    }

    public void startService() {
        try {
            // Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
            Debug.log("Shutdown begin", new java.util.Date());

            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Class<?> c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(telephony);
            Class<?> telephonyServiceClass = Class.forName(telephonyService.getClass().getName());
            Method endCallMethod = telephonyServiceClass.getDeclaredMethod("endCall");

            endCallMethod.setAccessible(true);
            endCallMethod.invoke(telephonyService);
            Toast.makeText(this, "002", Toast.LENGTH_LONG).show();
            // Toast.makeText(this, "Shutdown end", Toast.LENGTH_LONG).show();
        } catch (java.lang.Exception ex) {
            Toast.makeText(this, "ex ----", Toast.LENGTH_LONG).show();
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean stopService01() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        if (telecomManager.isInCall()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (telecomManager != null) {
                    boolean success = telecomManager.endCall();
                    return success;
                }
            } else {
                try {
                    Class classTelephony = Class.forName(telephonyManager.getClass().getName());
                    Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");
                    methodGetITelephony.setAccessible(true);
                    ITelephony telephonyService = (ITelephony) methodGetITelephony.invoke(telephonyManager);
                    if (telephonyService != null) {
                        return telephonyService.endCall();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public void stopService02() {
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
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean cutTheCall() {
        TelecomManager telecomManager = (TelecomManager) getApplicationContext().getSystemService(TELECOM_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || telecomManager == null) {
            return false;
        }

        if (telecomManager.isInCall()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                boolean callDisconnected = telecomManager.endCall();
                return callDisconnected;
            }
        }
        return true;
    }

    private void muteTheCall() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (audioManager.isMicrophoneMute() == false) {
            btnStopService.setText("Voice");
        } else {
            btnStopService.setText("Mute");
        }
    }
}

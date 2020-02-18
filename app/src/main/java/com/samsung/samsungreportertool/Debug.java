package com.samsung.samsungreportertool;


import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class Debug {

    public final static String LOG_FILE_NAME = "debug.txt";

    public static void log(String message) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + LOG_FILE_NAME), true));
            pw.append(message);
            pw.append("\n");
            pw.close();
        } catch(java.lang.Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void log(java.lang.String message, java.util.Date date) {
        java.lang.String when = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        log(when + " " + message);
    }

    public static void dumpBundle(Bundle bundle) {
        try {
            try {

                PrintWriter pw = new PrintWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + LOG_FILE_NAME), true));
                pw.append("{\n");
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        java.lang.Object value = bundle.get(key);
                        pw.append(String.format("%s$%s$%s\n", key, value.toString(), value.getClass().getName()));
                    }
                } else {
                    pw.append("bundle is null\n");
                }
                pw.append("}\n");
                pw.close();

            } catch (java.lang.Exception ex) {
                Debug.dumpException(ex);
            }
        } catch (java.lang.Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void dumpException(java.lang.Exception exception) {
        try {
            PrintWriter pringWriter = new PrintWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + LOG_FILE_NAME), true));
            exception.printStackTrace(pringWriter);
            pringWriter.append("\n");
            pringWriter.close();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}

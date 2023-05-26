package com.ddprojects.messager.service;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Hashtable;

import okhttp3.HttpUrl;

public class globals {
    public static final Hashtable<Object, Object> liveData = new Hashtable<>();
    public static SharedPreferences persistentDataOnDisk =
            fakeContext.getInstance().getSharedPreferences("data", Context.MODE_PRIVATE);
    public static SharedPreferences.Editor PDDEditor = persistentDataOnDisk.edit();

    public static boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager)
                fakeContext.getInstance().getApplicationContext().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        Network net = cm.getActiveNetwork();
        if (net == null) return false;
        NetworkCapabilities actNet = cm.getNetworkCapabilities(net);
        return actNet != null &&
                (
                        actNet.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || actNet.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || actNet.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                                || actNet.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                );
    }

    public static void showListDialog(
            Context context,
            String title,
            String[] items,
            DialogInterface.OnClickListener listener
    ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setItems(items, listener)
                .create();
        builder.show();
    }

    public static void showToastMessage(String text, boolean shortDuration) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(
                fakeContext.getInstance().getBaseContext(),
                text,
                shortDuration ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG
        ).show());
    }

    public static void writeErrorInLog(Exception ex) {
        _createLogFile(ex.getMessage(), _stackTraceToString(ex));
    }

    public static void log(Object message) {
        Log.d("ddMessager", String.valueOf(message));
    }

    public static String generateUrl(
            boolean isSecured,
            String host,
            int port,
            String[] arrayPath,
            @Nullable Hashtable<String, String> arrayParams
    ) {
        HttpUrl.Builder builder = new HttpUrl.Builder();

        builder.scheme(!isSecured ? "http" : "https");
        builder.host(host);
        builder.port(port);

        for (String pathSegment : arrayPath) {
            builder.addPathSegment(pathSegment);
        }

        if (arrayParams != null) arrayParams.forEach(builder::addQueryParameter);

        return builder.toString();
    }

    private static String _stackTraceToString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void _createLogFile(
            String error,
            String stacktrace
    ) {
        try {
            File logFile = new File(
                    fakeContext.getInstance().getApplicationContext().getDataDir(),
                    "log.txt"
            );
            if (logFile.createNewFile()) log("Log file created");

            FileWriter fr = new FileWriter(logFile, true);
            fr.write("Date: " + new Date() + "\n");
            fr.write("Error: " + error + "\n" + "Stacktrace:\n" + stacktrace);
            fr.write("==================================\n");
            fr.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
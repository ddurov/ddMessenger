package com.ddprojects.messager.service;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class globals {
    private static OkHttpClient client;
    public static final Hashtable<Object, Object> liveData = new Hashtable<>();
    public static final Hashtable<String, Object[]> APIEndpoints = new Hashtable<>();
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

    public static void setupApiClient(boolean pinningIsNeed) {
        if (!pinningIsNeed) {
            client = new OkHttpClient.Builder().readTimeout(25, TimeUnit.SECONDS).build();
            return;
        }

        StringBuilder sb = new StringBuilder();

        APIEndpoints.forEach((key, value) -> {
            sb.append(value[0]);
            sb.append(",");
        });

        Hashtable<String, String> params = new Hashtable<>();
        params.put("domains", sb.toString());
        Request request = new Request.Builder()
                .url(_generateUrl(
                        (int) Objects.requireNonNull(APIEndpoints.get("general"))[1] == 443,
                        (String) Objects.requireNonNull(APIEndpoints.get("general"))[0],
                        (int) Objects.requireNonNull(APIEndpoints.get("general"))[1],
                        new String[]{"utils", "getPinningHashDomains"},
                        params
                ))
                .build();

        CertificatePinner.Builder certsBuilder = new CertificatePinner.Builder();
        new OkHttpClient.Builder().build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                writeErrorInLog(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();

                APIResponse APIResponse = new Gson().fromJson(result, APIResponse.class);
                for (int i = 0; i < APIResponse.response.getAsJsonArray().size(); i++) {
                    if (APIResponse.response.
                            getAsJsonArray().get(i).
                            getAsJsonObject().get("requestStatus").getAsString().
                            equals("error")) continue;
                    certsBuilder.add(
                            APIResponse.response.
                                    getAsJsonArray().get(i).
                                    getAsJsonObject().get("domain").getAsString(),
                            "sha256/"+APIResponse.response.
                                    getAsJsonArray().get(i).
                                    getAsJsonObject().get("hash").getAsString()
                    );
                }
            }
        });

        client = new OkHttpClient.Builder().
                readTimeout(25, TimeUnit.SECONDS).certificatePinner(
                        certsBuilder.build()
                ).build();
    }

    public static void executeApiMethod(
            String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params,
            Callback callback
    ) {
        _request(_generateUrl(
                    (int) Objects.requireNonNull(APIEndpoints.get(typeApi))[1] == 443,
                    (String) Objects.requireNonNull(APIEndpoints.get(typeApi))[0],
                    (int) Objects.requireNonNull(APIEndpoints.get(typeApi))[1],
                    new String[]{"methods", method, function},
                    (requestType.equals("get")) ? params : null
                ),
                (requestType.equals("get")) ? null : params,
                callback
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
        Toast.makeText(
                fakeContext.getInstance().getApplicationContext(),
                text,
                shortDuration ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG
        ).show();
    }

    public static void writeErrorInLog(Exception ex) {
        _createLogFile(ex.getMessage(), _stackTraceToString(ex));
    }

    public static void log(Object message) {
        Log.d("ddMessager", String.valueOf(message));
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

    private static String _generateUrl(
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

    public static void _request(
            String url,
            @Nullable Hashtable<String, String> arrayParams,
            Callback cb
    ) {
        Request.Builder request = new Request.Builder().url(url);

        if (arrayParams != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            arrayParams.forEach(builder::addFormDataPart);

            request.post(builder.build());
        }

        request.header("session-id", persistentDataOnDisk.getString("sessionId", ""));
        request.header("token", persistentDataOnDisk.getString("token", ""));
        request.header("user_agent",
                String.format(Locale.US, "ddMessagerApp/%s", "2.0")
        );

        client.newCall(request.build()).enqueue(cb);
    }
}
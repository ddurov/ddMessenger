package com.ddprojects.messager.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class globals {
    private static OkHttpClient client;
    private static final String[][] apiEndPoints = new String[][]{{"general", "api.ddproj.ru"}, {"messager", "messager.api.ddproj.ru"}};
    public static Map<String, JSONObject> loadedData = new HashMap<>();

    public static boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) fakeContext.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) return false;
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static void initHTTPClient(String[] domains) {
        StringBuilder sb = new StringBuilder();

        for (String domain : domains) {
            if (!domain.matches(" *")) {
                sb.append(domain);
                sb.append(",");
            }
        }

        String[][] params = new String[][]{{"domains", sb.toString()}};
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://api.ddproj.ru/utils/getPinningHashDomains")).newBuilder();
        for (String[] param : params) {
            if (param.length == 0) continue;
            urlBuilder.addQueryParameter(param[0], param[1]);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .build();

        try {
            callbackRequests requestsCallback = new callbackRequests();
            new OkHttpClient.Builder().build().newCall(request).enqueue(requestsCallback);

            CertificatePinner.Builder certsBuilder = new CertificatePinner.Builder();

            JSONArray responseGetPinningHash = new JSONObject(Objects.requireNonNull(requestsCallback.get().body()).string()).getJSONArray("response");

            for (int i = 0; i < responseGetPinningHash.length(); i++) {
                if (responseGetPinningHash.getJSONObject(i).getString("requestStatus").equals("error")) continue;
                certsBuilder.add(responseGetPinningHash.getJSONObject(i).getString("domain"), "sha256/"+responseGetPinningHash.getJSONObject(i).getString("hash"));
            }

            client = new OkHttpClient.Builder().readTimeout(25, TimeUnit.SECONDS).certificatePinner(certsBuilder.build()).build();
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
    }

    public static String executeApiMethod(String requestType, String typeApi, String method, String function, String[][] params) {
        String result = null;

        try {
            String domain = null;

            for (String[] endPoint : apiEndPoints)
                if (Objects.equals(typeApi, endPoint[0]))
                    domain = endPoint[1];

            result = Objects.equals(requestType, "get") ? _requestGet("https://"+domain+"/methods/" + method + "/" + function, params) : _requestPost("https://"+domain+"/methods/" + method + "/" + function, params);
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }

        return result;
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

    private static void _createLogFile(String error, String stacktrace) {
        try {
            File log = new File(fakeContext.getInstance().getApplicationContext().getDataDir(), "log.txt");
            log.createNewFile();

            FileWriter fr = new FileWriter(log, true);
            fr.write("Ошибка: " + error + "\n" + "Стек ошибки:\n" + stacktrace);
            fr.write("==================================\n");
            fr.close();

            Toast.makeText(fakeContext.getInstance().getApplicationContext(), "Создан лог", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String _request(Request.Builder request) {
        try {
            callbackRequests future = new callbackRequests();
            request.header("session-id", "sample");
            request.header("token", "sample");
            client.newCall(request.build()).enqueue(future);
            return Objects.requireNonNull(future.get().body()).string();
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
        return null;
    }

    private static String _requestGet(String url, String[][] params) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (String[] param : params)
            builder.addQueryParameter(param[0], param[1]);

        Request.Builder request = new Request.Builder()
                .url(builder.build());

        return _request(request);
    }

    private static String _requestPost(String url, String[][] params) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (String[] param : params)
            builder.addFormDataPart(param[0], param[1]);

        Request.Builder request = new Request.Builder()
                .url(url)
                .post(builder.build());

        return _request(request);
    }

    private static String[][] _addNestedArray(int length, String[][] array, String[] stringAdd) {
        String[][] temp = new String[length + 1][];
        if (length >= 0) System.arraycopy(array, 0, temp, 0, length);
        temp[length] = stringAdd;
        return temp;
    }

    private static Object[] _addObject(int length, Object[] array, Object objectAdd) {
        Object[] temp = new Object[length + 1];
        if (length >= 0) System.arraycopy(array, 0, temp, 0, length);
        temp[length] = objectAdd;
        return temp;
    }
}

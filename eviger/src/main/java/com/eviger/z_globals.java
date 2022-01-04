package com.eviger;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class z_globals {

    public static SharedPreferences tokenSet = z_fakeContext.getInstance().getApplicationContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
    public static JSONObject myProfile;
    public static ArrayList<z_dialog> dialogs = new ArrayList<>();
    public static z_dialogAdapter dialogsAdapter;
    public static z_listener z_listener = new z_listener();
    public static boolean sendingOnline;
    public static String channelMessages = "notificationsMessages";
    private static final OkHttpClient client = new OkHttpClient.Builder().readTimeout(25, TimeUnit.SECONDS).certificatePinner(
            new CertificatePinner.Builder()
                    .add("api.eviger.ru", "sha256/e/Ct+Ll96IKxCpcBmRwQ4pKeJNHzujbDDnYJYzZyh4Q=").build()
    ).build();

    public static boolean hasConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static String executeApiMethodGet(String method, String subMethod, String[][] params) {
        String result = null;

        try {
            String[] methodsWithoutToken = {"service.getUpdates", "user.auth"};
            params = special_addStringArrayInArray(params.length, params, new String[]{"method", subMethod});
            if (!Arrays.asList(methodsWithoutToken).contains(method + "." + subMethod)) params = special_addStringArrayInArray(params.length, params, new String[]{"token", getToken()});

            result = special_requestGet("https://api.eviger.ru/methods/" + method, params);
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }

        return result;
    }

    public static String executeApiMethodPost(String method, String subMethod, JSONObject json) {
        String result = null;

        try {

            String[] methodsWithoutToken = {"user.registerAccount", "email.createCode", "email.confirmCode"};

            json.put("method", subMethod);
            if (!Arrays.asList(methodsWithoutToken).contains(method + "." + subMethod)) {
                json.put("token", getToken());
            }

            result = special_requestPost("https://api.eviger.ru/methods/" + method, json.toString());

        } catch (Exception ex) {
            writeErrorInLog(ex);
        }

        return result;
    }

    public static String executeLongPollMethod(String method, String[][] params) {
        String result = null;

        try {

            params = special_addStringArrayInArray(params.length, params, new String[]{"token", getToken()});
            result = special_requestGet("https://api.eviger.ru/longpoll/" + method, params);

        } catch (Exception ex) {
            writeErrorInLog(ex);
        }

        return result;

    }

    public static String getToken() {
        return tokenSet.getString("token", null);
    }

    public static void writeErrorInLog(Exception ex) {
        Toast.makeText(z_fakeContext.getInstance().getApplicationContext(), "Ошибка неизвестного характера, записываю в лог", Toast.LENGTH_SHORT).show();

        special_createLogFile(ex.getMessage(), stackTraceToString(ex));
    }

    public static String stackTraceToString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void log(Object message) {
        Log.d("l/e", String.valueOf(message));
    }

    public static void moveDialogToTop(ArrayList<z_dialog> listDialogs, int peerId, z_dialog newData) {
        int position = -1;

        for (int i = 0; i < listDialogs.size(); i++) {
            if (listDialogs.get(i).getId() == peerId) {
                position = i;
                break;
            }
        }

        if (position == -1) {
            listDialogs.add(newData);
            position = listDialogs.size()-1;
        } else {
            z_dialog dialog = listDialogs.get(position);
            dialog.setDate(newData.getDate());
            dialog.setMessage(newData.getMessage());
        }
        Collections.swap(listDialogs, position, 0);
    }

    public static void insertMessageByPeerId(ArrayList<z_message> listMessages, z_message message) {
        listMessages.add(message);
    }

    private static void special_createLogFile(String error, String stacktrace) {
        try {

            File log = new File(z_fakeContext.getInstance().getApplicationContext().getDataDir(), "log.txt");
            log.createNewFile();

            FileWriter fr = new FileWriter(log, true);
            fr.write("================= BEGIN LOGS =================\n");
            fr.write("Ошибка: " + error + "\n" + "Стек ошибки: " + stacktrace);
            fr.write("================= END LOGS =================\n");
            fr.close();

        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
    }

    private static String special_requestGet(String url, String[][] params) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (String[] param : params) {
            if (param.length == 0) continue;
            builder.addQueryParameter(param[0], param[1]);
        }

        Request request = new Request.Builder()
                .url(builder.build())
                .build();

        String response = null;

        try {
            z_callbackRequests future = new z_callbackRequests();
            client.newCall(request).enqueue(future);
            response = Objects.requireNonNull(future.get().body()).string();
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }

        return response;

    }

    private static String special_requestPost(String url, String data) {
        RequestBody requestBody = RequestBody.create(data, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        String response = null;

        try {
            z_callbackRequests future = new z_callbackRequests();
            client.newCall(request).enqueue(future);
            response = Objects.requireNonNull(future.get().body()).string();
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }

        return response;

    }

    private static String[][] special_addStringArrayInArray(int length, String[][] array, String[] stringAdd) {
        String[][] temp = new String[length + 1][];
        if (length >= 0) System.arraycopy(array, 0, temp, 0, length);
        temp[length] = stringAdd;
        return temp;
    }

    private static Object[] special_addObject(int length, Object[] array, Object objectAdd) {
        Object[] temp = new Object[length + 1];
        if (length >= 0) System.arraycopy(array, 0, temp, 0, length);
        temp[length] = objectAdd;
        return temp;
    }

    public static void setOnline() {
        try {
            JSONObject JSON = new JSONObject();
            JSON.put("token", getToken());
            executeApiMethodPost("user", "setOnline", JSON);
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
    }

    public static void setOffline() {
        try {
            JSONObject JSON = new JSONObject();
            JSON.put("token", getToken());

            executeApiMethodPost("user", "setOffline", JSON);
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
    }

    public static String requestEmailCode(String email) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
        return executeApiMethodPost("email", "createCode", json);
    }

    public static Object[] getProfileById(int id) {
        Object[] data = new Object[]{};
        try {
            JSONObject getResponse_usersGet = new JSONObject(executeApiMethodGet("users", "get", id != -1 ? new String[][]{{"id", String.valueOf(id)}} : new String[][]{{}}));

            data = special_addObject(data.length, data, getResponse_usersGet.getJSONObject("response").getInt("eid"));
            data = special_addObject(data.length, data, getResponse_usersGet.getJSONObject("response").getString("username"));
            data = special_addObject(data.length, data, getResponse_usersGet.getJSONObject("response").getInt("lastSeen"));
        } catch (Exception ex) {
            writeErrorInLog(ex);
        }
        return data;
    }

}

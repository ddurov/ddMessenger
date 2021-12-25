package com.eviger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class z_globals extends AppCompatActivity {

    public static SharedPreferences tokenSet = z_fakeContext.getInstance().getApplicationContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
    public static JSONObject myProfile;
    public static ArrayList<Object[]> dialogs = new ArrayList<>();
    public static boolean sendingOnline;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static boolean hasConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    private static String[][] addStringArray(int length, String[][] array, String[] stringAdd) {
        String[][] temp = new String[length + 1][];
        if (length >= 0) System.arraycopy(array, 0, temp, 0, length);
        temp[length] = stringAdd;
        return temp;
    }

    private static Object[] addObject(int length, Object[] array, Object objectAdd) {
        Object[] temp = new Object[length + 1];
        if (length >= 0) System.arraycopy(array, 0, temp, 0, length);
        temp[length] = objectAdd;
        return temp;
    }

    private static String response(Request request) {
        String responseStr = "";
        z_requester z_requester = new z_requester(request);
        z_requester.execute();
        try {
            responseStr = z_requester.get();
        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }
        return responseStr;
    }

    private static String requestGet(String url, String[][] params) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (String[] param : params) {
            if (param.length == 0) continue;
            builder.addQueryParameter(param[0], param[1]);
        }
        Request request = new Request.Builder()
                .url(builder.build())
                .build();
        return response(request);
    }

    private static String requestPost(String url, String data) {
        RequestBody requestBody = RequestBody.create(data, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return response(request);
    }

    public static String executeApiMethodGet(String method, String subMethod, String[][] params) {
        String[] methodsWithoutToken = {"service.getUpdates", "user.auth"};
        params = addStringArray(params.length, params, new String[]{"method", subMethod});
        if (!Arrays.asList(methodsWithoutToken).contains(method + "." + subMethod))
            params = addStringArray(params.length, params, new String[]{"token", getToken()});

        return requestGet("https://api.eviger.ru/methods/" + method, params);
    }

    public static String executeApiMethodPost(String method, String subMethod, JSONObject json) {
        try {

            String[] methodsWithoutToken = {"user.registerAccount", "email.createCode", "email.confirmCode"};

            json.put("method", subMethod);
            if (!Arrays.asList(methodsWithoutToken).contains(method + "." + subMethod)) {
                json.put("token", getToken());
            }

        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }

        return requestPost("https://api.eviger.ru/methods/" + method, json.toString());
    }

    public static String executeLongPollMethod(String method, String[][] params) {

        params = addStringArray(params.length, params, new String[]{"token", getToken()});
        return requestGet("https://api.eviger.ru/longpoll/" + method, params);

    }

    public static String getToken() {

        return tokenSet.getString("token", null);

    }

    public static void showOrWriteError(String error, String stacktrace, Context c) {
        log(error + "\n" + stacktrace);
        switch (error) {
            case "Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference":
                Toast.makeText(c, "Ошибка на наших серверах, повторите попытку чуть позже", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(c, "Ошибка неизвестного характера, записываю в лог", Toast.LENGTH_SHORT).show();

                special_createLogFile(error, stacktrace, c);

                Toast.makeText(c, "Лог создан", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static void grantPermissionStorage(Activity c) {
        new z_globals().special_grantStorage(c);
    }

    public static String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void log(Object message) {
        Log.e("l/e", String.valueOf(message));
    }

    public static ArrayList<Object[]> moveOrAddDialogToTop(ArrayList<Object[]> list, int peerId, z_dialog newData) {

        int positionPeerId = -1;

        for (int i = 0; i < list.size(); i++) {
            if ((int) list.get(i)[0] == peerId) {
                positionPeerId = i;
                break;
            }
        }

        if (positionPeerId == -1) {

            list.add(new Object[]{peerId, newData});

            positionPeerId = list.size();

        }

        Object[] valueBeingMoved = (Object[]) list.toArray()[positionPeerId];

        valueBeingMoved[1] = newData;

        for (int i = positionPeerId; i > 0; i--) {
            list.set(i, (Object[]) list.toArray()[i - 1]);
        }

        list.set(0, valueBeingMoved);

        return list;
    }

    private void special_grantStorage(Activity c) {
        if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private static void special_createLogFile(String error, String stacktrace, Context c) {
        try {

            File log = new File(c.getDataDir(), "log.txt");
            log.createNewFile();

            FileWriter fr = new FileWriter(log, true);
            fr.write("================= BEGIN LOGS =================\n");
            fr.write("Текст ошибки: " + error + "\n" + "Стек: " + stacktrace);
            fr.write("================= END LOGS =================\n");
            fr.close();

        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }
    }

    public static void setOnline() {
        try {
            JSONObject JSON = new JSONObject();
            JSON.put("token", getToken());
            executeApiMethodPost("user", "setOnline", JSON);
        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }
    }

    public static void setOffline() {
        try {
            JSONObject JSON = new JSONObject();
            JSON.put("token", getToken());
            executeApiMethodPost("user", "setOffline", JSON);
        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }
    }

    public static String requestEmailCode(String email) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }
        return executeApiMethodPost("email", "createCode", json);
    }

    public static Object[] getProfileById(int id) {
        Object[] data = new Object[]{};

        try {

            JSONObject getResponse_usersGet = new JSONObject(executeApiMethodGet("users", "get", id != -1 ? new String[][]{{"id", String.valueOf(id)}} : new String[][]{{}}));

            data = addObject(data.length, data, getResponse_usersGet.getJSONObject("response").getInt("eid"));
            data = addObject(data.length, data, getResponse_usersGet.getJSONObject("response").getString("username"));
            data = addObject(data.length, data, getResponse_usersGet.getJSONObject("response").getInt("lastSeen"));

        } catch (Throwable ex) {
            showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), z_fakeContext.getInstance().getApplicationContext());
        }

        return data;
    }

}

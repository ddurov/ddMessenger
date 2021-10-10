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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class globals extends AppCompatActivity {

    public static SharedPreferences tokenSet;
    public static final String APP_PREFERENCES = "tokens";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static boolean hasConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            return false;
        }
    }

    private static String[][] addString(int lenght, String[][] array, String[] arrayAdd) {
        String[][] temp = new String[lenght + 1][];
        if (lenght >= 0) System.arraycopy(array, 0, temp, 0, lenght);
        temp[lenght] = arrayAdd;
        return temp;
    }

    private static Object[] addObject(int lenght, Object[] array, Object objectAdd) {
        Object[] temp = new Object[lenght + 1];
        if (lenght >= 0) System.arraycopy(array, 0, temp, 0, lenght);
        temp[lenght] = objectAdd;
        return temp;
    }

    private static String implode(String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            sb.append(data[i]);
            sb.append("=");
        }
        sb.append(data[data.length - 1].trim());
        return sb.toString();
    }

    private static String buildQuery(String[][] array) {

        List<String[]> firstList = new ArrayList<>();
        List<String> secondList = new ArrayList<>();

        int e1;
        int e2;

        for (e1 = 0; e1 < array.length; e1++) {
            if (!Arrays.toString(array[e1]).equals(Arrays.toString(new String[]{}))) {
                firstList.add(array[e1]);
            }
        }
        for (e2 = 0; e2 < firstList.size(); e2++) {
            secondList.add(implode(firstList.get(e2)));
        }
        return secondList.stream().collect(Collectors.joining("&"));

    }

    private static String response(Request request) {
        String responseStr = "";
        apiRequester apiRequester = new apiRequester(request);
        apiRequester.execute();
        try {
            responseStr = apiRequester.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return responseStr;
    }

    public static String requestGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return response(request);
    }

    public static String requestPost(String url, String data) {
        RequestBody requestBody = RequestBody.create(data, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return response(request);
    }

    public static String executeApiMethodGet(String method, String subMethod, String[][] params) {
        String[] methodsWithoutToken = {"service.getUpdates", "user.auth"};
        params = addString(params.length, params, new String[]{"method", subMethod});
        params = addString(params.length, params, getToken() != null && !Arrays.asList(methodsWithoutToken).contains(method + "." + subMethod) ? new String[]{"token", getToken()} : new String[]{});

        return requestGet("https://api.eviger.ru/methods/" + method + "?" + buildQuery(params));
    }

    public static String executeApiMethodPost(String method, String subMethod, JSONObject json) {
        try {

            String[] methodsWithoutToken = {"user.registerAccount", "email.createCode", "email.confirmCode"};

            if (!Arrays.asList(methodsWithoutToken).contains(method + "." + subMethod)) {
                json.put("token", getToken());
            }
            json.put("method", subMethod);

        } catch (Exception e) {
            e.fillInStackTrace();
        }

        return requestPost("https://api.eviger.ru/methods/" + method, json.toString());
    }

    public static String getToken() {

        tokenSet = forContext.getInstance().getApplicationContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return tokenSet.getString("token", null);

    }

    public void createLogFile(String error, String stacktrace, Context c) {
        try {

            File log = new File(c.getDataDir(), "log.txt");
            log.createNewFile();

            FileWriter fr = null;
            try {
                fr = new FileWriter(log, true);
                fr.write("================= BEGIN LOGS =================\n");
                fr.write("Текст ошибки: " + error + "\n" + "Стек: " + stacktrace);
                fr.write("================= END LOGS =================\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert fr != null;
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showHumanReadlyTextError(String error, String stacktrace, Context c) {
        switch (error) {
            case "Attempt to invoke virtual method 'int java.lang.String.length()' on a null object reference":
                Toast.makeText(c, "Ошибка на наших серверах, повторите попытку чуть позже", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(c, "Ошибка неизвестного характера, записываю в лог", Toast.LENGTH_SHORT).show();

                globals g = new globals();
                g.createLogFile(error, stacktrace, c);

                Toast.makeText(c, "Лог создан", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void temp_grantStorage(Activity c) {
        if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(c, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public static void grantPermissionStorage(Activity c) {
        globals g = new globals();
        g.temp_grantStorage(c);
    }

    public static String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void setOnline() {
        Log.e("l/e/debug", "Онлайн отправлен");
        try {
            JSONObject JSON = new JSONObject();
            JSON.put("token", getToken());
            executeApiMethodPost("user", "setOnline", JSON);
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public static void setOffline() {
        Log.e("l/e/debug", "Оффлайн отправлен");
        try {
            JSONObject JSON = new JSONObject();
            JSON.put("token", getToken());
            executeApiMethodPost("user", "setOffline", JSON);
        } catch (JSONException e) {
            e.fillInStackTrace();
        }
    }

    public static Object[] getAccount(Integer userId) {
        Object[] data = new Object[]{};

        try {

            JSONObject json;

            if (userId == 0) {
                json = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));
            } else {
                json = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{"id", String.valueOf(userId)}}));
            }

            data = addObject(data.length, data, json.getJSONObject("response").getInt("eid"));
            data = addObject(data.length, data, json.getJSONObject("response").getString("username"));
            data = addObject(data.length, data, json.getJSONObject("response").getInt("lastSeen"));

        } catch (Throwable e) {
            e.fillInStackTrace();
        }

        return data;
    }

    public static String getHashCodeEmail(String email) {
        String hash = null;

        try {

            JSONObject json = new JSONObject();
            json.put("email", email);

            JSONObject jsonRequestData = new JSONObject(executeApiMethodPost("email", "createCode", json));

            hash = jsonRequestData.getJSONObject("response").getString("hash");

        } catch (Throwable e) {
            e.fillInStackTrace();
        }

        return hash;
    }

    public static boolean submitHashAndCodeEmail(String email, String code, String hash) {
        boolean correct = false;

        try {

            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("code", code);
            json.put("hash", hash);

            JSONObject jsonRequestData = new JSONObject(executeApiMethodPost("email", "confirmCode", json));

            correct = !jsonRequestData.getJSONObject("response").has("error");

        } catch (Throwable e) {
            e.fillInStackTrace();
        }

        return correct;
    }

}

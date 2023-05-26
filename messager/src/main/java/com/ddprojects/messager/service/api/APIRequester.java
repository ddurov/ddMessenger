package com.ddprojects.messager.service.api;

import static com.ddprojects.messager.service.globals.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ddprojects.messager.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class APIRequester {

    private static OkHttpClient client;
    public static final Hashtable<String, Object[]> APIEndPoints = new Hashtable<>();

    public static void setupApiClient(boolean pinningIsNeed) {
        if (BuildConfig.DEBUG) {
            APIEndPoints.put("general", new Object[]{"ddproj.ru", 8000});
            APIEndPoints.put("product", new Object[]{"ddproj.ru", 8001});
        } else {
            APIEndPoints.put("general", new Object[]{"api.ddproj.ru", 443});
            APIEndPoints.put("product", new Object[]{"messager.api.ddproj.ru", 443});
        }

        if (!pinningIsNeed) {
            client = new OkHttpClient.Builder().readTimeout(25, TimeUnit.SECONDS).build();
            return;
        }

        StringBuilder sb = new StringBuilder();

        APIEndPoints.forEach((key, value) -> {
            sb.append(value[0]);
            sb.append(",");
        });

        Hashtable<String, String> params = new Hashtable<>();
        params.put("domains", sb.toString());
        Request request = new Request.Builder()
                .url(generateUrl(
                        (int) Objects.requireNonNull(APIEndPoints.get("general"))[1] == 443,
                        (String) Objects.requireNonNull(APIEndPoints.get("general"))[0],
                        (int) Objects.requireNonNull(APIEndPoints.get("general"))[1],
                        new String[]{"utils", "getPinningHashDomains"},
                        params
                ))
                .build();

        CertificatePinner.Builder certsBuilder = new CertificatePinner.Builder();
        new OkHttpClient.Builder().build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                writeErrorInLog(e);
                showToastMessage("Произошла ошибка при выполнении запроса", false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();

                try {
                    JSONArray responseAsObject = new JSONObject(result).getJSONArray("body");

                    for (int i = 0; i < responseAsObject.length(); i++) {
                        JSONObject domain = responseAsObject.getJSONObject(i);
                        certsBuilder.add(
                                domain.getString("domain"),
                                "sha256/" + domain.getString("hash")
                        );
                    }
                } catch (JSONException ex) {
                    writeErrorInLog(ex);
                    showToastMessage("Произошла ошибка при выполнении запроса", false);
                }
            }
        });

        client = new OkHttpClient.Builder().
                readTimeout(25, TimeUnit.SECONDS).certificatePinner(
                        certsBuilder.build()
                ).build();
    }

    public static String executeApiMethodSync(
            String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params
    ) {
        try {

            Object[] response = _request(generateUrl(
                            (int) Objects.requireNonNull(APIEndPoints.get(typeApi))[1] == 443,
                            (String) Objects.requireNonNull(APIEndPoints.get(typeApi))[0],
                            (int) Objects.requireNonNull(APIEndPoints.get(typeApi))[1],
                            new String[]{"methods", method, function},
                            (requestType.equals("get")) ? params : null
                    ),
                    (requestType.equals("get")) ? null : params
            );

            if ((int) response[0] != 200) {
                throw new APIException(
                        new JSONObject((String) response[1]).getString("errorMessage")
                );
            } else return (String) response[1];

        } catch (IOException | JSONException ex) {
            writeErrorInLog(ex);
            showToastMessage("Произошла ошибка при выполнении запроса", false);
        } catch (APIException apiExceptions) {
            showToastMessage(
                    APIException.translate(method, apiExceptions.getMessage()),
                    false
            );
        }

        return null;
    }

    public static void executeApiMethodAsync(
            String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params,
            Callback callback
    ) {
        _request(generateUrl(
                        (int) Objects.requireNonNull(APIEndPoints.get(typeApi))[1] == 443,
                        (String) Objects.requireNonNull(APIEndPoints.get(typeApi))[0],
                        (int) Objects.requireNonNull(APIEndPoints.get(typeApi))[1],
                        new String[]{"methods", method, function},
                        (requestType.equals("get")) ? params : null
                ),
                (requestType.equals("get")) ? null : params,
                callback
        );
    }

    private static Object[] _request(
            String url,
            @Nullable Hashtable<String, String> arrayParams
    ) throws IOException {
        try (Response response = client.newCall(_requestBuilder(url, arrayParams)).execute()) {
            return new Object[]{response.code(), response.body().string()};
        }
    }

    private static void _request(
            String url,
            @Nullable Hashtable<String, String> arrayParams,
            Callback cb
    ) {
        client.newCall(_requestBuilder(url, arrayParams)).enqueue(cb);
    }

    private static Request _requestBuilder(
            String url,
            @Nullable Hashtable<String, String> arrayParams
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
        request.header("user-agent",
                String.format(Locale.US, "ddMessagerApp/%s", BuildConfig.VERSION_NAME)
        );

        return request.build();
    }

}

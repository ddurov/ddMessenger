package com.ddprojects.messager.service.api;

import static com.ddprojects.messager.service.globals.generateUrl;
import static com.ddprojects.messager.service.globals.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ddprojects.messager.BuildConfig;
import com.ddprojects.messager.R;
import com.ddprojects.messager.service.api.models.ErrorResponse;
import com.ddprojects.messager.service.api.models.SuccessResponse;
import com.ddprojects.messager.service.fakeContext;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIRequester {

    private static OkHttpClient client;
    public static final Hashtable<String, Object[]> APIEndPoints = new Hashtable<>();

    public static void setupApiClient() {
        if (BuildConfig.DEBUG) {
            APIEndPoints.put("general", new Object[]{"dev.ddproj.ru", 8000});
            APIEndPoints.put("product", new Object[]{"dev.ddproj.ru", 8001});
        } else {
            APIEndPoints.put("general", new Object[]{"api.ddproj.ru", 443});
            APIEndPoints.put("product", new Object[]{"messager.api.ddproj.ru", 443});
        }

        if (BuildConfig.DEBUG) {
            client = new OkHttpClient.Builder()
                    .readTimeout(25, TimeUnit.SECONDS)
                    .build();
            return;
        }

        StringBuilder sb = new StringBuilder();

        APIEndPoints.forEach((key, value) -> {
            sb.append(value[0]);
            sb.append(",");
        });

        Hashtable<String, String> servicePinningHashParams = new Hashtable<>();
        servicePinningHashParams.put("domains", sb.toString());
        Request request = new Request.Builder()
                .url(generateUrl(
                        (int) Objects.requireNonNull(APIEndPoints.get("general"))[1] == 443,
                        (String) Objects.requireNonNull(APIEndPoints.get("general"))[0],
                        (int) Objects.requireNonNull(APIEndPoints.get("general"))[1],
                        new String[]{"method", "service", "getPinningHashDomains"},
                        servicePinningHashParams
                ))
                .build();

        CertificatePinner.Builder certsBuilder = new CertificatePinner.Builder();
        new OkHttpClient.Builder().build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                writeErrorInLog(e);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String result = response.body().string();

                JsonArray responseAsObjects = new Gson().fromJson(result, SuccessResponse.class)
                        .body
                        .getAsJsonArray();

                for (int i = 0; i < responseAsObjects.size(); i++) {
                    JsonElement domain = responseAsObjects.get(i);
                    certsBuilder.add(
                            domain.getAsJsonObject().get("domain").getAsString(),
                            "sha256/" + domain.getAsJsonObject().get("hash").getAsString()
                    );
                }
            }
        });

        client = new OkHttpClient.Builder().
                readTimeout(25, TimeUnit.SECONDS).certificatePinner(
                        certsBuilder.build()
                )
                .build();
    }

    public static SuccessResponse executeApiMethodSync(
            @NonNull String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params
    ) throws APIException, IOException {
        Object[] responseObject = _request(generateUrl(
                        (int) Objects.requireNonNull(APIEndPoints.get(typeApi))[1] == 443,
                        (String) Objects.requireNonNull(APIEndPoints.get(typeApi))[0],
                        (int) Objects.requireNonNull(APIEndPoints.get(typeApi))[1],
                        new String[]{"methods", method, function},
                        (requestType.equals("get")) ? params : null
                ),
                (requestType.equals("get")) ? null : params
        );

        if ((int) responseObject[0] != 200) {
            ErrorResponse responseJSON = new Gson().fromJson((String) responseObject[1], ErrorResponse.class);
            throw new APIException(
                    responseJSON.errorMessage,
                    responseJSON.code
            );
        }

        return new Gson().fromJson((String) responseObject[1], SuccessResponse.class);
    }

    public static void executeApiMethodAsync(
            @NonNull String requestType,
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

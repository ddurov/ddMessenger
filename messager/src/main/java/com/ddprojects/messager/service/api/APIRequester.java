package com.ddprojects.messager.service.api;

import static com.ddprojects.messager.service.globals.APIEndPoints;
import static com.ddprojects.messager.service.globals.generateUrl;
import static com.ddprojects.messager.service.globals.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ddprojects.messager.BuildConfig;
import com.ddprojects.messager.R;
import com.ddprojects.messager.models.ErrorResponse;
import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.service.fakeContext;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIRequester {
    private static OkHttpClient client;

    public static void setupApiClient() {
        APIEndPoints.put("general", "api.ddproj.ru");
        APIEndPoints.put("product", "messager.api.ddproj.ru");

        if (BuildConfig.DEBUG) {
            try {
                X509TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                    }
                };

                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());

                client = new OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS)
                        .sslSocketFactory(sslContext.getSocketFactory(), TRUST_ALL_CERTS)
                        .hostnameVerifier((hostname, session) -> true)
                        .build();
                return;
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                writeErrorInLog(e);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_internal),
                        false
                );
            }
        }

        StringBuilder sb = new StringBuilder();

        APIEndPoints.forEach((key, value) -> {
            sb.append(value);
            sb.append(",");
        });

        Hashtable<String, String> servicePinningHashParams = new Hashtable<>();
        servicePinningHashParams.put("domains", sb.toString());
        Request request = new Request.Builder()
                .url(generateUrl(
                        true,
                        APIEndPoints.get("general"),
                        443,
                        new String[]{"method", "service", "getPinningHashDomains"},
                        servicePinningHashParams
                ))
                .build();

        CertificatePinner.Builder certsBuilder = new CertificatePinner.Builder();
        new OkHttpClient.Builder().build().newCall(request).enqueue(new okhttp3.Callback() {
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
                        .getBody()
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

        client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .certificatePinner(certsBuilder.build())
                .build();
    }

    public static void executeRawApiMethod(
            @NonNull String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params,
            RawCallback callback
    ) {
        String url = generateUrl(
                true,
                APIEndPoints.get(typeApi),
                443,
                new String[]{"methods", method, function},
                (requestType.equals("get")) ? params : null
        );

        client.newCall(_requestBuilder(
                requestType.equals("post"),
                url,
                params
        )).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        ErrorResponse responseJSON =
                                new Gson().fromJson(response.body().string(), ErrorResponse.class);
                        callback.onFailure(new APIException(
                                responseJSON.getErrorMessage(),
                                responseJSON.getCode()
                        ));
                        return;
                    }
                    callback.onSuccess(response);
                } catch (IOException e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    public static void executeApiMethod(
            @NonNull String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params,
            Callback callback
    ) {
        executeRawApiMethod(
                requestType,
                typeApi,
                method,
                function,
                params,
                new RawCallback() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onSuccess(Response response) {
                        try {
                            callback.onSuccess(new Gson().fromJson(response.body().string(), SuccessResponse.class));
                        } catch (IOException e) {
                            callback.onFailure(e);
                        }
                    }
                }
        );
    }

    private static Request _requestBuilder(
            boolean addParamsToBody,
            String url,
            Hashtable<String, String> arrayParams
    ) {
        Request.Builder request = new Request.Builder().url(url);

        if (addParamsToBody) {
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

    public interface RawCallback {
        void onFailure(Exception exception);
        void onSuccess(Response response);
    }

    public interface Callback {
        void onFailure(Exception exception);
        void onSuccess(SuccessResponse response);
    }
}
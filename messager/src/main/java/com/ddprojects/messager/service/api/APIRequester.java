package com.ddprojects.messager.service.api;

import static com.ddprojects.messager.service.globals.generateUrl;
import static com.ddprojects.messager.service.globals.appSimplePersistentData;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ddprojects.messager.BuildConfig;
import com.ddprojects.messager.models.ErrorResponse;
import com.ddprojects.messager.models.SuccessResponse;
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

    public static void setupApiClient(ClientReady ready) {
        if (BuildConfig.DEBUG) {
            try {
                @SuppressLint("CustomX509TrustManager")
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

                ready.onReady();

                return;
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                writeErrorInLog(e);
            }
        }

        Hashtable<String, String> getPinningHashParams = new Hashtable<>();
        getPinningHashParams.put(
                "domains",
                String.format(
                        "%s,%s",
                        BuildConfig.API_URL_GENERAL,
                        BuildConfig.API_URL_PRODUCT
                )
        );
        Request request = new Request.Builder()
                .url(generateUrl(
                        true,
                        BuildConfig.API_URL_GENERAL,
                        443,
                        new String[]{"methods", "service", "getPinningHashDomains"},
                        getPinningHashParams
                ))
                .build();

        CertificatePinner.Builder certsBuilder = new CertificatePinner.Builder();
        new OkHttpClient.Builder().build().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                writeErrorInLog(e);
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

                client = new OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS)
                        .certificatePinner(certsBuilder.build())
                        .build();

                ready.onReady();
            }
        });
    }

    public interface ClientReady {
        void onReady();
    }

    public static void executeRawApiMethod(
            @NonNull String requestType,
            String typeApi,
            String method,
            String function,
            @Nullable Hashtable<String, String> params,
            RawCallback callback
    ) {
        Request.Builder request = new Request.Builder().url(generateUrl(
                true,
                typeApi.equals("general") ? BuildConfig.API_URL_GENERAL : BuildConfig.API_URL_PRODUCT,
                443,
                new String[]{"methods", method, function},
                (requestType.equals("get")) ? params : null
        ));

        if (params != null && requestType.equals("post")) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            params.forEach(builder::addFormDataPart);

            request.post(builder.build());
        }

        request.header("session-id", appSimplePersistentData.getString("sessionId", ""));
        request.header("token", appSimplePersistentData.getString("token", ""));
        request.header("user-agent",
                String.format(Locale.US, "ddMessagerApp/%s", BuildConfig.VERSION_NAME)
        );

        client.newCall(request.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onRequestExecuteException(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        ErrorResponse responseJSON =
                                new Gson().fromJson(response.body().string(), ErrorResponse.class);
                        callback.onResponseError(new APIException(
                                responseJSON.getErrorMessage(),
                                responseJSON.getCode()
                        ));
                        return;
                    }
                    callback.onResponse(response);
                } catch (IOException e) {
                    callback.onRequestExecuteException(e);
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
                    public void onRequestExecuteException(Exception e) {
                        callback.onRequestExecuteException(e);
                    }

                    @Override
                    public void onResponseError(APIException e) {
                        callback.onResponseError(e);
                    }

                    @Override
                    public void onResponse(Response response) {
                        try {
                            callback.onSuccessResponse(
                                    new Gson().fromJson(
                                            response.body().string(),
                                            SuccessResponse.class
                                    )
                            );
                        } catch (IOException e) {
                            callback.onRequestExecuteException(e);
                        }
                    }
                }
        );
    }

    public interface RawCallback {
        void onRequestExecuteException(Exception exception);
        void onResponseError(APIException exception);
        void onResponse(Response response);
    }

    public interface Callback {
        void onRequestExecuteException(Exception exception);
        void onResponseError(APIException exception);
        void onSuccessResponse(SuccessResponse response);
    }
}
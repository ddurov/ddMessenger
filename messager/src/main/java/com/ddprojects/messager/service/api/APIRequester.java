package com.ddprojects.messager.service.api;

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
import java.util.Objects;
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
    public static final Hashtable<String, Object[]> APIEndPoints = new Hashtable<>();

    public static void setupApiClient() {
        APIEndPoints.put("general", new Object[]{"api.ddproj.ru", 443});
        APIEndPoints.put("product", new Object[]{"messager.api.ddproj.ru", 443});

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
                        .readTimeout(25, TimeUnit.SECONDS)
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

        if ((int) responseObject[0] >= 400) {
            ErrorResponse responseJSON = new Gson().fromJson((String) responseObject[1], ErrorResponse.class);
            throw new APIException(
                    responseJSON.getErrorMessage(),
                    responseJSON.getCode()
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
        client.newCall(_requestBuilder(url, arrayParams)).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cb.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    ErrorResponse responseJSON =
                            new Gson().fromJson(response.body().string(), ErrorResponse.class);
                    cb.onFailure(new APIException(
                            responseJSON.getErrorMessage(),
                            responseJSON.getCode()
                    ));
                    return;
                }
                cb.onSuccess(response);
            }
        });
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

    public interface Callback {
        void onFailure(Exception exception);
        void onSuccess(Response response) throws IOException;
    }

}

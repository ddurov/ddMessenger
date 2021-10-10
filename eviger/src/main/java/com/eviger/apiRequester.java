package com.eviger;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Objects;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class apiRequester extends AsyncTask<Void, Void, String> {

    private static final CertificatePinner certificatePinner = new CertificatePinner.Builder()
            .add("api.eviger.ru", "sha256/qzUNNashtNgPpKAQTGnADo08VhEgVN3r+gElpb0F5Qo=")
            .build();
    private static final OkHttpClient client = new OkHttpClient.Builder().certificatePinner(certificatePinner).build();
    private final Request request;

    public apiRequester(Request request) {
        this.request = request;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.e("l/e/requests", "Error code: "+response.code());
            }
            return Objects.requireNonNull(response.body()).string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}

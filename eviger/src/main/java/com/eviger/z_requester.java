package com.eviger;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class z_requester extends AsyncTask<Void, Void, String> {

    private final CertificatePinner certificatePinner = new CertificatePinner.Builder()
            .add("api.eviger.ru", "sha256/TiNyS1OoQIAzbv/Rc8rQkuplaF9mcu2Rcl/tUin1TAc=")
            .build();
    private final OkHttpClient client = new OkHttpClient.Builder().readTimeout(25, TimeUnit.SECONDS).certificatePinner(certificatePinner).build();
    private final Request request;

    public z_requester(Request request) {
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

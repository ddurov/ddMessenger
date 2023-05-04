package com.ddprojects.messager.debug;

import static com.ddprojects.messager.service.globals.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ddprojects.messager.R;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.listener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class routerActivities extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (new File(this.getDataDir(), "log.txt").delete())
            log("Logs deleted");

        if (!hasInternetConnection()) {
            showToastMessage("Отсутствует подключение к интернету", false);
            finish();
        }

        APIEndpoints.put("general", new Object[]{"development.ddproj.ru", 8000});
        APIEndpoints.put("product", new Object[]{"development.ddproj.ru", 8001});
        setupApiClient(false);

        Hashtable<String, String> params = new Hashtable<>();
        params.put("product", "messager");
        executeApiMethod(
                "get",
                "general",
                "updates",
                "get",
                params,
                new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        writeErrorInLog(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        liveData.put("updates", new Gson().toJson(result));
                    }
                });

        splashScreen.setKeepOnScreenCondition(() -> {
            if (liveData.containsKey("updates")) {
                startActivity(new Intent(this, welcomeActivity.class));
                finish();
                return false;
            }
            return true;
        });
    }
}
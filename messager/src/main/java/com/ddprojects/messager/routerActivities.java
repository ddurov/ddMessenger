package com.ddprojects.messager;

import static com.ddprojects.messager.services.globals.executeApiMethod;
import static com.ddprojects.messager.services.globals.hasInternetConnection;
import static com.ddprojects.messager.services.globals.initHTTPClient;
import static com.ddprojects.messager.services.globals.loadedData;
import static com.ddprojects.messager.services.globals.log;
import static com.ddprojects.messager.services.globals.writeErrorInLog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ddprojects.messager.services.fakeContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

public class routerActivities extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (new File(this.getDataDir(), "log.txt").delete())
            log("Logs deleted");

        if (!hasInternetConnection()) {
            Toast.makeText(
                    fakeContext.getInstance().getApplicationContext(),
                    "Отсутствует подключение к интернету",
                    Toast.LENGTH_LONG
            ).show();
            finish();
        }

        initHTTPClient(new String[]{"api.ddproj.ru", "messager.api.ddproj.ru"});

        splashScreen.setKeepOnScreenCondition(() -> {
            try {
                _loadData();
                if (Objects.requireNonNull(loadedData.get("updates")).getInt("code") != 200) {
                    Toast.makeText(
                            fakeContext.getInstance().getApplicationContext(),
                            "Ошибка загрузки данных",
                            Toast.LENGTH_LONG
                    ).show();
                    Thread.sleep(2000);
                    return true;
                }
            } catch (Exception ex) {
                writeErrorInLog(ex);
                finish();
            }
            startActivity(new Intent(this, welcomeActivity.class));
            finish();
            return false;
        });
    }

    private void _loadData() throws JSONException {
        loadedData.put("updates", new JSONObject(executeApiMethod(
            "get",
            "general",
            "updates",
            "get",
            new String[][]{{"product", "messager"}}))
        );
    }
}
package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.setupApiClient;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
import static com.ddprojects.messager.service.globals.liveData;
import static com.ddprojects.messager.service.globals.log;
import static com.ddprojects.messager.service.globals.showToastMessage;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.method.Updates;
import com.ddprojects.messager.service.api.models.Update;
import com.ddprojects.messager.service.globals;

import java.io.File;
import java.util.Hashtable;
import java.util.Objects;

public class routerActivities extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (new File(this.getDataDir(), "log.txt").delete()) log("Logs deleted");

        if (!hasInternetConnection()) {
            showToastMessage("Отсутствует подключение к интернету", false);
            finish();
        }

        setupApiClient(!BuildConfig.DEBUG);

        Hashtable<String, String> params = new Hashtable<>();
        params.put("product", "messager");
        new Thread() {
            @Override
            public void run() {
                try {
                    liveData.put("update", Updates.get(params));
                } catch (APIException API) {
                    if (API.getCode() == 404) {
                        liveData.put("update", new Update(
                                BuildConfig.VERSION_NAME,
                                "No changes are applied"
                        ));
                    }
                    globals.showToastMessage(
                            APIException.translate("updates", API.getMessage()),
                            false
                    );
                }
            }
        }.start();

        splashScreen.setKeepOnScreenCondition(() -> {
            if (liveData.containsKey("update")) {
                Update update = (Update) liveData.get("update");
                if (!Objects.equals(
                        Objects.requireNonNull(update).getVersion(),
                        BuildConfig.VERSION_NAME
                )) {
                    Intent intent = new Intent(this, updateActivity.class);
                    intent.putExtra("newVersion", update.getVersion());
                    intent.putExtra("description", update.getDescription());
                    startActivity(intent);
                    finish();
                    return false;
                }

                startActivity(new Intent(this, welcomeActivity.class));
                finish();
                return false;
            }
            return true;
        });
    }
}
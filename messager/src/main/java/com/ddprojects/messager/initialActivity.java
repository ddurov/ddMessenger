package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.api.APIRequester.setupApiClient;
import static com.ddprojects.messager.service.globals.PDDEditor;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
import static com.ddprojects.messager.service.globals.liveData;
import static com.ddprojects.messager.service.globals.writeMessageInLogCat;
import static com.ddprojects.messager.service.globals.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.models.Update;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.globals;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;

public class initialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        persistentDataOnDisk = getSharedPreferences("data", Context.MODE_PRIVATE);
        PDDEditor = persistentDataOnDisk.edit();

        if (new File(this.getDataDir(), "log.txt").delete()) writeMessageInLogCat("Logs deleted");

        if (!hasInternetConnection()) {
            showToastMessage(getString(R.string.error_internet_unavailable), false);
            finish();
        }

        setupApiClient();

        Hashtable<String, String> updatesGetParams = new Hashtable<>();
        updatesGetParams.put("product", "messager");

        new Thread(() -> {
            try {
                Update response = (Update) executeApiMethodSync(
                        "get",
                        "general",
                        "updates",
                        "get",
                        updatesGetParams
                );

                liveData.put("update", new Update(
                        response.getVersion(),
                        response.getDescription()
                ));
            } catch (APIException API) {
                if (API.getCode() == 404) {
                    liveData.put("update", new Update(
                            BuildConfig.VERSION_NAME,
                            "No changes are applied"
                    ));
                } else {
                    globals.showToastMessage(
                            APIException.translate("user", API.getMessage()),
                            false
                    );
                }
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            }
        }).start();

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
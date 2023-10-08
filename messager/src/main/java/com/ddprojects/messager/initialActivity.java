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
import com.ddprojects.messager.models.Update;
import com.ddprojects.messager.service.fakeContext;
import com.ddprojects.messager.service.globals;
import com.google.gson.Gson;

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
            Update newUpdate = new Update(
                    BuildConfig.VERSION_NAME,
                    "No changes are applied"
            );

            try {
                Update response = new Gson().fromJson(executeApiMethodSync(
                        "get",
                        "general",
                        "updates",
                        "get",
                        updatesGetParams
                ).getBody(), Update.class);

                newUpdate.setVersion(response.getVersion());
                newUpdate.setDescription(response.getDescription());

                liveData.put("update", newUpdate);
            } catch (APIException API) {
                globals.showToastMessage(
                        APIException.translate("updates", API.getMessage()),
                        false
                );
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            } finally {
                liveData.put("update", newUpdate);
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

                if (
                        persistentDataOnDisk.contains("sessionId") &&
                                persistentDataOnDisk.contains("token")
                ) {
                    startActivity(new Intent(this, dialogsActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(this, welcomeActivity.class));
                    finish();
                }
                return false;
            }
            return true;
        });
    }
}
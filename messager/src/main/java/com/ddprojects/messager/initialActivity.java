package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethod;
import static com.ddprojects.messager.service.api.APIRequester.setupApiClient;
import static com.ddprojects.messager.service.globals.cachedData;
import static com.ddprojects.messager.service.globals.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;
import static com.ddprojects.messager.service.globals.writeMessageInLogCat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ddprojects.messager.models.SuccessResponse;
import com.ddprojects.messager.models.Update;
import com.ddprojects.messager.models.User;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.appService;
import com.ddprojects.messager.service.cacheService;
import com.google.gson.Gson;

import java.io.File;
import java.util.Hashtable;
import java.util.Objects;

public class initialActivity extends AppCompatActivity {
    protected boolean splashScreenKeep = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (new File(this.getFilesDir(), "log.txt").delete()) writeMessageInLogCat("Logs deleted");

        setupApiClient();

        cachedData.setOnEventListener(cacheService::updateInstance);

        Hashtable<String, String> infoParams = new Hashtable<>();
        infoParams.put("product", "messager");

        executeApiMethod(
                "get",
                "general",
                "updates",
                "info",
                infoParams,
                new APIRequester.Callback() {
                    @Override
                    public void onFailure(Exception exception) {
                        if (exception instanceof APIException) {
                            showToastMessage(
                                    APIException.translate(exception.getMessage()),
                                    false
                            );
                        } else {
                            writeErrorInLog(exception);
                        }
                        splashScreenKeep = false;
                        startActivity(new Intent(initialActivity.this, welcomeActivity.class));
                        finish();
                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        Update update = new Gson().fromJson(
                                response.getBody(),
                                Update.class
                        );

                        if (!Objects.equals(update.getVersionCode(), BuildConfig.VERSION_CODE)) {
                            Intent intent = new Intent(
                                    initialActivity.this,
                                    updateActivity.class
                            );
                            intent.putExtra("newVersionName", update.getVersionName());
                            intent.putExtra("newVersionCode", update.getVersionCode());
                            intent.putExtra("description", update.getDescription());
                            splashScreenKeep = false;
                            startActivity(intent);
                            finish();
                        } else {
                            if (
                                    persistentDataOnDisk.contains("sessionId") &&
                                            persistentDataOnDisk.contains("token")
                            ) {
                                executeApiMethod(
                                        "get",
                                        "product",
                                        "user",
                                        "get",
                                        new Hashtable<>(),
                                        new APIRequester.Callback() {
                                            @Override
                                            public void onFailure(Exception exception) {
                                                if (exception instanceof APIException) {
                                                    showToastMessage(
                                                            APIException.translate(exception.getMessage()),
                                                            false
                                                    );
                                                } else {
                                                    writeErrorInLog(exception);
                                                }
                                                splashScreenKeep = false;
                                                startActivity(new Intent(initialActivity.this, welcomeActivity.class));
                                                finish();
                                            }

                                            @Override
                                            public void onSuccess(SuccessResponse response) {
                                                cachedData.put("user", new Gson().fromJson(
                                                        response.getBody(),
                                                        User.class
                                                ));

                                                startService(new Intent(initialActivity.this, appService.class));
                                                splashScreenKeep = false;
                                                startActivity(new Intent(initialActivity.this, dialogsActivity.class));
                                                finish();
                                            }
                                        }
                                );
                            } else {
                                splashScreenKeep = false;
                                startActivity(new Intent(initialActivity.this, welcomeActivity.class));
                                finish();
                            }
                        }
                    }
                }
        );

        splashScreen.setKeepOnScreenCondition(() -> splashScreenKeep);
    }
}
package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodAsync;
import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.fakeContext.liveData;
import static com.ddprojects.messager.service.fakeContext.persistentDataOnDisk;
import static com.ddprojects.messager.service.globals.appInitVars;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
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
import com.ddprojects.messager.service.fakeContext;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;

import okhttp3.Response;

public class initialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (new File(this.getDataDir(), "log.txt").delete()) writeMessageInLogCat("Logs deleted");

        if (!hasInternetConnection()) {
            showToastMessage(getString(R.string.error_internet_unavailable), false);
            finish();
        }

        appInitVars();

        new Thread(() -> {
            try {
                Hashtable<String, String> updatesGetParams = new Hashtable<>();
                updatesGetParams.put("product", "messager");

                Update response = new Gson().fromJson(executeApiMethodSync(
                        "get",
                        "general",
                        "updates",
                        "get",
                        updatesGetParams
                ).getBody(), Update.class);

                if (!Objects.equals(response.getVersion(), BuildConfig.VERSION_NAME)) {
                    liveData.put("update", new Update(
                            response.getVersion(),
                            response.getDescription()
                    ));
                }
            } catch (APIException APIex) {
                if (APIex.getCode() != 404) showToastMessage(
                        APIException.translate(APIex.getMessage()),
                        false
                );
            } catch (IOException IOEx) {
                writeErrorInLog(IOEx);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_request_failed),
                        false
                );
            }
        }).start();

        splashScreen.setKeepOnScreenCondition(() -> {
            if (liveData.contains("update")) {
                Update update = (Update) liveData.get("update");

                Intent intent = new Intent(this, updateActivity.class);
                intent.putExtra("newVersion", update.getVersion());
                intent.putExtra("description", update.getDescription());
                startActivity(intent);
                finish();

                liveData.remove("update");
            } else {
                if (
                        persistentDataOnDisk.contains("sessionId") &&
                                persistentDataOnDisk.contains("token")
                ) {
                    executeApiMethodAsync(
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
                                        showToastMessage(
                                                getString(R.string.error_request_failed),
                                                false
                                        );
                                    }
                                }

                                @Override
                                public void onSuccess(Response response) throws IOException {
                                    JsonElement userJson = new Gson().fromJson(
                                            response.body().string(),
                                            SuccessResponse.class
                                    ).getBody();
                                    liveData.put("user", new Gson().fromJson(userJson, User.class));

                                    startService(new Intent(initialActivity.this, appService.class));
                                    startActivity(new Intent(initialActivity.this, dialogsActivity.class));
                                    finish();
                                }
                            }
                    );
                    return false;
                } else {
                    startActivity(new Intent(this, welcomeActivity.class));
                    finish();
                }
            }
            return false;
        });
    }
}
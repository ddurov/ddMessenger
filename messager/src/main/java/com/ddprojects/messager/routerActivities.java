package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.api.APIRequester.setupApiClient;
import static com.ddprojects.messager.service.globals.hasInternetConnection;
import static com.ddprojects.messager.service.globals.liveData;
import static com.ddprojects.messager.service.globals.log;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.models.Update;
import com.ddprojects.messager.service.globals;

import org.json.JSONException;
import org.json.JSONObject;

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

        setupApiClient();

        Hashtable<String, String> params = new Hashtable<>();
        params.put("product", "messager");
        new Thread(() -> {
            String response = null;
            try {
                response = executeApiMethodSync(
                        "get",
                        "general",
                        "updates",
                        "get",
                        params
                );

                JSONObject responseAsObjects
                        = new JSONObject(response).getJSONObject("body");

                liveData.put("update", new Update(
                        responseAsObjects.getString("version"),
                        responseAsObjects.getString("description")
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
            } catch (JSONException JSONEx) {
                writeErrorInLog(JSONEx, "Response updates/get: " + response);
                globals.showToastMessage(
                        getString(R.string.error_responseReadingFailed),
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
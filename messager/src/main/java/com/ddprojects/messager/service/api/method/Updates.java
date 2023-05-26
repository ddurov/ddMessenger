package com.ddprojects.messager.service.api.method;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import com.ddprojects.messager.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class Updates {

    public Update get(Hashtable<String, String> params) {
        String response = executeApiMethodSync(
                "get",
                "general",
                "updates",
                "get",
                params
        );

        if (response != null) {
            try {
                JSONObject responseAsObjects = new JSONObject(response).getJSONObject("body");

                return new Update(
                        responseAsObjects.getString("version"),
                        responseAsObjects.getString("description")
                );
            } catch (JSONException e) {
                writeErrorInLog(e);
                showToastMessage("Произошла ошибка при чтении ответа сервера", false);
            }
        }

        return new Update(
                BuildConfig.VERSION_NAME,
                "No changes are applied"
        );
    }

    public static class Update {
        private String version;
        private String description;

        public Update(String version, String description) {
            this.version = version;
            this.description = description;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

package com.ddprojects.messager.service.api.method;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import com.ddprojects.messager.BuildConfig;
import com.ddprojects.messager.R;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.models.Update;
import com.ddprojects.messager.service.fakeContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class Updates {
    public static Update get(Hashtable<String, String> params) throws APIException {
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
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_responseReadingFailed),
                        false
                );
            }
        }

        return new Update(
                BuildConfig.VERSION_NAME,
                "No changes are applied"
        );
    }
}

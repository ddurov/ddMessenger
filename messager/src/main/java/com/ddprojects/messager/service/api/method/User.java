package com.ddprojects.messager.service.api.method;

import static com.ddprojects.messager.service.api.APIRequester.executeApiMethodSync;
import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import com.ddprojects.messager.R;
import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.fakeContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class User {
    public static int register(Hashtable<String, String> params) throws APIException {
        String response = executeApiMethodSync(
                "post",
                "product",
                "user",
                "register",
                params
        );

        if (response != null) {
            try {
                return new JSONObject(response).getInt("body");
            } catch (JSONException e) {
                writeErrorInLog(e);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_responseReadingFailed),
                        false
                );
            }
        }

        return 0;
    }

    public static String auth(Hashtable<String, String> params) throws APIException {
        String response = executeApiMethodSync(
                "get",
                "product",
                "user",
                "auth",
                params
        );

        if (response != null) {
            try {
                return new JSONObject(response).getString("body");
            } catch (JSONException e) {
                writeErrorInLog(e);
                showToastMessage(
                        fakeContext.getInstance().getString(R.string.error_responseReadingFailed),
                        false
                );
            }
        }

        return null;
    }
}

package com.eviger;

import static com.eviger.z_globals.dialogs;
import static com.eviger.z_globals.executeApiMethodGet;
import static com.eviger.z_globals.getProfileById;
import static com.eviger.z_globals.getToken;
import static com.eviger.z_globals.hasConnection;
import static com.eviger.z_globals.log;
import static com.eviger.z_globals.showOrWriteError;
import static com.eviger.z_globals.stackTraceToString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class splashScreen extends AppCompatActivity {

    public static String version = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        TextView statusApp = findViewById(R.id.statusApp);

        if (new File(this.getDataDir(), "log.txt").delete()) {
            Log.e("l/e", "Logs deleted");
        }

        new Thread(() -> {

            while (true) {

                try {

                    if (hasConnection(getApplicationContext())) {

                        runOnUiThread(() -> statusApp.setText("Данные загружаются.."));
                        Thread.sleep(500);

                        try {

                            JSONObject update = new JSONObject(executeApiMethodGet("service", "getUpdates", new String[][]{{}}));

                            if (!update.getJSONObject("response").getString("version").equals(version)) {

                                startActivity(new Intent(splashScreen.this, updateApp.class));
                                finish();

                            }

                            if (getToken() == null) {

                                startActivity(new Intent(splashScreen.this, chooseAuth.class));
                                finish();

                            }

                            JSONObject data = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));

                            if (data.getString("status").equals("ok")) {

                                if (z_globals.tokenSet.getBoolean("isSigned", false)) {

                                    z_globals.myProfile = data.getJSONObject("response");

                                    JSONArray responseGetDialogs = new JSONObject(executeApiMethodGet("messages", "getDialogs", new String[][]{{}})).getJSONArray("response");

                                    for (int i = 0; i < responseGetDialogs.length(); i++) {
                                        dialogs.add(new Object[]{responseGetDialogs.getJSONObject(i).getInt("peer_id"),
                                                new z_dialog(responseGetDialogs.getJSONObject(i).getInt("peer_id"),
                                                        (String) getProfileById(responseGetDialogs.getJSONObject(i).getInt("peer_id"))[1],
                                                        new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new java.util.Date(responseGetDialogs.getJSONObject(i).getInt("date") * 1000L)),
                                                        responseGetDialogs.getJSONObject(i).getString("message").replaceAll("\\n", ""))});
                                    }

                                    startActivity(new Intent(splashScreen.this, profilePage.class));

                                } else {

                                    startActivity(new Intent(splashScreen.this, chooseAuth.class));

                                }
                                finish();

                            } else {

                                String message = data.getJSONObject("response").getString("message");

                                switch (message) {

                                    case "token not found":
                                        startActivity(new Intent(splashScreen.this, chooseAuth.class));
                                        finish();
                                        break;

                                    case "account banned":
                                        Intent in = new Intent(splashScreen.this, restoreUserPage.class);
                                        in.putExtra("reason", data.getJSONObject("response").getJSONObject("details").getString("error"));
                                        in.putExtra("canRestore", data.getJSONObject("response").getJSONObject("details").getBoolean("canRestore"));
                                        startActivity(in);
                                        finish();
                                        break;

                                    default:
                                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());

                                }

                            }

                        } catch (Throwable ex) {
                            runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex)));
                        }
                        break;

                    } else {
                        runOnUiThread(() -> statusApp.setText("Отсутствует подключение к интернету"));
                    }
                    Thread.sleep(500);

                } catch (Exception ex) {
                    runOnUiThread(() -> showOrWriteError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex)));
                }

            }

        }).start();

    }
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
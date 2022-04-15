package com.eviger;

import static com.eviger.z_globals.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.CertificatePinner;

@SuppressLint("CustomSplashScreen")
public class splashScreen extends AppCompatActivity {

    public static String version = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        TextView statusApp = findViewById(R.id.statusApp);

        initHTTPClient("api.eviger.ru");

        if (new File(this.getDataDir(), "log.txt").delete()) {
           log("Logs deleted");
        }

        new Thread(() -> {

            while (true) {

                try {

                    if (hasConnection(getApplicationContext())) {

                        runOnUiThread(() -> statusApp.setText("Данные загружаются.."));
                        Thread.sleep(500);

                        try {

                            JSONObject update = new JSONObject(executeApiMethodGet("service", "getUpdates", new String[][]{}));

                            if (!update.getJSONObject("response").getString("version").equals(version)) {

                                startActivity(new Intent(splashScreen.this, updateApp.class));
                                finish();
                                break;

                            }

                            if (getToken() == null) {

                                startActivity(new Intent(splashScreen.this, chooseAuth.class));
                                finish();
                                break;

                            }

                            JSONObject data = new JSONObject(executeApiMethodGet("users", "get", new String[][]{}));

                            if (data.getString("status").equals("ok")) {

                                if (z_globals.tokenSet.getBoolean("isSigned", false)) {

                                    myProfile = data.getJSONObject("response");

                                    JSONArray responseGetDialogs = new JSONObject(executeApiMethodGet("messages", "getDialogs", new String[][]{})).getJSONArray("response");

                                    for (int i = 0; i < responseGetDialogs.length(); i++) {
                                        dialogs.add(
                                                new z_dialog(responseGetDialogs.getJSONObject(i).getInt("peerId"),
                                                        (String) getProfileById(responseGetDialogs.getJSONObject(i).getInt("peerId"))[1],
                                                        new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new Date(responseGetDialogs.getJSONObject(i).getInt("lastMessageDate") * 1000L)),
                                                        responseGetDialogs.getJSONObject(i).getString("lastMessage").replaceAll("\\n", "")));
                                    }

                                    startActivity(new Intent(splashScreen.this, profilePage.class));

                                } else {

                                    startActivity(new Intent(splashScreen.this, chooseAuth.class));

                                }

                            } else {

                                String message = data.getJSONObject("response").getString("message");

                                switch (message) {

                                    case "token not found":
                                        startActivity(new Intent(splashScreen.this, chooseAuth.class));
                                        break;

                                    case "account banned":
                                        Intent in = new Intent(splashScreen.this, restoreProfile.class);
                                        in.putExtra("reason", data.getJSONObject("response").getJSONObject("details").getString("error"));
                                        in.putExtra("canRestore", data.getJSONObject("response").getJSONObject("details").getBoolean("canRestore"));
                                        startActivity(in);
                                        break;

                                    default:
                                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());

                                }

                            }
                            finish();
                            break;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;

                    } else {
                        runOnUiThread(() -> statusApp.setText("Отсутствует подключение к интернету"));
                    }
                    Thread.sleep(500);

                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        writeErrorInLog(ex);
                    });
                }

            }

        }).start();

    }

}
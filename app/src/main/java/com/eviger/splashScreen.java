package com.eviger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.getToken;
import static com.eviger.globals.showHumanReadlyTextError;
import static com.eviger.globals.stackTraceToString;

public class splashScreen extends AppCompatActivity {

    public static final String version = BuildConfig.VERSION_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        TextView statusApp = findViewById(R.id.statusApp);

        if (new File(this.getDataDir(),"log.txt").delete()) {
            Log.e("l/e", "Logs deleted");
        }

        Thread run = new Thread(() -> {

            while(true) {

                try {

                    if (globals.hasConnection(getApplicationContext())) {
                        runOnUiThread(() -> statusApp.setText("Данные загружаются.."));
                        Thread.sleep(400);
                        ret();
                        break;
                    } else {
                        runOnUiThread(() -> statusApp.setText("Подключитесь к интернету."));
                    }
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        });
        run.start();

    }
    private void ret() {

        try {

            JSONObject update = new JSONObject(executeApiMethodGet("service", "getUpdates", new String[][]{{}}));

            if (update.getJSONObject("response").getString("version").equals(version)) {

                boolean tokenNulled = getToken() == null;

                if (tokenNulled) {

                    startActivity(new Intent(splashScreen.this, chooseAuth.class));
                    finish();

                } else {

                    JSONObject data = new JSONObject(executeApiMethodGet("users", "get", new String[][]{{}}));

                    if (!data.getJSONObject("response").has("error")) {

                        if (globals.tokenSet.getBoolean("isSigned", false)) {

                            startActivity(new Intent(splashScreen.this, profilePage.class));

                        } else {

                            startActivity(new Intent(splashScreen.this, chooseAuth.class));

                        }
                        finish();

                    } else {

                        if (data.getJSONObject("response").getString("error").equals("token not found")) {

                            startActivity(new Intent(splashScreen.this, chooseAuth.class));
                            finish();

                        } else {

                            if (data.getJSONObject("response").has("error")) {

                                if (data.getJSONObject("response").getString("error").equals("token inactive due hacking") || data.getJSONObject("response").getString("error").equals("token inactive due delete profile at own request")) {

                                    Intent in = new Intent(splashScreen.this, restoreUserPage.class);
                                    in.putExtra("reason", data.getJSONObject("response").getString("error"));
                                    in.putExtra("canRestore", data.getJSONObject("response").getBoolean("canRestore"));
                                    startActivity(in);
                                    finish();

                                }

                            }

                        }

                    }

                }

            } else {

                startActivity(new Intent(splashScreen.this, updateApp.class));
                finish();

            }

        } catch (Throwable ex) {
            runOnUiThread(() -> showHumanReadlyTextError(Objects.requireNonNull(ex.getMessage()), stackTraceToString(ex), this));
        }

    }

}
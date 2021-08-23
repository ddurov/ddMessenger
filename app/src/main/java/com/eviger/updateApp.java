package com.eviger;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;

import static com.eviger.globals.executeApiMethodGet;
import static com.eviger.globals.grantPermissionStorage;
import static com.eviger.globals.hasConnection;

public class updateApp extends AppCompatActivity {

    @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_app);

        TextView currentVersionApp = findViewById(R.id.currentVersionApp);
        TextView descriptionUpdateApp2 = findViewById(R.id.descriptionUpdateApp2);
        Button accept = findViewById(R.id.update_accept);
        grantPermissionStorage(updateApp.this);

        if (hasConnection(getApplicationContext())) {

            try {

                JSONObject updateJson = new JSONObject(executeApiMethodGet("service", "getUpdates", new String[][]{}));
                currentVersionApp.setText("На сервере найдено новое обновление! Текущая версия " + splashScreen.version + "\nОбновлённая версия " + updateJson.getJSONObject("response").getDouble("version"));
                descriptionUpdateApp2.setText(updateJson.getJSONObject("response").getString("changelog"));

                accept.setOnClickListener(v -> {

                    try {

                        Toast.makeText(getApplicationContext(), "Обновление загружается...", Toast.LENGTH_SHORT).show();

                        String dl = updateJson.getJSONObject("response").getString("download_link");

                        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File finalFile = new File(downloads + "/eviger/eviger-update.apk");
                        Uri content = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", finalFile);

                        DownloadManager DownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                        Uri Download_Uri = Uri.parse(dl);

                        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                        request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
                        request.setAllowedOverRoaming(false);
                        request.setTitle("eviger-update.apk");
                        request.setDescription("eviger-update.apk");
                        request.setVisibleInDownloadsUi(true);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/eviger/eviger-update.apk");

                        DownloadManager.enqueue(request);

                        BroadcastReceiver onComplete = new BroadcastReceiver() {

                            public void onReceive(Context ctxt, Intent intent) {

                                Intent intentD = new Intent(Intent.ACTION_VIEW);
                                intentD.setDataAndType(content, "application/vnd.android.package-archive");
                                intentD.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intentD.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(intentD);

                            }

                        };

                        registerReceiver(onComplete, new IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    } catch (Throwable ex) {
                        runOnUiThread(() -> Toast.makeText(this, "Ошибка: "+ex.getMessage(), Toast.LENGTH_LONG).show());
                    }

                });

            } catch (Throwable ex) {
                runOnUiThread(() -> Toast.makeText(this, "Ошибка: "+ex.getMessage(), Toast.LENGTH_LONG).show());
            }

        } else {

            Toast.makeText(getApplicationContext(), "Подключитесь к интернету!", Toast.LENGTH_LONG).show();

        }

    }

}
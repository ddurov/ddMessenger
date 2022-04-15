package com.eviger;

import static com.eviger.z_globals.*;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;

public class updateApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_app);

        TextView currentVersionApp = findViewById(R.id.currentVersionApp);
        TextView descriptionUpdateApp2 = findViewById(R.id.changeList);
        Button accept = findViewById(R.id.update_accept);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (!hasConnection(getApplicationContext()))
            Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

        try {

            JSONObject updateJson = new JSONObject(executeApiMethodGet("service", "getUpdates", new String[][]{}));
            currentVersionApp.setText("Найдено новое обновление! Текущая версия " + splashScreen.version + "\nОбновлённая версия " + updateJson.getJSONObject("response").getString("version"));
            descriptionUpdateApp2.setText(updateJson.getJSONObject("response").getString("changelog"));

            accept.setOnClickListener(v -> {

                try {

                    Toast.makeText(getApplicationContext(), "Обновление загружается...", Toast.LENGTH_SHORT).show();

                    File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File finalFile = new File(downloads + "/eviger-release.apk");
                    Uri content = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", finalFile);

                    DownloadManager DownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                    Uri Download_Uri = Uri.parse(updateJson.getJSONObject("response").getString("download_link"));

                    DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                    request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
                    request.setAllowedOverRoaming(false);
                    request.setTitle("eviger-release.apk");
                    request.setDescription("Загрузка обновления");
                    request.setVisibleInDownloadsUi(true);
                    request.setDestinationInExternalPublicDir(downloads.toString(), "/eviger-release.apk");

                    DownloadManager.enqueue(request);

                    BroadcastReceiver onComplete = new BroadcastReceiver() {

                        public void onReceive(Context ctxt, Intent intent) {

                            Intent intentDownloaded = new Intent(Intent.ACTION_VIEW);
                            intentDownloaded.setDataAndType(content, "application/vnd.android.package-archive");
                            intentDownloaded.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intentDownloaded);

                        }

                    };

                    registerReceiver(onComplete, new IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        writeErrorInLog(ex);
                    });
                }

            });

        } catch (Exception ex) {
            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                writeErrorInLog(ex);
            });
        }

    }

}
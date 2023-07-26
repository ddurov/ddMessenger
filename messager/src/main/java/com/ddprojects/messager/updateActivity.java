package com.ddprojects.messager;

import static com.ddprojects.messager.service.api.APIRequester.APIEndPoints;
import static com.ddprojects.messager.service.globals.generateUrl;

import android.annotation.SuppressLint;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.ddprojects.messager.service.fakeContext;

import java.io.File;
import java.util.Hashtable;
import java.util.Objects;

public class updateActivity extends AppCompatActivity {

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        TextView newVersionFound = findViewById(R.id.newVersionFound);
        TextView updateDescription = findViewById(R.id.updateDescription);
        Button update = findViewById(R.id.updateButton);

        newVersionFound.setText(
                getString(R.string.updateNewVersionFound)
                        .replace(
                                "{version}",
                                Objects.requireNonNull(getIntent().getStringExtra("newVersion"))
                        )
        );
        updateDescription.setText(getIntent().getStringExtra("description"));

        update.setOnClickListener(v -> {
            Hashtable<String, String> params = new Hashtable<>();
            params.put("product", "messager");
            params.put("version", getIntent().getStringExtra("newVersion"));

            if (ActivityCompat.checkSelfPermission(
                    getApplicationContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        updateActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1
                );
            }

            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File finalFile = new File(downloads + "/update.apk");
            Uri content = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    finalFile
            );

            DownloadManager DownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(generateUrl(
                            false,
                            (String) Objects.requireNonNull(APIEndPoints.get("general"))[0],
                            (int) Objects.requireNonNull(APIEndPoints.get("general"))[1],
                            new String[]{"methods", "updates", "download"},
                            params
                    ))
            );
            request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            request.setTitle("update.apk");
            request.setDescription("Download update...");
            request.setVisibleInDownloadsUi(true);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/update.apk");

            DownloadManager.enqueue(request);

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Intent intentDownloaded = new Intent(Intent.ACTION_VIEW);
                    intentDownloaded.setDataAndType(content, "application/vnd.android.package-archive");
                    intentDownloaded.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intentDownloaded);
                }
            };

            registerReceiver(onComplete, new IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        });
    }
}
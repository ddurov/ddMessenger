package com.ddprojects.messager;

import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.ddprojects.messager.service.api.APIException;
import com.ddprojects.messager.service.api.APIRequester;
import com.ddprojects.messager.service.binaryFileWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Objects;

import okhttp3.Response;

public class updateActivity extends AppCompatActivity {
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
                                getIntent().getStringExtra("newVersionName")
                        )
                        .replace(
                                "{version_code}",
                                String.valueOf(getIntent().getIntExtra("newVersionCode", 1))
                        )
        );
        updateDescription.setText(getIntent().getStringExtra("description"));

        update.setOnClickListener(v -> {
            update.setEnabled(false);

            Hashtable<String, String> getParams = new Hashtable<>();
            getParams.put("product", "messager");
            getParams.put("versionName", getIntent().getStringExtra("newVersionName"));

            File finalFile = new File(getFilesDir() + "/update.apk");
            Uri content = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    finalFile
            );

            APIRequester.executeRawApiMethod(
                    "get",
                    "general",
                    "updates",
                    "get",
                    getParams,
                    new APIRequester.RawCallback() {
                        @Override
                        public void onFailure(Exception exception) {
                            if (exception instanceof APIException) {
                                showToastMessage(
                                        APIException.translate(exception.getMessage()),
                                        false
                                );
                            } else {
                                writeErrorInLog(exception);
                            }
                        }

                        @Override
                        public void onSuccess(Response response) {
                            try {
                                (new binaryFileWriter(
                                        new FileOutputStream(finalFile),
                                        progress -> {
                                            update.setText(String.format("%s%%", progress));
                                            if (progress >= 100) {
                                                Intent intentDownloaded = new Intent(Intent.ACTION_VIEW);
                                                intentDownloaded.setDataAndType(content, "application/vnd.android.package-archive");
                                                intentDownloaded.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                startActivity(intentDownloaded);

                                                runOnUiThread(() -> {
                                                    update.setText(R.string.updateStartButton);
                                                    update.setOnClickListener(V -> startActivity(intentDownloaded));
                                                    update.setEnabled(true);
                                                });
                                            }
                                        }
                                )).write(
                                        response.body().byteStream(),
                                        Double.parseDouble(Objects.requireNonNull(
                                                response.header("Content-Length", "1")
                                        ))
                                );
                            } catch (IOException e) {
                                writeErrorInLog(e);
                            }
                        }
                    }
            );
        });
    }
}
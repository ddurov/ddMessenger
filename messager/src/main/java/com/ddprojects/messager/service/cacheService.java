package com.ddprojects.messager.service;

import static com.ddprojects.messager.service.globals.showToastMessage;
import static com.ddprojects.messager.service.globals.writeErrorInLog;
import static com.ddprojects.messager.service.globals.writeMessageInLogCat;

import com.ddprojects.messager.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class cacheService {
    private static final File cacheFile = new File(fakeContext.getInstance().getCacheDir(),"/data.json");

    public static HashMap<Object, Object> getInstance() {
        try {
            if (cacheFile.createNewFile()) {
                writeMessageInLogCat("Cache file created");
                updateInstance(new HashMap<>());
            }
            return new Gson().fromJson(
                    new FileReader(cacheFile),
                    new TypeToken<HashMap<Object, Object>>(){}.getType()
            );
        } catch (IOException IOEx) {
            writeErrorInLog(IOEx);
            showToastMessage(
                    fakeContext.getInstance().getString(R.string.error_internal),
                    false
            );
        }
        return null;
    }

    public static void updateInstance(HashMap<Object, Object> instance) {
        try (Writer writer = new FileWriter(cacheFile)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .create();
            gson.toJson(instance, writer);
        } catch (IOException IOEx) {
            writeErrorInLog(IOEx);
            showToastMessage(
                    fakeContext.getInstance().getString(R.string.error_internal),
                    false
            );
        }
    }
}

package com.ddprojects.messager.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

public class cacheService {
    private static final File cacheFile = new File(fakeContext.getInstance().getFilesDir(),"/app.dat");
    public static Hashtable<Object, Object> getInstance() throws IOException, ClassNotFoundException {
        if (!cacheFile.exists()) {
            FileOutputStream fileOutStream = new FileOutputStream(cacheFile);
            ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
            objectOutStream.writeObject(new observableHashtable<>());
            objectOutStream.flush();
            objectOutStream.close();
            fileOutStream.close();
        }
        FileInputStream fileInStream = new FileInputStream(fakeContext.getInstance().getFilesDir() + "/app.dat");
        ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
        Hashtable<Object, Object> result = (Hashtable<Object, Object>) objectInStream.readObject();
        objectInStream.close();
        fileInStream.close();
        return result;
    }

    public static void updateInstance(Hashtable<Object, Object> instance) throws IOException {
        FileOutputStream fileOutStream = new FileOutputStream(cacheFile);
        ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
        objectOutStream.writeObject(instance);
        objectOutStream.flush();
        objectOutStream.close();
        fileOutStream.close();
    }

}

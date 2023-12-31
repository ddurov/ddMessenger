package com.ddprojects.messager.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class binaryFileWriter implements AutoCloseable {
    private static final int CHUNK_SIZE = 1024;
    private final OutputStream outputStream;
    private final ProgressCallback progressCallback;

    public binaryFileWriter(OutputStream outputStream, ProgressCallback progressCallback) {
        this.outputStream = outputStream;
        this.progressCallback = progressCallback;
    }

    public void write(InputStream inputStream, double length) throws IOException {
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            byte[] dataBuffer = new byte[CHUNK_SIZE];
            int readBytes;
            long totalBytes = 0;
            while ((readBytes = input.read(dataBuffer)) != -1) {
                totalBytes += readBytes;
                outputStream.write(dataBuffer, 0, readBytes);
                progressCallback.onProgress(Double.parseDouble(new DecimalFormat("#.##").format(totalBytes / length * 100.0)));
            }
        }
    }

    public interface ProgressCallback {
        void onProgress(double progress);
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
    }
}
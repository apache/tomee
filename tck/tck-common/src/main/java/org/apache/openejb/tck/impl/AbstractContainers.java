package org.apache.openejb.tck.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;

public class AbstractContainers {
    protected static final String tmpDir = System.getProperty("java.io.tmpdir");

    protected void writeToFile(File file, InputStream archive) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = archive.read(buffer)) > -1) {
                fos.write(buffer, 0, bytesRead);
            }
            Util.close(fos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }
        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    protected static final class Util {
        static void close(Closeable closeable) throws IOException {
            if (closeable == null)
                return;
            try {
                if (closeable instanceof Flushable) {
                    ((Flushable) closeable).flush();
                }
            } catch (IOException e) {
                // no-op
            }
            try {
                closeable.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }
}

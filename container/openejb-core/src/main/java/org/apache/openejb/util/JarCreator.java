package org.apache.openejb.util;

import org.apache.openejb.loader.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class JarCreator {
    private static final int BUFFER_SIZE = 1024;

    private JarCreator() {
        // no-op
    }

    public static void jarDir(final File dir, final File zipName) throws IOException, IllegalArgumentException {
        final String[] entries = dir.list();
        final JarOutputStream out = new JarOutputStream(new FileOutputStream(zipName));
        for (String entry : entries) {
            File f = new File(dir, entry);
            jarFile(out, f);
        }
        IO.close(out);
    }

    private static void jarFile(final JarOutputStream out, final File f) throws IOException {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                jarFile(out, child);
            }
        } else {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            final String path = f.getPath();

            final FileInputStream in = new FileInputStream(f);
            final JarEntry entry = new JarEntry(path);
            out.putNextEntry(entry);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            IO.close(in);
        }
    }
}

package org.apache.openejb.maven.plugin.util;

import org.apache.openejb.loader.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Zips {
    private static final int BUFFER_SIZE = 1024;

    private Zips() {
        // no-op
    }

    public static void zip(final File dir, final File zipName) throws IOException, IllegalArgumentException {
        final String[] entries = dir.list();
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipName));

        String prefix = dir.getAbsolutePath();
        if (!prefix.endsWith(File.separator)) {
            prefix += File.separator;
        }

        for (String entry : entries) {
            File f = new File(dir, entry);
            zip(out, f, prefix);
        }
        IO.close(out);
    }

    private static void zip(final ZipOutputStream out, final File f, final String prefix) throws IOException {
        if (f.isDirectory()) {
            final File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) {
                    zip(out, child, prefix);
                }
            }
        } else {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            final String path = f.getPath().replace(prefix, "");

            final FileInputStream in = new FileInputStream(f);
            final ZipEntry entry = new ZipEntry(path);
            out.putNextEntry(entry);
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            IO.close(in);
        }
    }
}

package org.apache.openejb.arquillian.remote;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public static void unzip(File source, File targetDirectory) {
        OutputStream os = null;
        ZipInputStream is = null;

        try {
            is = new ZipInputStream(new FileInputStream(source));
            ZipEntry entry;

            while ((entry = is.getNextEntry()) != null) {
                String name = entry.getName();
                File file = new File(targetDirectory, name);

                if (name.endsWith("/")) {
                    file.mkdir();
                } else {
                    file.createNewFile();

                    int bytesRead;
                    byte data[] = new byte[8192];

                    os = new FileOutputStream(file);
                    while ((bytesRead = is.read(data)) != -1) {
                        os.write(data, 0, bytesRead);
                    }

                    is.closeEntry();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }

        }
    }
}

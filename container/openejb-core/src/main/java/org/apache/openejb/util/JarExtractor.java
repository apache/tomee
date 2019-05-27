/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.util;

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Zips;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class JarExtractor {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");

    /**
     * Extract the Jar file into an unpacked directory structure, and
     * return the absolute pathname to the extracted directory.
     *
     * @param file     Jar file to unpack
     * @param pathname Context path name for web application
     * @throws IllegalArgumentException if this is not a "jar:" URL
     * @throws IOException              if an input/output error was encountered
     *                                  during expansion
     */
    public static File extract(final File file, final String pathname) throws IOException {

        final Properties properties = SystemInstance.get().getProperties();
        final String key = "tomee.unpack.dir";

        File unpackDir = file.getParentFile();

        if (properties.containsKey(key)) {
            final FileUtils base = SystemInstance.get().getBase();
            unpackDir = base.getDirectory(properties.getProperty(key), true);
        }

        File docBase = new File(unpackDir, pathname);

        docBase = extract(file, docBase);
        return docBase;
    }

    /**
     * Extract the jar file into the specifiec destination directory.  If the destination directory
     * already exists, the jar will not be unpacked.
     *
     * @param file           jar file to unpack
     * @param destinationDir the directory in which the jar will be unpacked; must not exist
     * @throws IOException if an input/output error was encountered during expansion
     */
    public static File extract(final File file, File destinationDir) throws IOException {
        if (destinationDir.exists()) {

            if (destinationDir.lastModified() > file.lastModified()) {
                // Ear file is already installed
                // Unpacked dir is newer than archive
                return destinationDir.getAbsoluteFile();
            }

            if (!deleteDir(destinationDir)) {
                Files.deleteOnExit(destinationDir);
                final File pf = destinationDir.getParentFile();
                final String name = destinationDir.getName() + System.currentTimeMillis();
                destinationDir = new File(pf, name);
                destinationDir.deleteOnExit();
                Files.deleteOnExit(destinationDir);
            }
        }

        logger.info("Extracting jar: " + file.getAbsolutePath());

        try {
            Files.mkdirs(destinationDir);
        } catch (final Files.FileRuntimeException e) {
            throw new IOException("Failed to create: " + destinationDir);
        }

        try {
            Zips.unzip(file, destinationDir);
        } catch (final IOException e) {
            // If something went wrong, delete extracted dir to keep things clean
            Files.delete(destinationDir);
            throw e;
        }

        // Return the absolute path to our new document base directory
        logger.info("Extracted path: " + destinationDir.getAbsolutePath());
        return destinationDir.getAbsoluteFile();
    }

    /**
     * Copy the specified file or directory to the destination.
     *
     * @param src  File object representing the source
     * @param dest File object representing the destination
     */
    public static boolean copyRecursively(final File src, final File dest) {

        boolean result = true;

        String[] files;

        if (src.isDirectory()) {
            files = src.list();
            result = dest.mkdir();
        } else {
            files = new String[1];
            files[0] = "";
        }

        if (files == null) {
            files = new String[0];
        }
        for (int i = 0; i < files.length && result; i++) {
            final File fileSrc = new File(src, files[i]);
            final File fileDest = new File(dest, files[i]);

            if (fileSrc.isDirectory()) {

                result = copyRecursively(fileSrc, fileDest);

            } else {

                FileChannel ic = null;
                FileChannel oc = null;
                try {
                    ic = new FileInputStream(fileSrc).getChannel();
                    oc = new FileOutputStream(fileDest).getChannel();
                    ic.transferTo(0, ic.size(), oc);
                } catch (final IOException e) {
                    logger.error("Copy failed: src: " + fileSrc + ", dest: " + fileDest, e);
                    result = false;
                } finally {
                    IO.close(ic);
                    IO.close(oc);
                }
            }
        }
        return result;

    }

    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    public static boolean delete(final File dir) {
        return deleteDir(dir);
    }

    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    public static boolean deleteDir(final File dir) {
        if (dir == null) {
            return true;
        }

        if (dir.isDirectory()) {
            final File[] files = dir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    deleteDir(file);
                }
            }
        }

        return dir.delete();
    }

    /**
     * Extract the specified input stream into the specified directory, creating
     * a file named from the specified relative path.
     *
     * @param input   InputStream to be copied
     * @param docBase Document base directory into which we are extracting
     * @param name    Relative pathname of the file to be created
     * @return A handle to the extracted File
     * @throws IOException if an input/output error occurs
     */
    protected static File extract(final InputStream input, final File docBase, final String name)
        throws IOException {

        final File file = new File(docBase, name);
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            final byte[] buffer = new byte[2048];
            while (true) {
                final int n = input.read(buffer);
                if (n <= 0) {
                    break;
                }
                output.write(buffer, 0, n);
            }
        }
        // Ignore

        return file;
    }

}

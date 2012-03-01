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

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * @version $Rev$ $Date$
 */
public class JarExtractor {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");

    /**
     * Extract the Jar file into an unpacked directory structure, and
     * return the absolute pathname to the extracted directory.
     *
     * @param file Jar file to unpack
     * @param pathname Context path name for web application
     * @throws IllegalArgumentException if this is not a "jar:" URL
     * @throws java.io.IOException              if an input/output error was encountered
     *                                  during expansion
     */
    public static File extract(File file, String pathname) throws IOException {
        File docBase = new File(file.getParentFile(), pathname);
        extract(file, docBase);
        return docBase;
    }

    /**
     * Extract the jar file into the specifiec destination directory.  If the destination directory
     * already exists, the jar will not be unpacked.
     *
     * @param file jar file to unpack
     * @param destinationDir the directory in which the jar will be unpacked; must not exist
     * @throws java.io.IOException if an input/output error was encountered during expansion
     */
    public static void extract(File file, File destinationDir) throws IOException {
        if (destinationDir.exists()) {
            // Ear file is already installed
            return;
        }

        logger.info("Extracting jar: " + file.getAbsolutePath());

        Files.mkdirs(destinationDir);

        try {
            Zips.unzip(file, destinationDir);
        } catch (IOException e) {
            // If something went wrong, delete extracted dir to keep things
            // clean
            deleteDir(destinationDir);
            throw e;
        }

        // Return the absolute path to our new document base directory
        logger.info("Extracted path: " + destinationDir.getAbsolutePath());
    }


    /**
     * Copy the specified file or directory to the destination.
     *
     * @param src  File object representing the source
     * @param dest File object representing the destination
     */
    public static boolean copyRecursively(File src, File dest) {

        boolean result = true;

        String files[];

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
        for (int i = 0; (i < files.length) && result; i++) {
            final File fileSrc = new File(src, files[i]);
            final File fileDest = new File(dest, files[i]);

            if (fileSrc.isDirectory()) {

                result = copyRecursively(fileSrc, fileDest);

            } else {

                FileChannel ic = null;
                FileChannel oc = null;
                try {
                    ic = (new FileInputStream(fileSrc)).getChannel();
                    oc = (new FileOutputStream(fileDest)).getChannel();
                    ic.transferTo(0, ic.size(), oc);
                } catch (IOException e) {
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
    public static boolean delete(File dir) {
        if (dir == null) return true;

        if (dir.isDirectory()) {
            return deleteDir(dir);
        } else {
            return dir.delete();
        }
    }


    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    public static boolean deleteDir(File dir) {
        if (dir == null) return true;

        String fileNames[] = dir.list();
        if (fileNames == null) {
            fileNames = new String[0];
        }
        for (String fileName : fileNames) {
            File file = new File(dir, fileName);
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
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
     * @throws java.io.IOException if an input/output error occurs
     */
    protected static File extract(InputStream input, File docBase, String name)
            throws IOException {

        File file = new File(docBase, name);
        BufferedOutputStream output = null;
        try {
            output =
                    new BufferedOutputStream(new FileOutputStream(file));
            byte buffer[] = new byte[2048];
            while (true) {
                int n = input.read(buffer);
                if (n <= 0)
                    break;
                output.write(buffer, 0, n);
            }
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        return file;
    }


}

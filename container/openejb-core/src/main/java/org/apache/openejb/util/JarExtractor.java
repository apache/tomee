/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

        // Create the new document base directory
        destinationDir.mkdirs();

        // Extract the JAR into the new directory
        JarFile jarFile = null;
        InputStream input = null;
        try {
            jarFile = new JarFile(file);
            Enumeration jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                String name = jarEntry.getName();
                int last = name.lastIndexOf('/');
                if (last >= 0) {
                    File parent = new File(destinationDir,
                            name.substring(0, last));
                    parent.mkdirs();
                }
                if (name.endsWith("/")) {
                    continue;
                }
                input = jarFile.getInputStream(jarEntry);

                File extractedFile = extract(input, destinationDir, name);
                long lastModified = jarEntry.getTime();
                if ((lastModified != -1) && (lastModified != 0) && (extractedFile != null)) {
                    extractedFile.setLastModified(lastModified);
                }

                input.close();
                input = null;
            }
        } catch (IOException e) {
            // If something went wrong, delete extracted dir to keep things
            // clean
            deleteDir(destinationDir);
            throw e;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable t) {
                }
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable t) {
                }
            }
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
    public static boolean copy(File src, File dest) {

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
            File fileSrc = new File(src, files[i]);
            File fileDest = new File(dest, files[i]);
            if (fileSrc.isDirectory()) {
                result = copy(fileSrc, fileDest);
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
                    if (ic != null) {
                        try {
                            ic.close();
                        } catch (IOException e) {
                        }
                    }
                    if (oc != null) {
                        try {
                            oc.close();
                        } catch (IOException e) {
                        }
                    }
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

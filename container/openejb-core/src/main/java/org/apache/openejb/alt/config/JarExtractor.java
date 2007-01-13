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
package org.apache.openejb.alt.config;

import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.nio.channels.FileChannel;

/**
 * @version $Rev$ $Date$
 */
public class JarExtractor {

    /**
     * Extract the WAR file found at the specified URL into an unpacked
     * directory structure, and return the absolute pathname to the extracted
     * directory.
     *
     * @param jar      URL of the web application archive to be extracted
     *                 (must start with "jar:")
     * @param pathname Context path name for web application
     * @param file
     * @throws IllegalArgumentException if this is not a "jar:" URL
     * @throws java.io.IOException              if an input/output error was encountered
     *                                  during expansion
     */
    public static File extract(URL jar, String pathname, File file)
            throws IOException {

        // Make sure that there is no such directory already existing
        FileUtils base = SystemInstance.get().getBase();
        File appBase = base.getDirectory("apps", true);
        if (!appBase.exists() || !appBase.isDirectory()) {
            throw new IOException("" + appBase.getAbsolutePath());
        }

        File docBase = new File(appBase, pathname);
        if (docBase.exists()) {
            // Ear file is already installed
            return docBase;
        }

        DeploymentLoader.logger.info("Extracting jar: " + file.getAbsolutePath());

        // Create the new document base directory
        docBase.mkdir();

        // Extract the JAR into the new directory
        JarURLConnection jarURLConnection = (JarURLConnection) jar.openConnection();
        jarURLConnection.setUseCaches(false);
        JarFile jarFile = null;
        InputStream input = null;
        try {
            jarFile = jarURLConnection.getJarFile();
            Enumeration jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
                String name = jarEntry.getName();
                int last = name.lastIndexOf('/');
                if (last >= 0) {
                    File parent = new File(docBase,
                            name.substring(0, last));
                    parent.mkdirs();
                }
                if (name.endsWith("/")) {
                    continue;
                }
                input = jarFile.getInputStream(jarEntry);

                File extractedFile = extract(input, docBase, name);
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
            deleteDir(docBase);
            throw e;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable t) {
                    ;
                }
                input = null;
            }
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable t) {
                    ;
                }
                jarFile = null;
            }
        }

        // Return the absolute path to our new document base directory
        DeploymentLoader.logger.info("Extracted path: " + docBase.getAbsolutePath());

        return docBase;

    }


    /**
     * Copy the specified file or directory to the destination.
     *
     * @param src  File object representing the source
     * @param dest File object representing the destination
     */
    public static boolean copy(File src, File dest) {

        boolean result = true;

        String files[] = null;
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
                    DeploymentLoader.logger.error("Copy failed: src: " + fileSrc + ", dest: " + fileDest, e);
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

        String files[] = dir.list();
        if (files == null) {
            files = new String[0];
        }
        for (int i = 0; i < files.length; i++) {
            File file = new File(dir, files[i]);
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

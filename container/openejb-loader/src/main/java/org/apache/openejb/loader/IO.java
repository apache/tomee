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
package org.apache.openejb.loader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @version $Revision$ $Date$
 *          <p/>
 *          NOTE: CHECK ExecMojo before adding dependency or inner class to it please
 */
public class IO {
    private static final int MAX_TIMEOUT;

    static {
        int timeout = 5000;
        try {
            timeout = SystemInstance.isInitialized() ?
                    SystemInstance.get().getOptions().get("openejb.io.util.timeout", timeout) :
                    Integer.getInteger("openejb.io.util.timeout", timeout);
        } catch (final Throwable th) {
            // no-op: see ExecMojo
        }
        MAX_TIMEOUT = timeout;
    }

    /**
     * Method for reading files as String
     * 
     * @param uri URI
     * @return String
     * @throws IOException if an I/O error occurs
     */
    public static String readFileAsString(final URI uri) throws IOException {
        final StringBuilder builder = new StringBuilder("");
        for (final Proxy proxy : ProxySelector.getDefault().select(uri)) {
            final InputStream is;

            try {
                final URLConnection urlConnection = uri.toURL().openConnection(proxy);
                urlConnection.setConnectTimeout(MAX_TIMEOUT);
                is = urlConnection.getInputStream();
            } catch (final IOException e) {
                continue;
            }

            String line;
            try {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } finally {
                close(is);
            }
        }

        return builder.toString();
    }

    /**
     * Method for reading Properties
     * 
     * @param resource URL
     * @return Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties readProperties(final URL resource) throws IOException {
        return readProperties(resource, new Properties());
    }

    /**
     * Reading Properties 
     * 
     * @param resource URL
     * @param properties Properties
     * @return Properties
     * @throws IOException if and I/O error occurs
     */
    public static Properties readProperties(final URL resource, final Properties properties) throws IOException {
        return readProperties(read(resource), properties);
    }

    /**
     * Read properties of the specified pathname
     * 
     * @param resource File
     * @return Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties readProperties(final File resource) throws IOException {
        return readProperties(resource, new Properties());
    }

    /**
     * Read properties of the specified pathname
     * 
     * @param resource File
     * @param properties Properties
     * @return Properties
     * @throws IOException if an I/O error occurs
     */
    public static Properties readProperties(final File resource, final Properties properties) throws IOException {
        return readProperties(read(resource), properties);
    }

    /**
     * Reads and closes the input stream
     *
     * @param in         InputStream
     * @param properties Properties
     * @return Properties
     * @throws IOException
     */
    public static Properties readProperties(final InputStream in, final Properties properties) throws IOException {
        if (in == null) {
            throw new NullPointerException("InputStream is null");
        }
        if (properties == null) {
            throw new NullPointerException("Properties is null");
        }
        try {
            properties.load(in);
        } finally {
            close(in);
        }
        return properties;
    }

    /**
     * Method for reading the String of the specified pathname
     * 
     * @param url URL
     * @return String
     * @throws IOException if an I/O error occurs
     */
    public static String readString(final URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        } finally {
            close(in);
        }
    }

    /**
     * Method for reading the String of the specified pathname
     * 
     * @param file File
     * @return String
     * @throws IOException if an I/O error occurs
     */
    public static String readString(final File file) throws IOException {
        final FileReader in = new FileReader(file);
        try {
            final BufferedReader reader = new BufferedReader(in);
            return reader.readLine();
        } finally {
            close(in);
        }
    }

    /**
     * Method to read the entire File into a String
     * 
     * @param file File
     * @return String
     * @throws IOException if an I/O error occurs
     */
    public static String slurp(final File file) throws IOException {
        try (final InputStream is = read(file)) {
            return slurp(is);
        }
    }

    /**
     * Method to read the entire specified pathname into a String
     * 
     * @param url URL
     * @return String
     * @throws IOException if an I/O error occurs
     */
    public static String slurp(final URL url) throws IOException {
        return slurp(url.openStream());
    }

    /**
     * Method to read the entire InputStream into a String
     * 
     * @param in InputStream
     * @return String
     * @throws IOException if an I/O error occurs
     */
    public static String slurp(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return new String(out.toByteArray());
    }

    /**
     * Method to write the specified String to File
     * 
     * @param file File
     * @param string String
     * @throws IOException if an I/O error occurs
     */
    public static void writeString(final File file, final String string) throws IOException {
        final FileWriter out = new FileWriter(file);
        try {
            final BufferedWriter bufferedWriter = new BufferedWriter(out);
            try {
                bufferedWriter.write(string);
                bufferedWriter.newLine();
            } finally {
                close(bufferedWriter);
            }
        } finally {
            close(out);
        }
    }

    /**
     * Method to copy File object
     * 
     * @param from File
     * @param to File
     * @throws IOException if an I/O error occurs
     */
    public static void copy(final File from, final File to) throws IOException {
        if (!from.isDirectory()) {
            final FileOutputStream fos = new FileOutputStream(to);
            try {
                copy(from, fos);
            } finally {
                close(fos);
            }
        } else {
            copyDirectory(from, to);
        }
    }

    /**
     * Method to copy directory 
     * 
     * @param srcDir File
     * @param destDir File
     * @throws IOException if an I/O error occurs
     */
    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles = srcDir.listFiles();
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, exclusionList);
    }

    private static void doCopyDirectory(final File srcDir, final File destDir, final List<String> exclusionList) throws IOException {
        final File[] files = srcDir.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File file : files) {
            final File copiedFile = new File(destDir, file.getName());
            if (exclusionList == null || !exclusionList.contains(file.getCanonicalPath())) {
                if (file.isDirectory()) {
                    doCopyDirectory(file, copiedFile, exclusionList);
                } else {
                    copy(file, copiedFile);
                }
            }
        }
    }

    /**
     * Method to copy File to OutputStream 
     * 
     * @param from File
     * @param to OutputStream
     * @throws IOException if an I/O error occurs
     */
    public static void copy(final File from, final OutputStream to) throws IOException {
        final InputStream read = read(from);
        try {
            copy(read, to);
        } finally {
            close(read);
        }
    }

    /**
     * Method to copy from specified pathname to OutputStream
     * 
     * @param from URL
     * @param to OutputStream
     * @throws IOException if an I/O error occurs
     */
    public static void copy(final URL from, final OutputStream to) throws IOException {
        final InputStream read = read(from);
        try {
            copy(read, to);
        } finally {
            close(read);
        }
    }

    /**
     * Method to copy from InputStream to File 
     * 
     * @param from InputStream
     * @param to File
     * @throws IOException if an I/O error occurs
     */
    public static void copy(final InputStream from, final File to) throws IOException {
        final OutputStream write = write(to);
        try {
            copy(from, write);
        } finally {
            close(write);
        }
    }

    /**
     * Method to copy InputStream to File and append
     * 
     * @param from InputStream
     * @param to File
     * @param append boolean
     * @throws IOException if an I/O error occurs
     */
    public static void copy(final InputStream from, final File to, final boolean append) throws IOException {
        final OutputStream write = write(to, append);
        try {
            copy(from, write);
        } finally {
            close(write);
        }
    }

    public static void copy(final InputStream from, final OutputStream to) throws IOException {
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = from.read(buffer)) != -1) {
            to.write(buffer, 0, length);
        }
        to.flush();
    }

    public static void copy(final byte[] from, final File to) throws IOException {
        copy(new ByteArrayInputStream(from), to);
    }

    public static void copy(final byte[] from, final OutputStream to) throws IOException {
        copy(new ByteArrayInputStream(from), to);
    }

    /**
     * Method for writing specified File as ZIP file format
     * 
     * @param file File
     * @return ZipOutputStream ZipOutputStream
     * @throws IOException if an I/O error occurs
     */
    public static ZipOutputStream zip(final File file) throws IOException {
        final OutputStream write = write(file);
        return new ZipOutputStream(write);
    }

    /**
     * Method to unzip ZIP file 
     * 
     * @param file File
     * @return ZipInputStream
     * @throws IOException if an I/O error occurs
     */
    public static ZipInputStream unzip(final File file) throws IOException {
        final InputStream read = read(file);
        return new ZipInputStream(read);
    }

    public static void close(final Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            if (Flushable.class.isInstance(closeable)) {
                ((Flushable) closeable).flush();
            }
        } catch (final Throwable e) {
            //Ignore
        }
        try {
            closeable.close();
        } catch (final Throwable e) {
            //Ignore
        }
    }

    /**
     * Delete file
     * 
     * @param file File
     * @return boolean if file successfully deleted
     */
    public static boolean delete(final File file) {
        if (file == null) {
            return false;
        }
        if (!file.delete()) {
            Logger.getLogger(IO.class.getName()).log(Level.WARNING, "Delete failed on: {0}", file.getAbsolutePath());
            return false;
        }

        return true;
    }

    /**
     * Write data to the specified File destination
     * 
     * @param destination File
     * @return OutputStream
     * @throws FileNotFoundException if the file is not found
     */
    public static OutputStream write(final File destination) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(destination);
        return new BufferedOutputStream(out, 32768);
    }

    /**
     * Write data to the specified File destination and append 
     * 
     * @param destination File
     * @param append boolean
     * @return OutputStream
     * @throws FileNotFoundException if the file is not found
     */
    public static OutputStream write(final File destination, final boolean append) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(destination, append);
        return new BufferedOutputStream(out, 32768);
    }

    /**
     * Read data from the specified File source
     * 
     * @param source File
     * @return InputStream
     * @throws FileNotFoundException if the file is not found
     */
    public static InputStream read(final File source) throws FileNotFoundException {
        final InputStream in = new FileInputStream(source);
        return new BufferedInputStream(in, 32768);
    }

    /**
     * Read the next byte from the specified String to input stream
     * 
     * @param content String
     * @return InputStream
     */
    public static InputStream read(final String content) {
        return read(content.getBytes());
    }

    /**
     * Read the next byte from the specified String to input stream using encoding
     * 
     * @param content String
     * @param encoding String
     * @return InputStream
     * @throws UnsupportedEncodingException
     */
    public static InputStream read(final String content, final String encoding) throws UnsupportedEncodingException {
        return read(content.getBytes(encoding));
    }

    /**
     * Read the data from byte array 
     * 
     * @param content byte[]
     * @return InputStream
     */
    public static InputStream read(final byte[] content) {
        return new ByteArrayInputStream(content);
    }

    /**
     * Open connection to the specified URL and return InputStream for reading from the connection
     * @param url URL
     * @return InputStream
     * @throws IOException
     */
    public static InputStream read(final URL url) throws IOException {
        return url.openStream();
    }
}
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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
import java.util.Properties;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @version $Revision$ $Date$
 */
public class IO {
    private static final int MAX_TIMEOUT = SystemInstance.get().getOptions().get("openejb.io.util.timeout", 5000);

    public static String readFileAsString(final URI uri) throws IOException {
        final StringBuilder builder = new StringBuilder("");
        for (Proxy proxy : ProxySelector.getDefault().select(uri)) {
            InputStream is;

            try {
                URLConnection urlConnection = uri.toURL().openConnection(proxy);
                urlConnection.setConnectTimeout(MAX_TIMEOUT);
                is = urlConnection.getInputStream();
            } catch (IOException e) {
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

    public static Properties readProperties(URL resource) throws IOException {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = resource.openStream();
            in = new BufferedInputStream(in);
            properties.load(in);
        } finally{
            try {
                if (in != null) in.close();
            } catch (IOException e) {
            }
        }
        return properties;
    }

    public static Properties readProperties(final File resource) throws IOException {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(resource));
            properties.load(in);
        } finally{
            try {
                if (in != null) in.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
        return properties;
    }

    public static String readString(URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        } finally {
            close(in);
        }
    }

    public static String readString(File file) throws IOException {
        final FileReader in = new FileReader(file);
        try {
            BufferedReader reader = new BufferedReader(in);
            return reader.readLine();
        } finally {
            close(in);
        }
    }

    public static String slurp(String fileName) throws IOException {
        return slurp(new File(fileName));
    }

    public static String slurp(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(file, out);
        return new String(out.toByteArray());
    }

    public static String slurp(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return new String(out.toByteArray(), "UTF-8");
    }

    public static String slurp(URL url) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(read(url), out);
        return new String(out.toByteArray());
    }

    public static void writeString(File file, String string) throws IOException {
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

    public static void copy(final File from, final File to) throws IOException {
        final FileOutputStream fos = new FileOutputStream(to);
        try {
            copy(from, fos);
        } finally {
            close(fos);
        }
    }

    public static void copy(File from, OutputStream to) throws IOException {
        final InputStream read = read(from);
        try {
            copy(read, to);
        } finally {
            close(read);
        }
    }

    public static void copy(InputStream from, File to) throws IOException {
        final OutputStream write = write(to);
        try {
            copy(from, write);
        } finally {
            close(write);
        }
    }

    public static void copy(InputStream from, File to, boolean append) throws IOException {
        final OutputStream write = write(to, append);
        try {
            copy(from, write);
        } finally {
            close(write);
        }
    }

    public static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = from.read(buffer)) != -1) {
            to.write(buffer, 0, length);
        }
        to.flush();
    }

    public static ZipOutputStream zip(File file) throws IOException {
        final OutputStream write = write(file);
        return new ZipOutputStream(write);
    }

    public static ZipInputStream unzip(File file) throws IOException {
        final InputStream read = read(file);
        return new ZipInputStream(read);
    }

    public static void close(Closeable closeable) {
        if (closeable == null) return;
        try {
            if (closeable instanceof Flushable) {
                ((Flushable) closeable).flush();
            }
        } catch (IOException e) {
        }
        try {
            closeable.close();
        } catch (IOException e) {
        }
    }

    public static boolean delete(File file) {
        if (file == null) return false;
        if (!file.delete()) {
            System.err.println("Delete failed " + file.getAbsolutePath());
            return false;
        }

        return true;
    }

    public static OutputStream write(File destination) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(destination);
        return new BufferedOutputStream(out, 32768);
    }

    public static OutputStream write(File destination, boolean append) throws FileNotFoundException {
        final OutputStream out = new FileOutputStream(destination, append);
        return new BufferedOutputStream(out, 32768);
    }

    public static InputStream read(File source) throws FileNotFoundException {
        final InputStream in = new FileInputStream(source);
        return new BufferedInputStream(in, 32768);
    }

    public static InputStream read(String content) {
        return read(content.getBytes());
    }

    public static InputStream read(String content, String encoding) throws UnsupportedEncodingException {
        return read(content.getBytes(encoding));
    }

    public static InputStream read(byte[] content) {
        return new ByteArrayInputStream(content);
    }

    public static InputStream read(URL url) throws IOException {
        return url.openStream();
    }
}
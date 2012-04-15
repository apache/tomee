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
package org.apache.xbean.finder.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class IOUtil {
    public static String readString(URL url) throws IOException {
        InputStream in = url.openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.readLine();
        } finally {
            close(in);
        }
    }

    public static String readString(File file) throws IOException {
        FileReader in = new FileReader(file);
        try {
            BufferedReader reader = new BufferedReader(in);
            return reader.readLine();
        } finally {
            close(in);
        }
    }

    public static void writeString(File file, String string) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write(string);
        bufferedWriter.newLine();
        close(bufferedWriter);
    }

    public static void copy(File from, OutputStream to) throws IOException {
        copy(read(from), to);
    }

    public static void copy(InputStream from, OutputStream to) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = from.read(buffer)) != -1) {
                to.write(buffer, 0, length);
            }
        } finally {
            close(from);
            close(to);
        }

    }

    public static void close(Closeable closeable) throws IOException {
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

    public static OutputStream write(File destination) throws FileNotFoundException {
        OutputStream out = new FileOutputStream(destination);
        return new BufferedOutputStream(out, 32768);
    }

    public static InputStream read(File source) throws FileNotFoundException {
        InputStream in = new FileInputStream(source);
        return new BufferedInputStream(in, 32768);
    }
}

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
package org.apache.tomee.installer;

import org.apache.openejb.loader.IO;
import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;

import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;

/**
 * @version $Rev$ $Date$
*/
public class Installers {
    public static String readEntry(final JarFile jarFile, final String name, final Alerts alerts) {
        final ZipEntry entry = jarFile.getEntry(name);
        if (entry == null) {
            return null;
        }
        try {
            final String text = IO.slurp(jarFile.getInputStream(entry));
            return text;
        } catch (final Exception e) {
            alerts.addError("Unable to read " + name + " from " + jarFile.getName());
            return null;
        }
    }

    public static String replace(final String inputText, final String begin, final String newBegin, final String end, final String newEnd) throws IOException {
        final BeginEndTokenHandler tokenHandler = new BeginEndTokenHandler(newBegin, newEnd);

        final ByteArrayInputStream in = new ByteArrayInputStream(inputText.getBytes());

        final InputStream replacementStream = new DelimitedTokenReplacementInputStream(in, begin, end, tokenHandler, true);
        // SwizzleStream block read methods are broken so read byte at a time
        final StringBuilder sb = new StringBuilder();
        int i = replacementStream.read();
        while (i != -1) {
            sb.append((char) i);
            i = replacementStream.read();
        }
        final String newServerXml = sb.toString();
        IO.close(replacementStream);
        return newServerXml;
    }

    public static boolean backup(final File source, final Alerts alerts) {
        try {
            final File backupFile = new File(source.getParent(), source.getName() + ".original");
            if (!backupFile.exists()) {
                copyFile(source, backupFile);
            }
            return true;
        } catch (final IOException e) {
            alerts.addError("Unable to backup " + source.getAbsolutePath() + "; No changes will be made to this file");
            return false;
        }
    }

    public static void copyFile(final File source, final File destination) throws IOException {
        final File destinationDir = destination.getParentFile();
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            throw new IOException("Cannot create directory : " + destinationDir);
        }

        IO.copy(source, destination);
    }

    public static boolean writeAll(final File file, final String text, final Alerts alerts) {
        // compare text with existing file content - to stop the file being touched

        if (file.exists()) {
            try {
                final String oldText = IO.slurp(file);
                if (oldText.equals(text)) {
                    return true;
                }
            } catch (final Exception e) {
                // no-op
            }

            if (! file.delete()) {
                    alerts.addError("can't replace " + file.getName());
            }
        }

        try {
            IO.copy(IO.read(text), file);
            return true;
        } catch (final Exception e) {
            alerts.addError("Unable to write to " + file.getAbsolutePath(), e);
            return false;
        }
    }

    public static void writeAll(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }

    public static String readAll(final File file, final Alerts alerts) {
        try {
            final String text = IO.slurp(file);
            return text;
        } catch (final Exception e) {
            alerts.addError("Unable to read " + file.getAbsolutePath());
            return null;
        }
    }

     public static void copy(final File srcFile, final File destFile) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }
         IO.copy(srcFile, destFile);
    }

    public static class BeginEndTokenHandler extends StringTokenHandler {
        private final String begin;
        private final String end;

        public BeginEndTokenHandler(final String begin, final String end) {
            this.begin = begin;
            this.end = end;
        }
        
        @Override
        public String handleToken(final String token) throws IOException {
            final String result = begin + token + end;
            return result;
        }
    }
}

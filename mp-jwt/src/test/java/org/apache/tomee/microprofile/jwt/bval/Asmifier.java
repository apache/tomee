/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.openejb.loader.IO;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @version $Revision$ $Date$
 */
public class Asmifier {

    private static final AtomicReference<File> tmpDir = new AtomicReference<>(null);

    public static byte[] readClassFile(final ClassLoader classLoader, final Class clazz) throws IOException {
        final String internalName = clazz.getName().replace('.', '/') + ".class";
        final URL resource = classLoader.getResource(internalName);
        return readBytes(resource);
    }

    public static byte[] readBytes(final URL url) throws IOException {
        final InputStream in = IO.read(url);
        try {
            return readBytes(in);
        } finally {
            IO.close(in);
        }
    }

    public static byte[] readBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IO.copy(in, out);
        return out.toByteArray();
    }

    public static byte[] readClassFile(final ClassLoader classLoader, final String className) throws IOException {
        final String internalName = className.replace('.', '/') + ".class";
        final URL resource = classLoader.getResource(internalName);
        return readBytes(resource);
    }

    public static byte[] readClassFile(final Class<?> clazz) throws IOException {
        return readClassFile(clazz.getClassLoader(), clazz);
    }

    static {
        try {
            final File asmifier = File.createTempFile("Asmifier", null);
            tmpDir.set(asmifier.getParentFile());
            if (!asmifier.delete()) {
                asmifier.deleteOnExit();
            }
        } catch (final IOException e) {
            throw new RuntimeException("Asmifier failed to locate a temporary directory");
        }
    }

    public static void print(final ClassLoader classLoader, final String className) throws IOException {
        final String internalName = className.replace('.', '/') + ".class";
        final URL resource = classLoader.getResource(internalName);

        if (null == resource) {
            throw new IOException("Failed to find resource: " + internalName);
        }

        final ClassReader reader = new ClassReader(resource.openStream());


        final File file = new File("/tmp/" + className);

        write(reader, file);
    }

    private static void write(final ClassReader reader, final File file) throws IOException {
        final OutputStream write = IO.write(file);
        write(reader, write);
    }

    public static void write(final ClassReader reader, final OutputStream write) throws IOException {
        final TraceClassVisitor visitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(write));
        reader.accept(visitor, ClassReader.SKIP_DEBUG);
        write.close();
    }

    public static void asmify(final Class clazz, final String suffix) throws IOException {
        asmify(clazz.getName(), readClassFile(clazz.getClassLoader(), clazz), suffix);
    }

    public static void asmify(final ClassLoader loader, final String className, final String suffix) throws IOException {
        asmify(className, readClassFile(loader, className), suffix);
    }

    public static void asmify(final String className, final byte[] bytes, final String suffix) throws IOException {
        final org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(bytes);
        final File file = new File(tmpDir.get(), className + "." + suffix);

        write(reader, file);
    }

    public static String asmify(final byte[] actualBytes) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        write(new ClassReader(actualBytes), byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }
}

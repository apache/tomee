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
package org.apache.openejb.maven.plugin.customizer.monkey;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.plugin.customizer.monkey.classloader.ClassLoaderFactory;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MonkeyTest {
    private final ClassLoaderFactory loaderFactory = new ClassLoaderFactory();

    @Test
    public void run() throws Exception {
        final File tomee = prepareProject();
        try {
            invoke(tomee); // invalid, should fail
        } catch (final ClassFormatError cfe) {
            // ok, we created an invalid jar
        }

        new Monkey(tomee).run();
        final File[] libs = new File(tomee, "lib").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".jar");
            }
        });
        assertEquals(1, libs.length);
        final File jar = libs[0];
        assertTrue(jar.getName().contains("tomee-monkey-"));
        assertEquals(2, invoke(tomee));

        FileUtils.deleteDirectory(tomee.getParentFile().getParentFile());
    }

    private Object invoke(final File tomee) throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        final ClassLoader loader = loaderFactory.create(new File(tomee, "lib"));
        try {
            return loader.loadClass("test.patch.MyMain").getMethod("test").invoke(null);
        } finally {
            loaderFactory.release(loader);
        }
    }

    private File prepareProject() throws IOException {
        final File target = new File("target/MonkeyTest_run" + System.currentTimeMillis() + "/mvn/target");
        target.mkdirs();

        final File classes = new File(target, "classes");
        classes.mkdirs();

        writeBinary(classes, "target/test-classes/test/patch/MyMain.class", "test/patch/MyMain.class");
        writeBinary(classes, "target/test-classes/test/patch/foo/Another.class", "test/patch/foo/Another.class");

        final File tomee = new File(target, "tomee");
        final File lib = new File(tomee, "lib");
        lib.mkdirs();

        // create the jar to patch, it is invalid but when patched it should work
        JarArchiveOutputStream stream = null;
        try {
            stream = new JarArchiveOutputStream(new FileOutputStream(new File(lib, "t.jar")));
            stream.putArchiveEntry(new JarArchiveEntry("test/patch/MyMain.class"));
            stream.write("invalid".getBytes());
            stream.closeArchiveEntry();
            stream.putArchiveEntry(new JarArchiveEntry("test/patch/foo/Another.class"));
            stream.write("invalid-too".getBytes());
            stream.closeArchiveEntry();
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IO.close(stream);
        }

        return tomee;
    }

    private void writeBinary(final File root, final String location, final String jarPath) throws IOException {
        final FileInputStream in = new FileInputStream(location);
        final File outFile = new File(root, jarPath);
        outFile.getParentFile().mkdirs();
        final FileOutputStream out = new FileOutputStream(outFile);
        IO.copy(in, out);
        IO.close(in);
        IO.close(out);
    }
}

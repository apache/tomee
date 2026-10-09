/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnzipTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void rejectsEntryEscapingCatalinaBase() throws Exception {
        final File catalinaBase = folder.newFolder("base");
        final File zip = zip(folder.newFile("evil.zip"), "../evil.txt");

        try {
            unzip(mojo(catalinaBase, false), zip);
            fail("entry escaping catalina base must be rejected");
        } catch (final InvocationTargetException e) {
            assertTrue(String.valueOf(e.getCause()), e.getCause() instanceof TomEEException);
        }
        assertFalse(new File(folder.getRoot(), "evil.txt").exists());
    }

    @Test
    public void rejectsEntryEscapingCatalinaBaseAfterRootFolderSkip() throws Exception {
        final File catalinaBase = folder.newFolder("work", "base");
        final File zip = zip(folder.newFile("evil.zip"), "apache-tomee/../../evil.txt");

        try {
            unzip(mojo(catalinaBase, true), zip);
            fail("entry escaping catalina base must be rejected");
        } catch (final InvocationTargetException e) {
            assertTrue(String.valueOf(e.getCause()), e.getCause() instanceof TomEEException);
        }
        assertFalse(new File(folder.getRoot(), "evil.txt").exists());
    }

    @Test
    public void extractsRegularEntries() throws Exception {
        final File catalinaBase = folder.newFolder("base");
        final File zip = zip(folder.newFile("dist.zip"), "apache-tomee/conf/marker.txt");

        unzip(mojo(catalinaBase, true), zip);

        assertTrue(new File(catalinaBase, "conf/marker.txt").isFile());
    }

    private static AbstractTomEEMojo mojo(final File catalinaBase, final boolean skipRootFolder) {
        final AbstractTomEEMojo mojo = new AbstractTomEEMojo() {
            @Override
            public String getCmd() {
                return "test";
            }
        };
        mojo.catalinaBase = catalinaBase;
        mojo.skipRootFolderOnUnzip = skipRootFolder;
        mojo.overrideOnUnzip = true;
        return mojo;
    }

    private static void unzip(final AbstractTomEEMojo mojo, final File zip) throws Exception {
        final Method unzip = AbstractTomEEMojo.class.getDeclaredMethod("unzip", File.class);
        unzip.setAccessible(true);
        unzip.invoke(mojo, zip);
    }

    private static File zip(final File target, final String entryName) throws Exception {
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target))) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write("content".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        return target;
    }
}

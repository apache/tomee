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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZipsTest {

    @Test
    public void unzipExtractsRegularEntries() throws Exception {
        final File destination = Files.mkdirs(new File("target/test/zips/normal"));

        Zips.unzip(zip("dir/file.txt", "hello"), destination, false);

        assertTrue(new File(destination, "dir/file.txt").isFile());
    }

    @Test
    public void unzipRejectsEntryEscapingDestination() throws Exception {
        final File destination = Files.mkdirs(new File("target/test/zips/slip/dest"));
        final File victim = new File(destination.getParentFile(), "victim.txt");
        Files.delete(victim);

        try {
            Zips.unzip(zip("../victim.txt", "pwned"), destination, false);
            fail("expected IOException for a zip entry escaping the destination");
        } catch (final IOException expected) {
            // expected
        }
        assertFalse("no file may be written outside the destination", victim.exists());
    }

    @Test
    public void unzipNoparentStillRejectsEscapingEntry() throws Exception {
        final File destination = Files.mkdirs(new File("target/test/zips/noparent/dest"));
        final File victim = new File(destination.getParentFile(), "victim.txt");
        Files.delete(victim);

        try {
            // after the noparent strip "x/" is removed, leaving "../victim.txt"
            Zips.unzip(zip("x/../../victim.txt", "pwned"), destination, true);
            fail("expected IOException for a zip entry escaping the destination");
        } catch (final IOException expected) {
            // expected
        }
        assertFalse("no file may be written outside the destination", victim.exists());
    }

    private static InputStream zip(final String entryName, final String content) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (final ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(content.getBytes());
            zip.closeEntry();
        }
        return new ByteArrayInputStream(bytes.toByteArray());
    }
}

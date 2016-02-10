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
package org.apache.tomee.jul.handler.rotating;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ArchivingTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> formats() {
        return asList(new Object[] {"zip"}, new Object[] {"gzip"});
    }

    @Parameterized.Parameter(0)
    public String format;

    @Test
    public void logAndRotate() throws IOException, NoSuchMethodException {
        clean("target/ArchivingTest-" + format + "/logs");

        final AtomicReference<String> today = new AtomicReference<String>();
        final Map<String, String> config = new HashMap<String, String>();

        // initial config
        today.set("2015-09-01");
        config.put("filenamePattern", "target/ArchivingTest-" + format + "/logs/test.%s.%d.log");
        config.put("archiveDirectory", "target/ArchivingTest-" + format + "/logs/archives");
        config.put("archiveFormat", format);
        config.put("archiveOlderThan", "1 s");
        config.put("limit", "10 kilobytes");
        config.put("level", "INFO");
        config.put("dateCheckInterval", "1 second");

        final LocalFileHandler handler = new LocalFileHandler() {
            @Override
            protected String currentDate() {
                return today.get();
            }

            @Override
            protected String getProperty(final String name, final String defaultValue) {
                final String s = config.get(name.substring(name.lastIndexOf('.') + 1));
                return s != null ? s : defaultValue;
            }
        };

        final String string10chars = "abcdefghij";
        final int iterations = 950;
        for (int i = 0; i < iterations; i++) {
            handler.publish(new LogRecord(Level.INFO, string10chars));
        }

        today.set("2015-09-02");
        try { // ensure we test the date
            Thread.sleep(1500);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        handler.publish(new LogRecord(Level.INFO, string10chars)); // will trigger the archiving
        handler.close();

        final File logGzip = new File("target/ArchivingTest-" + format + "/logs/archives/test.2015-09-01.0.log." + format);
        assertTrue(logGzip.getAbsolutePath(), logGzip.isFile());

        if ("gzip".equals(format)) {
            final GZIPInputStream gis = new GZIPInputStream(new FileInputStream("target/ArchivingTest-gzip/logs/archives/test.2015-09-01.0.log.gzip"));
            final String content = IOUtils.toString(gis);
            assertTrue(content.contains("INFO: abcdefghij\n"));
            assertTrue(content.length() > 10000);
            gis.close();
        } else {
            final ZipInputStream zis = new ZipInputStream(new FileInputStream("target/ArchivingTest-zip/logs/archives/test.2015-09-01.0.log.zip"));
            assertEquals("test.2015-09-01.0.log", zis.getNextEntry().getName());
            final String content = IOUtils.toString(zis);
            assertTrue(content.contains("INFO: abcdefghij\n"));
            assertTrue(content.length() > 10000);
            assertNull(zis.getNextEntry());
            zis.close();
        }
    }

    @Test
    public void logAndRotateAndPurge() throws IOException, NoSuchMethodException {
        clean("target/ArchivingTestPurge-" + format + "/logs");

        final AtomicReference<String> today = new AtomicReference<String>();
        final Map<String, String> config = new HashMap<String, String>();

        // initial config
        today.set("2015-09-01");
        config.put("filenamePattern", "target/ArchivingTestPurge-" + format + "/logs/test.%s.%d.log");
        config.put("archiveDirectory", "target/ArchivingTestPurge-" + format + "/logs/archives");
        config.put("archiveFormat", format);
        config.put("archiveOlderThan", "1 s");
        config.put("purgeOlderThan", "2 s");
        config.put("limit", "10 kilobytes");
        config.put("level", "INFO");
        config.put("dateCheckInterval", "1 second");

        final LocalFileHandler handler = new LocalFileHandler() {
            @Override
            protected String currentDate() {
                return today.get();
            }

            @Override
            protected String getProperty(final String name, final String defaultValue) {
                final String s = config.get(name.substring(name.lastIndexOf('.') + 1));
                return s != null ? s : defaultValue;
            }
        };

        final String string10chars = "abcdefghij";
        final int iterations = 950;
        for (int i = 0; i < iterations; i++) {
            handler.publish(new LogRecord(Level.INFO, string10chars));
        }

        final File logArchive = new File("target/ArchivingTestPurge-" + format + "/logs/archives/test.2015-09-01.0.log." + format);

        today.set("2015-09-02");
        try {
            Thread.sleep(1500);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        handler.publish(new LogRecord(Level.INFO, string10chars)); // will trigger the archiving
        for (int i = 0; i < 120; i++) { // async so retry
            if (logArchive.exists()) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        }
        assertTrue(logArchive.getAbsolutePath() + " was archived", logArchive.exists());

        today.set("2015-09-03");
        try {
            Thread.sleep(2500);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        handler.publish(new LogRecord(Level.INFO, string10chars)); // will trigger the purging
        handler.close();

        assertFalse(logArchive.getAbsolutePath() + " was purged", logArchive.exists());
    }

    private static void clean(final String base) {
        {
            final File out = new File(base);
            if (out.exists()) {
                for (final File file : asList(out.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File pathname) {
                        return pathname.getName().startsWith("test");
                    }
                }))) {
                    file.delete();
                }
            }
        }
        {
            final File out = new File(base + "/archives");
            if (out.exists()) {
                for (final File file : asList(out.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File pathname) {
                        return pathname.getName().startsWith("test");
                    }
                }))) {
                    file.delete();
                }
            }
        }
    }
}

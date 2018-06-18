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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ArchivingTest {

    private static final AtomicReference<WatchEvent<?>> lastEvent = new AtomicReference<>();
    private static final AtomicReference<CountDownLatch> latch = new AtomicReference<>(null);

    private static Thread watcherThread;

    @Parameterized.Parameters(name = "{0}")
    public static String[][] formats() {
        return new String[][]{{"zip"}, {"gzip"}};
    }

    @Parameterized.Parameter(0)
    public String format;

    @Test
    public void logAndRotate() throws IOException, NoSuchMethodException {
        clean("target/ArchivingTest-" + format + "/logs");

        final AtomicReference<String> today = new AtomicReference<>();
        final Map<String, String> config = new HashMap<>();

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
            Thread.sleep(2000);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        handler.publish(new LogRecord(Level.INFO, string10chars)); // will trigger the archiving
        handler.close();

        withRetry(10, 3, new Runnable() {
            @Override
            public void run() {
                final File logGzip = new File("target/ArchivingTest-" + format + "/logs/archives/test.2015-09-01.0.log." + format);
                assertTrue(logGzip.getAbsolutePath(), logGzip.isFile());
            }
        });
        // note: size depends on the date so just use a > min
        if ("gzip".equals(format)) {
            try (final GZIPInputStream gis = new GZIPInputStream(new FileInputStream("target/ArchivingTest-gzip/logs/archives/test.2015-09-01.0.log.gzip"))) {
                final String content = IOUtils.toString(gis);
                assertTrue(content.contains(Level.INFO.getLocalizedName() + ": abcdefghij" + System.lineSeparator()));
                assertTrue(content.length() > 10000);
            }
        } else {
            try (final ZipInputStream zis = new ZipInputStream(new FileInputStream("target/ArchivingTest-zip/logs/archives/test.2015-09-01.0.log.zip"))) {
                assertEquals("test.2015-09-01.0.log", zis.getNextEntry().getName());
                final String content = IOUtils.toString(zis);
                assertTrue(content, content.contains(Level.INFO.getLocalizedName() + ": abcdefghij" + System.lineSeparator())); // INFO or INFOS
                assertTrue(content, content.length() > 10000);
                assertNull(zis.getNextEntry());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void logAndRotateAndPurge() throws Exception {
        clean("target/ArchivingTestPurge-" + format + "/logs");

        final AtomicReference<String> today = new AtomicReference<>();
        final Map<String, String> config = new HashMap<>();

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

        today.set("2015-09-02");
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        final File logArchive = new File("target/ArchivingTestPurge-" + format + "/logs/archives/test.2015-09-01.0.log." + format);
        if (logArchive.delete()) {
            System.out.println("Deleted existing test file: " + logArchive);
        }

        final File parentFile = logArchive.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            Assert.fail("Unable to create: " + parentFile);
        }
        final Path dir = parentFile.toPath();
        final WatchService watcher = FileSystems.getDefault().newWatchService();
        final WatchKey key = dir.register(watcher,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY);

        latch.set(new CountDownLatch(1));
        watch(key);

        handler.publish(new LogRecord(Level.INFO, string10chars)); // will trigger the archiving

        assertTrue("Failed to get archived log", latch.get().await(20, TimeUnit.SECONDS));
        final WatchEvent<?> watchEvent = lastEvent.get();

        assertTrue(StandardWatchEventKinds.ENTRY_CREATE.equals(watchEvent.kind()) || StandardWatchEventKinds.ENTRY_MODIFY.equals(watchEvent.kind()));

        final WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;

        final String io = ev.context().toString();
        assertTrue(io.startsWith("test.2015-09-01.") && io.endsWith(format));

        today.set("2015-09-03");
        try {
            Thread.sleep(2500);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        handler.publish(new LogRecord(Level.INFO, string10chars)); // will trigger the purging
        handler.close();
        withRetry(10, 2, new Runnable() {
            @Override
            public void run() {
                assertFalse(logArchive.getAbsolutePath() + " was purged", logArchive.exists());
            }
        });
    }

    private void withRetry(final int countDown, final long timeout, final Runnable assertCallback) {
        try {
            assertCallback.run();
        } catch (final AssertionError e) {
            if (countDown < 1) {
                throw e;
            }
            try {
                TimeUnit.SECONDS.sleep(timeout);
            } catch (final InterruptedException e1) {
                Thread.interrupted();
            }
            withRetry(countDown - 1, timeout, assertCallback);
        }
    }

    private static void clean(final String base) {
        cleanUp(new File(base));
        cleanUp(new File(base + "/archives"));
    }

    private static void cleanUp(final File out) {
        if (out.exists()) {
            final File[] files = out.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File pathname) {
                    return pathname.getName().startsWith("test");
                }
            });
            if (null != files) {
                for (final File file : asList(files)) {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
        }
    }

    private static void watch(final WatchKey key) {

        if (watcherThread != null) {
            // tell the old watchter thread to shutdown
            watcherThread.interrupt();
        }

         watcherThread = new Thread("ArchivingTest.watch") {
            @Override
            public void run() {


                for (; ; ) {
                    for (final WatchEvent<?> event : key.pollEvents()) {
                        final WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        if (watcherThread != this || isInterrupted()) {
                            return;
                        }

                        lastEvent.set(event);

                        latch.get().countDown();
                    }

                    final boolean valid = key.reset();
                    if (!valid) {
                        System.out.println("ArchivingTest.watch terminated");
                        break;
                    }
                }
            }
        };

        watcherThread.start();
    }
}

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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalFileHandlerTest {
    @Test
    public void logAndRotate() throws IOException {
        final File out = new File("target/LocalFileHandlerTest/logs/");
        if (out.exists()) {
            for (final File file : asList(out.listFiles(new FileFilter() {
                @Override
                public boolean accept(final File pathname) {
                    return pathname.getName().startsWith("test");
                }
            }))) {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }

        final AtomicReference<String> today = new AtomicReference<>();
        final Map<String, String> config = new HashMap<>();

        // initial config
        today.set("day1");
        config.put("filenamePattern", "target/LocalFileHandlerTest/logs/test.%s.%d.log");
        config.put("limit", "10 kilobytes");
        config.put("level", "INFO");
        config.put("dateCheckInterval", "1 second");
        config.put("formatter", MessageOnlyFormatter.class.getName());

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

        final File[] logFiles = out.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.getName().startsWith("test");
            }
        });
        final Set<String> logFilesNames = new HashSet<>();
        for (final File f : logFiles) {
            logFilesNames.add(f.getName());
        }
        assertEquals(2, logFiles.length);
        assertEquals(new HashSet<>(asList("test.day1.0.log", "test.day1.1.log")), logFilesNames);

        try (final InputStream is = new FileInputStream(new File(out, "test.day1.1.log"))) {
            final List<String> lines = IOUtils.readLines(is);
            assertEquals(19, lines.size());
            assertEquals(string10chars, lines.iterator().next());
        }

        final long firstFileLen = new File(out, "test.day1.0.log").length();
        assertTrue(firstFileLen >= 1024 * 10 && firstFileLen < 1024 * 10 + (1 + string10chars.getBytes().length));

        // now change of day
        today.set("day2");
        try { // ensure we tets the date
            Thread.sleep(1500);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        handler.publish(new LogRecord(Level.INFO, string10chars));
        assertTrue(new File(out, "test.day2.0.log").exists());

        handler.close();
    }

    public static class MessageOnlyFormatter extends Formatter {
        @Override
        public String format(final LogRecord record) {
            return record.getMessage() + "\r";
        }
    }
}

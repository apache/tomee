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
package org.apache.openejb.util;

import junit.framework.TestCase;

import java.util.Properties;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.apache.openejb.loader.Options;

/**
 * @version $Rev$ $Date$
 */
public class OptionsTest extends TestCase {
    private Properties properties;
    private Options options;
    private TestLog log;

    public void testEnumCase() throws Exception {
        properties.setProperty("caseSensitive", Colors.RED.toString());
        properties.setProperty("caseInsensitive", "blue");

        assertSame(Colors.RED, options.get("caseSensitive", Colors.GREEN));
        assertSame(Colors.BLUE, options.get("caseInsensitive", Colors.GREEN));
        assertSame(Colors.GREEN, options.get("default", Colors.GREEN));
    }

    public void testNoneNone() throws Exception {
        // User specified NONE
        String userValue = "NONE";

        properties.setProperty("colors", userValue);
        
        // Default is NONE
        Set<Colors> colors = options.getAll("colors", Colors.class);

        assertNotNull(colors);
        assertEquals("size", 0, colors.size());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Info.class, log.messages.get(0).getClass());
        assertContains(log.messages.get(0).message, "colors="+userValue);
    }

    public void testNoneAll() throws Exception {
        // User specified ALL
        String userValue = "ALL";
        properties.setProperty("colors", userValue);

        // Default is NONE
        Set<Colors> colors = options.getAll("colors", Colors.class);

        assertNotNull(colors);
        assertEquals("size", Colors.values().length, colors.size());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Info.class, log.messages.get(0).getClass());
        assertContains(log.messages.get(0).message, "colors="+userValue);
    }

    public void testNoneSome() throws Exception {
        // User specified RED
        String userValue = "red";
        properties.setProperty("colors", userValue);

        // Default is NONE
        Set<Colors> colors = options.getAll("colors", Colors.class);

        assertNotNull(colors);
        assertEquals("size", 1, colors.size());
        assertEquals("size", Colors.RED, colors.iterator().next());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Info.class, log.messages.get(0).getClass());
        assertContains(log.messages.get(0).message, "colors=red");
    }

    public void testNoneDefault() throws Exception {
        // User specified nothing

        // Default is NONE
        Set<Colors> colors = options.getAll("colors", Colors.class);

        assertNotNull(colors);
        assertEquals("size", 0, colors.size());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Debug.class, log.messages.get(0).getClass());

        String message = log.messages.get(0).message;


        assertContains(message, "colors=NONE");
        assertContains(message, "Possible values");

        message = message.substring(message.indexOf("Possible values"));

        for (Colors color : colors) {
            assertContains(message, color.name().toLowerCase());
        }

        assertContains(message, "NONE");
        assertContains(message, "ALL");
    }

    public void testAllAll() throws Exception {
        // User specified ALL
        String userValue = "ALL";

        properties.setProperty("colors", userValue);

        // Default is ALL
        Set<Colors> colors = options.getAll("colors", Colors.values());

        assertNotNull(colors);
        assertEquals("size", Colors.values().length, colors.size());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Info.class, log.messages.get(0).getClass());
        assertContains(log.messages.get(0).message, "colors="+userValue);
    }

    public void testAllNone() throws Exception {
        // User specified NONE
        String userValue = "NONE";
        properties.setProperty("colors", userValue);

        // Default is ALL
        Set<Colors> colors = options.getAll("colors", Colors.values());

        assertNotNull(colors);
        assertEquals("size", 0, colors.size());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Info.class, log.messages.get(0).getClass());
        assertContains(log.messages.get(0).message, "colors="+userValue);
    }

    public void testAllSome() throws Exception {
        // User specified NONE
        String userValue = "red";
        properties.setProperty("colors", userValue);

        // Default is ALL
        Set<Colors> colors = options.getAll("colors", Colors.values());

        assertNotNull(colors);
        assertEquals("size", 1, colors.size());
        assertEquals("size", Colors.RED, colors.iterator().next());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Info.class, log.messages.get(0).getClass());
        assertContains(log.messages.get(0).message, "colors=red");
    }

    public void testAllDefault() throws Exception {
        // User specified nothing

        // Default is ALL
        Set<Colors> colors = options.getAll("colors", Colors.values());

        assertNotNull(colors);
        assertEquals("size", Colors.values().length, colors.size());

        assertEquals("messages.size", 1, log.messages.size());
        assertEquals("messages level", Debug.class, log.messages.get(0).getClass());

        String message = log.messages.get(0).message;


        assertContains(message, "colors=ALL");
        assertContains(message, "Possible values");

        message = message.substring(message.indexOf("Possible values"));

        for (Colors color : colors) {
            assertContains(message, color.name().toLowerCase());
        }

        assertContains(message, "ALL");
        assertContains(message, "NONE");
    }

    private void assertContains(String message, String expected) {
        assertTrue("Expected ["+expected+"], actual ["+message+"]", message.contains(expected));
    }

    public void setUp() {
        properties = new Properties();
        options = new Options(properties);
        log = new TestLog();
        options.setLogger(log);
    }

    public static enum Colors {
        RED, GREEN, BLUE;
    }

    // Just to test if the logging works as expected
    private static class TestLog implements Options.Log {
        private final List<Message> messages = new ArrayList<Message>();

        public boolean isDebugEnabled() {
            return isInfoEnabled();
        }

        public boolean isInfoEnabled() {
            return isWarningEnabled();
        }

        public boolean isWarningEnabled() {
            return true;
        }

        public void warning(String message, Throwable t) {
            messages.add(new Warning(message));
        }

        public void warning(String message) {
            messages.add(new Warning(message));
        }

        public void info(String message, Throwable t) {
            messages.add(new Info(message));
        }

        public void info(String message) {
            messages.add(new Info(message));
        }

        public void debug(String message, Throwable t) {
            messages.add(new Debug(message));
        }

        public void debug(String message) {
            messages.add(new Debug(message));
        }

    }

    private abstract static class Message {
        private final String message;

        private Message(String message) {
            this.message = message;
        }
    }

    private static class Debug extends Message {
        private Debug(String message) {
            super(message);
        }
    }

    private static class Info extends Message {
        private Info(String message) {
            super(message);
        }
    }

    private static class Warning extends Message {
        private Warning(String message) {
            super(message);
        }
    }
}

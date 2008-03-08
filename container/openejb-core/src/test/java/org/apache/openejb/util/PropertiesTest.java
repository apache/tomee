/**
 *
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

public class PropertiesTest extends TestCase {

    public void testProperties() throws Exception {
        Properties properties = createProperties();

        // empty
        assertTrue(properties.isEmpty());
        assertEquals(0, properties.size());
        assertTrue(properties.keySet().isEmpty());
        assertEquals(0, properties.keySet().size());
        assertTrue(properties.values().isEmpty());
        assertEquals(0, properties.values().size());
        assertTrue(properties.entrySet().isEmpty());
        assertEquals(0, properties.entrySet().size());
        assertFalse(properties.keys().hasMoreElements());
        assertFalse(properties.propertyNames().hasMoreElements());
        assertFalse(properties.elements().hasMoreElements());
        assertNull(properties.get("foo"));
        assertNull(properties.getProperty("foo"));
        assertEquals("default", properties.getProperty("foo", "default"));

        // one entry
        assertNull(properties.put("foo", "bar"));
        assertFalse(properties.isEmpty());
        assertEquals(1, properties.size());
        assertFalse(properties.keySet().isEmpty());
        assertEquals(1, properties.keySet().size());
        assertEquals(Collections.singleton("foo"), properties.keySet());
        assertFalse(properties.values().isEmpty());
        assertEquals(1, properties.values().size());
        assertFalse(properties.entrySet().isEmpty());
        assertEquals(1, properties.entrySet().size());
        assertTrue(properties.keys().hasMoreElements());
        assertEquals("foo", properties.keys().nextElement());
        assertTrue(properties.propertyNames().hasMoreElements());
        assertEquals("foo", properties.keys().nextElement());
        assertTrue(properties.elements().hasMoreElements());
        assertEquals("bar", properties.get("foo"));
        assertEquals("bar", properties.getProperty("foo"));
        assertEquals("bar", properties.getProperty("foo", "default"));

    }

    public void testSimpleLoad() throws Exception {
        Properties properties;

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo:bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo = bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo : bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo\t \t=\t \tbar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo\t \t:\t \tbar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo:".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo =".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo :".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo = ".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo : ".getBytes()));
        assertEquals(singletonProperty("foo", ""), properties);

        //
        // Invalid key valid separator (results in separator in value)
        //

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo := bar".getBytes()));
        assertEquals(singletonProperty("foo", "= bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo == bar".getBytes()));
        assertEquals(singletonProperty("foo", "= bar"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo :: bar".getBytes()));
        assertEquals(singletonProperty("foo", ": bar"), properties);

    }

    public void testUnicode() throws Exception {
        Properties properties = createProperties();
        properties.load(new ByteArrayInputStream("a=\\u1234z".getBytes()));

        try {
            properties = createProperties();
            properties.load(new ByteArrayInputStream("a=\\u123".getBytes()));
            fail("Expected IllegalArgumentException due to invalid unicode sequence");
        } catch (IllegalArgumentException expected) {
        }

        try {
            properties = createProperties();
            properties.load(new ByteArrayInputStream("a=\\u123z".getBytes()));
            fail("Expected IllegalArgumentException due to invalid unicode sequence");
        } catch (IllegalArgumentException expected) {
        }

        properties = new SuperProperties();
        properties.load(new ByteArrayInputStream("a=\\".getBytes()));
        assertEquals(singletonProperty("a", "\u0000"), properties);

        properties = createProperties();
        properties.load(new ByteArrayInputStream("a=\\q".getBytes()));
        assertEquals(singletonProperty("a", "q"), properties);
    }

    public void testKeyLineContinuation() throws Exception {
        Properties properties;

        // line continuation (\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\noo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation with white space (\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\n  oo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\n\t\too=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\n \t oo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation (\r)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\roo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation (\r) with white space
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r  oo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r\t\too=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r \t oo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation (\r\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r\noo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        //
        // Invalid contunuations (results in a break)
        //

        // line continuation (\n\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\n\noo=bar".getBytes()));
        assertEquals(properties("f", "", "oo", "bar"), properties);

        // line continuation (\r\r)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r\roo=bar".getBytes()));
        assertEquals(properties("f", "", "oo", "bar"), properties);

        // line continuation (\r\n\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r\n\noo=bar".getBytes()));
        assertEquals(properties("f", "", "oo", "bar"), properties);

        // line continuation (\r\n\r)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("f\\\r\n\roo=bar".getBytes()));
        assertEquals(properties("f", "", "oo", "bar"), properties);
    }

    public void testValueLineContinuation() throws Exception {
        Properties properties;

        // line continuation (\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\nar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation with white space (\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\n  ar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\n\t\tar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\n \t ar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation (\r)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\rar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation (\r) with white space
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r  ar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r\t\tar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r \t ar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        // line continuation (\r\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r\nar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        //
        // Invalid contunuations (results in a break)
        //

        // line continuation (\n\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\n\nar".getBytes()));
        assertEquals(properties("foo", "b", "ar", ""), properties);

        // line continuation (\r\r)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r\rar".getBytes()));
        assertEquals(properties("foo", "b", "ar", ""), properties);

        // line continuation (\r\n\n)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r\n\nar".getBytes()));
        assertEquals(properties("foo", "b", "ar", ""), properties);

        // line continuation (\r\n\r)
        properties = createProperties();
        properties.load(new ByteArrayInputStream("foo=b\\\r\n\rar".getBytes()));
        assertEquals(properties("foo", "b", "ar", ""), properties);
    }

    public void testEscapedKeyValueSeparators() throws Exception {
        Properties properties = createProperties();

        // test put and get
        properties.put("\t\r\n my: \t\n\rkey=", "foo");
        assertEquals("foo", properties.get("\t\r\n my: \t\n\rkey="));

        // test store
        String text = store(properties);
        if (text.startsWith("#")) text = text.split("\\n", 2)[1];
        text = text.trim();
        assertEquals("\\t\\r\\n\\ my\\:\\ \\t\\n\\rkey\\==foo", text);

        // test load
        properties = createProperties();
        properties.load(new ByteArrayInputStream("\\t\\r\\n\\ my\\:\\ \\t\\n\\rkey\\==foo".getBytes()));
        assertEquals("foo", properties.get("\t\r\n my: \t\n\rkey="));
    }

    protected Properties createProperties() {
        return new Properties();
    }

    protected static Properties singletonProperty(String key, String value) {
        return properties(key, value);
    }

    protected static Properties properties(String... keysAndValues) {
        Properties properties = new Properties();
        for (int i = 0; i+1 < keysAndValues.length; i += 2) {
            String key = keysAndValues[i];
            String value = keysAndValues[i + 1];
            properties.put(key, value);
        }
        return properties;
    }

    @SuppressWarnings({"unchecked"})
    protected static void assertProperties(Properties expected, Properties actual) {
        if (expected.equals(actual)) return;

        StringBuilder message = new StringBuilder().append("\n");

        Set<String> keys = new TreeSet<String>();
        keys.addAll(new HashSet(expected.keySet()));
        keys.addAll(new HashSet(actual.keySet()));
        for (String key : keys) {
            if (!expected.containsKey(key)) {
                message.append("A ").append(key).append("=").append(actual.get(key)).append("\n");
            } else if (!actual.containsKey(key)) {
                message.append("R ").append(key).append("=").append(expected.get(key)).append("\n");
            } else {
                Object expectedValue = expected.get(key);
                Object actualValue = actual.get(key);
                if (expectedValue != expectedValue && (expectedValue == null || !expectedValue.equals(actual))) {
                    message.append("C ").append(key).append("=").append(expectedValue).append("\n");
                    message.append("  ").append(key).append("=").append(actualValue).append("\n");
                }

            }
        }

        fail(message.toString());
    }

    protected String store(Properties properties) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);
        return new String(out.toByteArray());
    }
}

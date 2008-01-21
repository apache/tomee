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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class SuperPropertiesTest extends PropertiesTest {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCaseSensitivity() throws Exception {
        SuperProperties properties = createProperties();
        properties.setProperty("foo", "bar");
        properties.setComment("foo", "comment");
        properties.getAttributes("foo").put("name", "value");

        // case sensitive
        assertTrue(properties.containsKey("foo"));
        assertEquals("bar", properties.get("foo"));
        assertEquals("bar", properties.getProperty("foo"));
        assertEquals("comment", properties.getComment("foo"));
        assertNotNull(properties.getAttributes("foo"));
        assertEquals("value", properties.getAttributes("foo").get("name"));
        assertFalse(properties.containsKey("FOO"));
        assertNull(properties.get("FOO"));
        assertNull(properties.getProperty("FOO"));
        assertNull(properties.getComment("FOO"));
        assertNull(properties.getAttributes("FOO"));

        // property differing only incase
        properties.setProperty("FOO", "BAR");
        properties.setComment("FOO", "COMMENT");
        properties.getAttributes("FOO").put("NAME", "VALUE");
        assertTrue(properties.containsKey("foo"));
        assertEquals("bar", properties.get("foo"));
        assertEquals("bar", properties.getProperty("foo"));
        assertEquals("comment", properties.getComment("foo"));
        assertNotNull(properties.getAttributes("foo"));
        assertEquals("value", properties.getAttributes("foo").get("name"));
        assertTrue(properties.containsKey("FOO"));
        assertEquals("BAR", properties.get("FOO"));
        assertEquals("BAR", properties.getProperty("FOO"));
        assertEquals("COMMENT", properties.getComment("FOO"));
        assertNotNull(properties.getAttributes("FOO"));
        assertEquals("VALUE", properties.getAttributes("FOO").get("NAME"));

        // case insensitive
        properties = createProperties();
        properties.setCaseInsensitive(true);
        properties.setProperty("foo", "bar");
        properties.setComment("foo", "comment");
        properties.getAttributes("foo").put("name", "value");
        assertTrue(properties.containsKey("foo"));
        assertEquals("bar", properties.get("foo"));
        assertEquals("bar", properties.getProperty("foo"));
        assertEquals("comment", properties.getComment("foo"));
        assertNotNull(properties.getAttributes("foo"));
        assertEquals("value", properties.getAttributes("foo").get("name"));
        assertTrue(properties.containsKey("FOO"));
        assertEquals("bar", properties.get("FOO"));
        assertEquals("bar", properties.getProperty("FOO"));
        assertEquals("comment", properties.getComment("FOO"));
        assertNotNull(properties.getAttributes("FOO"));
        assertEquals("value", properties.getAttributes("FOO").get("name"));

        // property differing only incase
        properties.setProperty("FOO", "BAR");
        properties.setComment("FOO", "COMMENT");
        properties.getAttributes("FOO").put("name", "VALUE");
        assertTrue(properties.containsKey("foo"));
        assertEquals("BAR", properties.get("foo"));
        assertEquals("BAR", properties.getProperty("foo"));
        assertEquals("COMMENT", properties.getComment("foo"));
        assertNotNull(properties.getAttributes("foo"));
        assertEquals("VALUE", properties.getAttributes("foo").get("name"));
        assertTrue(properties.containsKey("FOO"));
        assertEquals("BAR", properties.get("FOO"));
        assertEquals("BAR", properties.getProperty("FOO"));
        assertEquals("COMMENT", properties.getComment("FOO"));
        assertNotNull(properties.getAttributes("FOO"));
        assertEquals("VALUE", properties.getAttributes("FOO").get("name"));

    }

    public void testSynchronization() throws Exception {
        SuperProperties properties = createProperties();
        properties.setProperty("foo", "bar");
        properties.setComment("foo", "comment");
        assertNotNull(properties.getAttributes("foo"));
        properties.getAttributes("foo").put("name", "value");

        // changing a property value should not effect comments or attributes
        properties.put("foo", "bar2");
        assertEquals("comment", properties.getComment("foo"));
        assertNotNull(properties.getAttributes("foo"));
        assertEquals("value", properties.getAttributes("foo").get("name"));

        // removing a property should remove comments and attributes
        properties.remove("foo");
        assertNull(properties.getComment("foo"));
        assertNull(properties.getAttributes("foo"));
    }

    public void testLoadStoreLoad() throws Exception {
        SuperProperties expected = new SuperProperties();
        expected.load(getClass().getResourceAsStream("test.properties"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        expected.store(out, null);

        SuperProperties actual = createProperties();
        actual.load(new ByteArrayInputStream(out.toByteArray()));

        assertProperties(expected, actual);
    }

    public void testLoadStoreLoadXml() throws Exception {
        SuperProperties expected = new SuperProperties();
        expected.load(getClass().getResourceAsStream("test.properties"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        expected.storeToXML(out, null);

        SuperProperties actual = createProperties();
        actual.loadFromXML(new ByteArrayInputStream(out.toByteArray()));

        assertProperties(expected, actual);
    }

    public void testStore() throws Exception {
        SuperProperties properties = createProperties();
        assertTrue(properties.isSpaceBetweenProperties());
        assertFalse(properties.isSpaceAfterComment());

        // one property
        properties.setProperty("foo", "bar");
        assertEquals("foo=bar\n", store(properties));

        properties.setIndent(4);
        assertEquals("    foo=bar\n", store(properties));

        // two properties
        properties.setProperty("number", "42");
        assertEquals("    foo=bar\n\n    number=42\n", store(properties));

        properties.setSpaceBetweenProperties(false);
        assertEquals("    foo=bar\n    number=42\n", store(properties));

        // one comment
        properties.setComment("foo", "foo comment");
        assertEquals("    # foo comment\n    foo=bar\n    number=42\n", store(properties));

        properties.setCommentIndent(0);
        assertEquals("    #foo comment\n    foo=bar\n    number=42\n", store(properties));

        properties.setCommentIndent(2);
        assertEquals("    #  foo comment\n    foo=bar\n    number=42\n", store(properties));

        properties.setSpaceAfterComment(true);
        assertEquals("    #  foo comment\n\n    foo=bar\n    number=42\n", store(properties));

        properties.setSpaceBetweenProperties(true);
        assertEquals("    #  foo comment\n\n    foo=bar\n\n    number=42\n", store(properties));

        // one attribute
        properties.getAttributes("foo").put("name", "value");
        assertEquals("    #  foo comment\n    #  @name=value\n\n    foo=bar\n\n    number=42\n", store(properties));

        properties.getAttributes("foo").put("name", null);
        assertEquals("    #  foo comment\n    #  @name\n\n    foo=bar\n\n    number=42\n", store(properties));        

        properties.getAttributes("foo").put("name", "");
        assertEquals("    #  foo comment\n    #  @name\n\n    foo=bar\n\n    number=42\n", store(properties));

        // two attribute
        properties.getAttributes("number").put("hidden", "yes");
        assertEquals("    #  foo comment\n    #  @name\n\n    foo=bar\n\n    #  @hidden=yes\n\n    number=42\n", store(properties));

        // key value separator
        properties = createProperties();
        properties.setProperty("foo", "bar");
        properties.setKeyValueSeparator(" ");
        assertEquals("foo bar\n", store(properties));

        properties.setKeyValueSeparator(":");
        assertEquals("foo:bar\n", store(properties));

        properties.setKeyValueSeparator(" = ");
        assertEquals("foo = bar\n", store(properties));

        properties.setKeyValueSeparator("XXXX");
        assertEquals("fooXXXXbar\n", store(properties));
    }

    public void testLoadComments() throws Exception {
        SuperProperties properties;

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  Comment\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Comment", properties.getComment("foo"));

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  Line1\n#  Line2\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Line1\nLine2", properties.getComment("foo"));

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  Comment\n#    Indented\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Comment\n  Indented", properties.getComment("foo"));

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  Comment\n#Outdented\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Comment\nOutdented", properties.getComment("foo"));
    }

    public void testLoadCommentsXml() throws Exception {
        SuperProperties properties;

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", null).getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "\nComment\n").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Comment\n", properties.getComment("foo"));

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "Line1\n  Line2\n").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Line1\nLine2\n", properties.getComment("foo"));

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "Line1\n  Line2\n      Indented").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Line1\nLine2\n    Indented", properties.getComment("foo"));

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "Line1\n  Line2\nOutdented").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals("Line1\nLine2\nOutdented", properties.getComment("foo"));
    }

    public void testLoadAttributes() throws Exception {
        SuperProperties properties;

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  @name=value\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("name", "value"), properties.getAttributes("foo"));

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  @name\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("name", ""), properties.getAttributes("foo"));

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  @name=\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("name", ""), properties.getAttributes("foo"));

        properties = createProperties();
        properties.load(new ByteArrayInputStream("#  @a=b\n#  @c=d\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("a", "b", "c", "d"), properties.getAttributes("foo"));
    }

    public void testLoadAttributesXml() throws Exception {
        SuperProperties properties;

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "@name=value").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("name", "value"), properties.getAttributes("foo"));

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "@name").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("name", ""), properties.getAttributes("foo"));

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "@name=").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("name", ""), properties.getAttributes("foo"));

        properties = createProperties();
        properties.loadFromXML(new ByteArrayInputStream(getXml("foo", "bar", "@a = b \n@ c = d ").getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(map("a", "b", "c", "d"), properties.getAttributes("foo"));
    }

    public void testIndentDetection() throws Exception {
        SuperProperties properties;
        properties = createProperties();
        assertEquals(0, properties.getIndent());
        assertEquals(1, properties.getCommentIndent());

        properties = createProperties();
        properties.load(new ByteArrayInputStream("    foo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(4, properties.getIndent());
        assertEquals(1, properties.getCommentIndent());

        properties = createProperties();
        properties.load(new ByteArrayInputStream("    #  Comment\n    foo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(4, properties.getIndent());
        assertEquals(2, properties.getCommentIndent());

        properties = createProperties();
        properties.load(new ByteArrayInputStream("    #  Line1\n#  Line2\n    foo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(4, properties.getIndent());
        assertEquals(2, properties.getCommentIndent());

        properties = createProperties();
        properties.load(new ByteArrayInputStream("    #  Comment\n#    Indented\n        foo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(4, properties.getIndent());
        assertEquals(2, properties.getCommentIndent());

        properties = createProperties();
        properties.load(new ByteArrayInputStream("    #  Comment\n#Outdented\nfoo=bar".getBytes()));
        assertEquals(singletonProperty("foo", "bar"), properties);
        assertEquals(4, properties.getIndent());
        assertEquals(2, properties.getCommentIndent());
    }

    protected String store(Properties properties) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);
        return new String(out.toByteArray());
    }

    protected SuperProperties createProperties() {
        return new SuperProperties();
    }

    protected static Map<String,String> map(String... keysAndValues) {
        Map<String,String> map = new LinkedHashMap<String,String>();
        for (int i = 0; i+1 < keysAndValues.length; i += 2) {
            String key = keysAndValues[i];
            String value = keysAndValues[i + 1];
            map.put(key, value);
        }
        return map;
    }

    private String getXml(String key, String value, String comment) {
        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buf.append("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n");
        buf.append("<properties>\n");

        if (comment != null) {
            buf.append("  <!--").append(comment).append("-->\n");
        }
        buf.append("  <entry key=\"").append(key).append("\">").append(value).append("</entry>\n");

        buf.append("</properties>\n");

        return buf.toString();
    }
}

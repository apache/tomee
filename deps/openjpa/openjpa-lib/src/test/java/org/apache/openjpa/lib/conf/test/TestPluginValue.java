/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.conf.test;

import org.apache.openjpa.lib.conf.PluginListValue;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.conf.StringValue;
import org.apache.openjpa.lib.conf.Value;
import org.apache.openjpa.lib.test.AbstractTestCase;

/**
 * Tests the {@link Value}, {@link PluginValue}, and
 * {@link PluginListValue} classes.
 *
 * @author Abe White
 */
public class TestPluginValue extends AbstractTestCase {

    public TestPluginValue(String test) {
        super(test);
    }

    public void testDefault() {
        defaultTest(new StringValue("testKey"));
        defaultTest(new PluginValue("testKey", true));
        defaultTest(new PluginListValue("testKey"));
        pluginDefaultTest(new PluginValue("testKey", true));
    }

    private void defaultTest(Value val) {
        assertNull(val.getString());
        val.setString("foo");
        assertEquals("foo", val.getString());
        val.setString(null);
        assertNull(val.getString());
        val.setDefault("xxx");
        val.setString(null);
        assertEquals("xxx", val.getString());
        val.setString("bar");
        assertEquals("bar", val.getString());
        val.setString(" ");
        assertEquals("xxx", val.getString());
        val.setString("yyy");
        assertEquals("yyy", val.getString());
        val.setString(null);
        assertEquals("xxx", val.getString());
    }

    private void pluginDefaultTest(PluginValue val) {
        val.setDefault("foo");
        val.setString("x=y");
        assertEquals("foo(x=y)", val.getString());
        val.set(new Integer(1));
        assertEquals("java.lang.Integer", val.getString());
        assertEquals(new Integer(1), val.get());
        val.set(null);
        assertEquals("foo", val.getString());
        assertEquals(null, val.get());
    }

    public void testAlias() {
        aliasTest(new StringValue("testKey"));
        aliasTest(new PluginValue("testKey", true));
        aliasTest(new PluginListValue("testKey"));
        emptyAliasTest(new StringValue("testKey"));
        emptyAliasTest(new StringValue("testKey"));
        pluginAliasTest(new PluginValue("testKey", true));
        pluginAliasTest(new PluginListValue("testKey"));
        pluginListAliasTest(new PluginListValue("testKey"));
    }

    private void aliasTest(Value val) {
        val.setAliases(new String[]{ "foo", "bar" });
        val.setDefault("bar");
        assertEquals("bar", val.getDefault());
        val.setString(null);
        assertEquals("foo", val.getString());
        val.setDefault("xxx");
        val.setString(null);
        assertEquals("xxx", val.getString());
        val.setDefault("bar");
        val.setString(null);
        assertEquals("foo", val.getString());
        val.setString("yyy");
        val.setAliases(new String[]{ "xxx", "yyy" });
        assertEquals("xxx", val.getString());
    }

    private void emptyAliasTest(Value val) {
        val.setDefault("foo");
        val.setAliases(new String[]{ "false", null });
        val.setString("false");
        assertEquals("false", val.getString());
        val.setString(null);
        assertEquals("foo", val.getString());
    }

    private void pluginAliasTest(Value val) {
        // test plugin name-only aliases
        val.setString("foo(biz=baz)");
        assertEquals("foo(biz=baz)", val.getString());
        val.setAliases(new String[]{ "xxx", "foo" });
        assertEquals("xxx(biz=baz)", val.getString());
    }

    private void pluginListAliasTest(Value val) {
        // and test on lists
        val.setString("foo(biz=baz), foo(a=b),xxx, foo, yyy");
        assertEquals("foo(biz=baz), foo(a=b), xxx, foo, yyy", val.getString());
        val.setAliases(new String[]{ "bar", "foo" });
        assertEquals("bar(biz=baz), bar(a=b), xxx, bar, yyy", val.getString());

        val.setAliases(new String[]{ "none", null });
        val.setString("none");
        assertTrue(((PluginListValue) val).getClassNames().length == 0);
    }

    public void testPluginListParsing() {
        PluginListValue val = new PluginListValue("testKey");
        assertEquals(0, val.getClassNames().length);
        val.setString("foo");
        assertEquals(1, val.getClassNames().length);
        assertEquals("foo", val.getClassNames()[0]);
        assertNull(val.getProperties()[0]);
        val.setString("foo()");
        assertEquals(1, val.getClassNames().length);
        assertEquals("foo", val.getClassNames()[0]);
        assertNull(val.getProperties()[0]);
        val.setString("foo(a=b)");
        assertEquals(1, val.getClassNames().length);
        assertEquals("foo", val.getClassNames()[0]);
        assertEquals("a=b", val.getProperties()[0]);
        val.setString("foo(a=b, c=\"d,e f\", g=\"h\")");
        assertEquals(1, val.getClassNames().length);
        assertEquals("foo", val.getClassNames()[0]);
        assertEquals("a=b, c=\"d,e f\", g=\"h\"", val.getProperties()[0]);
        val.setString("foo(a=b, c=\"d,e f\"), bar, biz(a=c, d=g), baz()");
        assertEquals(4, val.getClassNames().length);
        assertEquals("foo", val.getClassNames()[0]);
        assertEquals("a=b, c=\"d,e f\"", val.getProperties()[0]);
        assertEquals("bar", val.getClassNames()[1]);
        assertNull(val.getProperties()[1]);
        assertEquals("biz", val.getClassNames()[2]);
        assertEquals("a=c, d=g", val.getProperties()[2]);
        assertEquals("baz", val.getClassNames()[3]);
        assertNull(val.getProperties()[3]);
    }

    /**
     * Required because we use a 'testKey' dummy property name.
     */
    public String getTestKey() {
        return null;
    }

    /**
     * Required because we use a 'testKey' dummy property name.
     */
    public void setTestKey(String key) {
    }

    public static void main(String[] args) {
        main();
    }
}

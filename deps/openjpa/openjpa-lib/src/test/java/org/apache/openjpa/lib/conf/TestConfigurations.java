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
package org.apache.openjpa.lib.conf;

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.lib.test.AbstractTestCase;
import org.apache.openjpa.lib.util.Options;

/**
 * Tests the {@link Configurations} class.
 *
 * @author Abe White
 */
public class TestConfigurations extends AbstractTestCase {

    public TestConfigurations(String test) {
        super(test);
    }

    public void testParsePlugin() {
        String str = null;
        assertNull(Configurations.getClassName(str));
        assertNull(Configurations.getProperties(str));
        str = "foo";
        assertEquals("foo", Configurations.getClassName(str));
        assertNull(Configurations.getProperties(str));
        str = "a=b";
        assertNull(Configurations.getClassName(str));
        assertEquals("a=b", Configurations.getProperties(str));
        str = "a=b, c=d";
        assertNull(Configurations.getClassName(str));
        assertEquals("a=b, c=d", Configurations.getProperties(str));
        str = "foo(a=b, c=d)";
        assertEquals("foo", Configurations.getClassName(str));
        assertEquals("a=b, c=d", Configurations.getProperties(str));
        str = " foo( a=\"b,c d\", c=\"d\" ) ";
        assertEquals("foo", Configurations.getClassName(str));
        assertEquals("a=\"b,c d\", c=\"d\"", Configurations.getProperties(str));
        str = " foo( a='b,c d', c='d' ) ";
        assertEquals("foo", Configurations.getClassName(str));
        assertEquals("a='b,c d', c='d'", Configurations.getProperties(str));
    }

    public void testParseProperties() {
        Options opts = Configurations.parseProperties(null);
        assertEquals(0, opts.size());

        opts = Configurations.parseProperties(" foo=bar, biz=baz ");
        assertEquals(2, opts.size());
        assertEquals("bar", opts.getProperty("foo"));
        assertEquals("baz", opts.getProperty("biz"));

        opts = Configurations.parseProperties("foo=bar,biz=\"baz,=,baz\",x=y");
        assertEquals(3, opts.size());
        assertEquals("bar", opts.getProperty("foo"));
        assertEquals("baz,=,baz", opts.getProperty("biz"));
        assertEquals("y", opts.getProperty("x"));

        opts = Configurations.parseProperties
            ("foo=\"bar bar,10\",biz=\"baz baz\"");
        assertEquals(2, opts.size());
        assertEquals("bar bar,10", opts.getProperty("foo"));
        assertEquals("baz baz", opts.getProperty("biz"));
        opts = Configurations.parseProperties
            ("foo='bar bar,10',biz='baz baz'");
        assertEquals(2, opts.size());
        assertEquals("bar bar,10", opts.getProperty("foo"));
        assertEquals("baz baz", opts.getProperty("biz"));
    }

    public void testCombinePlugins() {
        assertPluginsCombined("jpa", null, 
            null, null,
            "jpa", null);
        assertPluginsCombined("jpa", null,
            "jpa", null,
            "jpa", null);
        assertPluginsCombined("jdo", null,
            "jpa", null,
            "jpa", null);
        assertPluginsCombined("jdo", new String[] { "foo", "bar" },
            "jpa", null,
            "jpa", null);
        assertPluginsCombined("jdo", new String[] { "foo", "bar" },
            "jpa", new String[] { "biz", "baz" },
            "jpa", new String[] { "biz", "baz" }); 
        assertPluginsCombined("jdo", new String[] { "foo", "bar" },
            null, new String[] { "biz", "baz" },
            "jdo", new String[] { "foo", "bar", "biz", "baz" }); 
        assertPluginsCombined(null, new String[] { "foo", "bar" },
            null, new String[] { "biz", "baz" },
            null, new String[] { "foo", "bar", "biz", "baz" }); 
        assertPluginsCombined(null, new String[] { "foo", "bar" },
            "jpa", new String[] { "biz", "baz" },
            "jpa", new String[] { "foo", "bar", "biz", "baz" }); 
        assertPluginsCombined("jpa", new String[] { "foo", "bar" },
            "jpa", new String[] { "biz", "baz" },
            "jpa", new String[] { "foo", "bar", "biz", "baz" }); 
        assertPluginsCombined("jpa", new String[] { "foo", "bar" },
            "jpa", new String[] { "foo", "baz" },
            "jpa", new String[] { "foo", "baz" }); 
    }

    private void assertPluginsCombined(String cls1, String[] props1,
        String cls2, String[] props2, String expCls, String[] expProps) {
        String plugin1 = Configurations.getPlugin(cls1, 
            Configurations.serializeProperties(toProperties(props1)));
        String plugin2 = Configurations.getPlugin(cls2, 
            Configurations.serializeProperties(toProperties(props2)));

        String res = Configurations.combinePlugins(plugin1, plugin2);
        String resCls = Configurations.getClassName(res);
        Map resProps = Configurations.parseProperties(Configurations.
            getProperties(res));
        assertEquals(expCls, resCls);
        assertEquals(toProperties(expProps), resProps);
    }

    private static Map toProperties(String[] props) {
        Map map = new HashMap();
        if (props != null)
            for (int i = 0; i < props.length; i++)
                map.put(props[i], props[++i]);
        return map;
    }

    public static void main(String[] args) {
        main(TestConfigurations.class);
    }
}

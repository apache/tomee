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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


public class TestConfigurationProviderPrefixes
    extends TestCase {
    
    private static final String CUSTOM_PREFIX = 
        TestConfigurationProviderPrefixes.class.getName();

    private String[] _origPrefixes;

    public void setUp() {
        _origPrefixes = ProductDerivations.getConfigurationPrefixes();
        List l = new ArrayList(Arrays.asList(_origPrefixes));
        l.add(CUSTOM_PREFIX);
        ProductDerivations.setConfigurationPrefixes(
            (String[]) l.toArray(new String[0]));
    }
    
    public void tearDown() {
        ProductDerivations.setConfigurationPrefixes(_origPrefixes);
    }
    
    public void testPrefixContents() { 
        String[] prefixes = ProductDerivations.getConfigurationPrefixes();
        assertEquals(CUSTOM_PREFIX, prefixes[prefixes.length - 1]);
        assertEquals("openjpa", prefixes[0]);
    }
    
    public void testPartialKeyAndNullMap() {
        assertEquals("openjpa.Foo", "Foo", (Map) null, null);
    }
        
    public void testPartialKeyWithInvalidPrefix() {
        Map map = new HashMap();
        map.put("bar.Foo", "value");
        assertEquals("openjpa.Foo", "Foo", map, null);
    }

    public void testPartialKeyWithoutMatch() {
        Map map = new HashMap();
        map.put("bar.Baz", "value");
        assertEquals("openjpa.Foo", "Foo", map, null);
    }

    public void testPartialKeyWithOpenJPAMatch() {
        Map map = new HashMap();
        map.put("openjpa.Foo", "value");
        assertEquals("openjpa.Foo", "Foo", map, "value");
    }

    public void testPartialKeyWithCustomMatch() {
        Map map = new HashMap();
        map.put(CUSTOM_PREFIX + ".Foo", "value");
        assertEquals(CUSTOM_PREFIX + ".Foo", "Foo", map, "value");
    }
    
    public void testPartialKeyDuplicateFullKeys() {
        Map map = new HashMap();
        map.put(CUSTOM_PREFIX + ".Foo", "value");
        map.put("openjpa.Foo", "value");
        try {
            ProductDerivations.getConfigurationKey("Foo", map);
            fail("duplicate keys should result in an IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    private static void assertEquals(String fullKey, String partialKey, 
        Map map, Object value) {
        assertEquals(fullKey, ProductDerivations.getConfigurationKey(
            partialKey, map));
        if (map != null)
            assertEquals(value, map.get(fullKey));
    }
}

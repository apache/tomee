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
package org.apache.openjpa.lib.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests the {@link TypedProperties} type.
 *
 * @author Abe White
 */
public class TestTypedProperties extends TestCase {

    private TypedProperties _props = null;
    private TypedProperties _defs = null;

    public TestTypedProperties(String test) {
        super(test);
    }

    public void setUp() {
        _props = new TypedProperties();
        _props.setProperty("bool", "true");
        _props.setProperty("int", "1");
        _props.setProperty("long", "2");
        _props.setProperty("float", "1.1");
        _props.setProperty("double", "2.2");

        _defs = new TypedProperties(_props);
    }

    /**
     * Tests basic typed gets.
     */
    public void testTypes() {
        assertTrue(_props.getBooleanProperty("bool"));
        assertEquals(1, _props.getIntProperty("int"));
        assertEquals(2L, _props.getLongProperty("long"));
        assertEquals(1.1F, _props.getFloatProperty("float"), 0.01F);
        assertEquals(2.2D, _props.getDoubleProperty("double"), 0.01D);
        assertEquals("2.2", _props.getProperty("double"));
    }

    /**
     * Tests the defaults returned for missing keys.
     */
    public void testNoDefaults() {
        assertTrue(!_props.getBooleanProperty("bool2"));
        assertEquals(0, _props.getIntProperty("int2"));
        assertEquals(0L, _props.getLongProperty("long2"));
        assertEquals(0F, _props.getFloatProperty("float2"), 0F);
        assertEquals(0D, _props.getDoubleProperty("double2"), 0D);
        assertEquals(null, _props.getProperty("double2"));
    }

    /**
     * Tests the defaults returned by keys found in the default
     * backing properties instance.
     */
    public void testDefaults() {
        assertTrue(_defs.getBooleanProperty("bool"));
        assertEquals(1, _defs.getIntProperty("int"));
        assertEquals(2L, _defs.getLongProperty("long"));
        assertEquals(1.1F, _defs.getFloatProperty("float"), 0.01F);
        assertEquals(2.2D, _defs.getDoubleProperty("double"), 0.01D);
        assertEquals("2.2", _defs.getProperty("double"));
    }

    /**
     * Tests that given defaults works.
     */
    public void testGivenDefaults() {
        assertTrue(_props.getBooleanProperty("bool2", true));
        assertEquals(1, _props.getIntProperty("int2", 1));
        assertEquals(2L, _props.getLongProperty("long2", 2L));
        assertEquals(1.1F, _props.getFloatProperty("float2", 1.1F), 0.01F);
        assertEquals(2.2D, _props.getDoubleProperty("double2", 2.2D), 0.01D);
        assertEquals("2.2", _props.getProperty("double2", "2.2"));

        assertTrue(_defs.getBooleanProperty("bool", false));
        assertEquals(1, _defs.getIntProperty("int", 2));
        assertEquals(2L, _defs.getLongProperty("long", 3L));
        assertEquals(1.1F, _defs.getFloatProperty("float", 2.2F), 0.01F);
        assertEquals(2.2D, _defs.getDoubleProperty("double", 3.3D), 0.01D);
        assertEquals("2.2", _defs.getProperty("double", "3.3"));
    }

    public static Test suite() {
        return new TestSuite(TestTypedProperties.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}

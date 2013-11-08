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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import org.apache.openjpa.lib.conf.*;
import org.apache.openjpa.lib.test.AbstractTestCase;

/**
 * Tests the {@link ConfigurationImpl} type. This needs to be placed
 * in a sub-package so that it can have its own localizer.properties
 * properties, which are required for the bean descriptors used by the
 * configuration framework {@link Value}.
 * 
 * This test assumes a <code>"test.properties"</code> file available in
 * classpath and also system property <code>"openjpatest.properties"</code>
 * is set to <code>"test.properties"</code>.
 *
 * @author Abe White
 * @author Pinaki Poddar
 */
public class TestConfigurationImpl extends AbstractTestCase {

    private ConfigurationTest _conf = new ConfigurationTest();

    public TestConfigurationImpl(String test) {
        super(test);
    }

    public void setUp() {
        System.setProperty("openjpatest.properties", "test.properties");
    }

    public void tearDown() throws Exception {
        System.setProperty("openjpatest.properties", "");
    	super.tearDown();
    }

    /**
     * Test that global properties are found and loaded.
     */
    public void testGlobals() {
        System.setProperty("openjpa.sysKey", "sysvalue");
        assertNull(_conf.getTestKey());
        assertNull(_conf.getSysKey());
        assertNull(_conf.getPluginKey());
        assertNull(_conf.getObjectKey());
        assertTrue(_conf.loadGlobals());
        assertEquals("testvalue", _conf.getTestKey());
        assertEquals("sysvalue", _conf.getSysKey());
        assertNull(_conf.getPluginKey());
        assertNull(_conf.getObjectKey());

        // override the properties location to a non-existant value
        _conf.setTestKey(null);
        _conf.setSysKey(null);
        System.setProperty("openjpatest.properties", "foo.properties");
        try {
            assertTrue(!_conf.loadGlobals());
            fail("Should have thrown exception for missing resource.");
        } catch (MissingResourceException mre) {
        }

        // set back for remainder of tests
        System.setProperty("openjpatest.properties", "test.properties");
        System.setProperty("openjpa.pluginKey", "java.lang.Object");
        assertTrue(_conf.loadGlobals());
        assertEquals("testvalue", _conf.getTestKey());
        assertEquals("sysvalue", _conf.getSysKey());
        assertEquals("java.lang.Object", _conf.getPluginKey());
        assertNotNull(_conf.getPluginKeyInstance());
        assertNull(_conf.getObjectKey());
    }

    /**
     * Test that the configuration is serialized to properties correctly.
     */
    public void testToProperties() {
        assertTrue(_conf.loadGlobals());
        assertEquals("testvalue", _conf.getTestKey());
        Map props = _conf.toProperties(false);
        assertEquals("testvalue", props.get("openjpa.testKey"));
        assertFalse(props.containsKey("openjpa.objectKey"));
        _conf.setTestKey("foo");
        _conf.setPluginKey(new Object());
        _conf.setObjectKey(new Object());
        props = _conf.toProperties(false);
        assertEquals("foo", props.get("openjpa.testKey"));
        assertEquals("java.lang.Object", props.get("openjpa.pluginKey"));
        assertFalse(props.containsKey("openjpa.objectKey"));
    }

    /**
     * Tests properties caching.
     */
    public void testPropertiesCaching() {
        _conf.setTestKey("val");
        _conf.setPluginKey("java.lang.Object");
        Map props1 = _conf.toProperties(false);
        Map props2 = _conf.toProperties(false);
        _conf.setObjectKey(new Object());
        assertNotNull(_conf.getPluginKeyInstance()); // instantiate
        Map props3 = _conf.toProperties(false);
        _conf.setTestKey("changed");
        Map props4 = _conf.toProperties(false);
        _conf.setPluginKey(new Integer(1));
        Map props5 = _conf.toProperties(false);
        assertEquals(props1, props2);
        assertEquals(props1, props3);
        assertNotEquals(props1, props4);
        assertNotEquals(props4, props5);
    }

    /**
     * Test the equals method.
     */
    public void testEquals() {
        ConfigurationTest conf = new ConfigurationTest();
        conf.setTestKey(_conf.getTestKey());
        conf.setSysKey(_conf.getSysKey());
        conf.setPluginKey(_conf.getPluginKey());
        conf.setObjectKey(_conf.getObjectKey());
        assertEquals(_conf, conf);

        conf.setTestKey("newval");
        assertTrue(!_conf.equals(conf));
        conf.setTestKey(_conf.getTestKey());
        assertEquals(_conf, conf);

        conf.setObjectKey(new Object());
        assertEquals(_conf, conf);

        conf.setPluginKey(new StringBuffer());
        assertTrue(!_conf.equals(conf));
    }

    /**
     * Test using bean introspection.
     */
    public void testBeanAccessors() throws Exception {
        PropertyDescriptor[] pds = _conf.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            assertNotNull(pds[i].getShortDescription());
            assertNotNull(pds[i].getDisplayName());

            assertNotNull(pds[i].getWriteMethod());
            assertNotNull(pds[i].getReadMethod());

            pds[i].getReadMethod().invoke(_conf, (Object[]) null);

            Method setter = pds[i].getWriteMethod();
            Method getter = pds[i].getReadMethod();
            Class param = pds[i].getReadMethod().getReturnType();

            Object setVal = null;
            if (param == int.class)
                setVal = randomInt();
            else if (param == long.class)
                setVal = randomLong();
            else if (param == String.class)
                setVal = randomString();
            else if (param == boolean.class)
                setVal = new Boolean(!(((Boolean) getter.invoke(_conf,
                    (Object[]) null)).booleanValue()));
            else
                continue;

            setter.invoke(_conf, new Object []{ setVal });
            assertEquals(setVal, getter.invoke(_conf, (Object[]) null));
        }
    }

    /**
     * Test freezing.
     */
    public void testFreezing() {
        assertTrue(!_conf.isReadOnly());
        _conf.setReadOnly(Configuration.INIT_STATE_FROZEN);
        assertTrue(_conf.isReadOnly());
        try {
            _conf.setTestKey("bar");
            fail("Allowed set on read only configuration.");
        } catch (RuntimeException re) {
        }
        try {
            Properties p = new Properties();
            p.put("x", "y");
            _conf.fromProperties(p);
            fail("Allowed fromMap on read only configuration.");
        } catch (RuntimeException re) {
        }
    }

    /**
     * Test serialization.
     */
    public void testSerialization() throws Exception {
        assertTrue(_conf.loadGlobals());
        _conf.setTestKey("testvalue");
        _conf.setSysKey("sysvalue");
        _conf.setObjectKey(new Object());
        _conf.setPluginKey(new Object());

        ConfigurationTest copy = (ConfigurationTest) roundtrip(_conf, true);
        assertEquals("testvalue", copy.getTestKey());
        assertEquals("sysvalue", copy.getSysKey());
        assertNull(copy.getObjectKey());
        assertEquals("java.lang.Object", copy.getPluginKey());
        assertNotNull(copy.getPluginKeyInstance());

        copy.setTestKey("testvalue2");
        copy.setSysKey("sysvalue2");
        copy.setPluginKey(new StringBuffer());

        ConfigurationTest copy2 = (ConfigurationTest) roundtrip(copy, true);
        assertEquals("testvalue2", copy2.getTestKey());
        assertEquals("sysvalue2", copy2.getSysKey());
        assertNull(copy2.getObjectKey());
        assertEquals("java.lang.StringBuffer", copy2.getPluginKey());
        assertEquals("", copy2.getPluginKeyInstance().toString());
    }
    
    public void testProductDerivationCloseCallback() {
        // toggle the static. This will be reset by the close invocation.
        ConfigurationTestProductDerivation.closed = false;
        _conf.close();
        assertTrue(ConfigurationTestProductDerivation.closed);
    }

    public static void main(String[] args) {
        main();
    }

    private static class ConfigurationTest extends ConfigurationImpl {

        private final StringValue _testKey;
        private final StringValue _sysKey;
        private final PluginValue _pluginKey;
        private final ObjectValue _objectKey;

        public ConfigurationTest() {
            this(true);
        }

        public ConfigurationTest(boolean canSetPlugin) {
            super(false);
            _testKey = addString("testKey");
            _sysKey = addString("sysKey");
            _pluginKey = addPlugin("pluginKey", canSetPlugin);
            _objectKey = addObject("objectKey");
        }

        public String getTestKey() {
            return _testKey.get();
        }

        public void setTestKey(String val) {
            _testKey.set(val);
        }

        public String getSysKey() {
            return _sysKey.get();
        }

        public void setSysKey(String val) {
             _sysKey.set(val);
        }

        public String getPluginKey() {
            return _pluginKey.getString();
        }

        public void setPluginKey(String val) {
            _pluginKey.setString(val);
        }

        public Object getPluginKeyInstance() {
            if (_pluginKey.get() == null)
                return _pluginKey.instantiate(Object.class, this);
            return _pluginKey.get();
        }

        public void setPluginKey(Object val) {
            _pluginKey.set(val);
        }

        public Object getObjectKey() {
            return _objectKey.get();
        }

        public void setObjectKey(Object val) {
            _objectKey.set(val);
        }

        public void deriveObjectKey(Object val) {
            _objectKey.set(val, true);
        }
        
        protected boolean isInvalidProperty(String s) {
            if ("openjpatest.properties".equals(s))
                return false;
            else 
                return super.isInvalidProperty(s);
        }
    }
}

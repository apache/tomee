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
package org.apache.openjpa.persistence.simple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.conf.Value;
import org.apache.openjpa.persistence.AutoClearType;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * This test case tests the getProperties() and getSupportedProperties() methods
 * for the EntityManager and EntityManagerFactory.
 * 
 * @author Dianne Richards
 * @author Pinaki Poddar
 * 
 */
public class TestPropertiesMethods extends AbstractPersistenceTestCase {
    private static final String UNIT_NAME = "test";
    private OpenJPAEntityManagerFactory emf;
    private OpenJPAEntityManager em;
    
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        Map config = new HashMap();
        config.putAll(System.getProperties());
            
/* numeric   */ config.put("openjpa.DataCacheTimeout", 300);
/* num enum  */ config.put("openjpa.AutoClear", 0);
/* hidden    */ config.put("openjpa.Connection2Password", "xyz");
/* plug-in   */ config.put("openjpa.ProxyManager", "default(TrackChanges=false)");
/* no funky  */ config.put("openjpa.DynamicEnhancementAgent", "false");

// following properties are not used becuase that makes the test dependent on database specifics
/* equiv key */ //config.put("javax.persistence.jdbc.url", "jdbc:derby:target/database/test;create=true"); 
/* prime use */ //config.put("openjpa.ConnectionUserName", "root");
            
        emf = OpenJPAPersistence.cast(Persistence.createEntityManagerFactory(UNIT_NAME, config));
        assertNotNull(emf);
        em = OpenJPAPersistence.cast(emf.createEntityManager());
    }
    
    @Override
    public void tearDown() {
        closeEM(em);
        em = null;
        closeEMF(emf);
        emf = null;
    }
    
//    public void testProperties() {
//        print("EMF Properties", emf.getProperties());
//        print("EMF Supported Properties", emf.getSupportedProperties());
//        print("EM Properties", em.getProperties());
//        print("EM Supported Properties", emf.getSupportedProperties());
//    }
    
    public void testConfigurationPrefixes() {
        String[] prefixes = ProductDerivations.getConfigurationPrefixes();
        assertEquals("openjpa", prefixes[0]);
        assertTrue(Arrays.asList(prefixes).contains("javax.persistence"));
    }
    
    public void testEMNumericPropertyValueForEnumTypeIsReturnedAsString() {
        Map<String, Object> props = em.getProperties();

        assertProperty("openjpa.AutoClear", props, AutoClearType.DATASTORE);
        assertProperty("openjpa.IgnoreChanges", props, Boolean.FALSE);
    }

    public void testEMPluginPropertyParameterIsPreserved() {
        Map<String,Object> props = emf.getProperties();
        Object val = props.get("openjpa.ProxyManager");
        assertNotNull(val);
        assertTrue(val instanceof String);
        String proxyManager = (String)val;
        assertEquals("default(TrackChanges=false)", proxyManager);
    }
    
    /**
     * Test the EntityManagerFactory getProperties() method.
     */
    public void testFactoryPropertiesContainDefaultValue() {
        Map<String, Object> props = emf.getProperties();

        assertEquals("default", props.get("openjpa.DataCacheManager"));
        assertEquals(300, props.get("openjpa.DataCacheTimeout"));
    }
    
    public void testFactoryPropertiesContainUserSpecifiedValue() {
        Map<String, Object> props = emf.getProperties();
        assertEquals(new Integer(300), props.get("openjpa.DataCacheTimeout"));
    }

    public void testFactoryPropertiesAddPlatformOrVendor() {
        Map<String, Object> props = emf.getProperties();
        assertTrue(props.containsKey("Platform"));
        assertNotNull(props.containsKey("VendorName"));
    }

    /**
     * Test the EntityManagerFactory getSupportedProperties() method.
     */
    public void testFactorySupportedProperties() {
        Set<String> props = emf.getSupportedProperties();
        assertTrue(props.contains("openjpa.IgnoreChanges"));
    }

    /**
     * Test the EntityManager getSupportedProperties() method.
     */
    public void testEMGetSupportedProperties() {
        Set<String> emSupportedProperties = em.getSupportedProperties();
        assertNotNull(emSupportedProperties);
        assertTrue(emSupportedProperties.contains("openjpa.AutoDetach"));
        
        // Make sure the all possible keys are returned
        assertTrue(emSupportedProperties.contains("javax.persistence.lock.timeout"));
        assertTrue(emSupportedProperties.contains("openjpa.LockTimeout"));
        
        // Make sure the spec property for query timeout, that only has one
        // key, is returned.
        assertTrue(emSupportedProperties.contains("javax.persistence.query.timeout"));
        assertFalse(emSupportedProperties.contains("openjpa.javax.persistence.query.timeout"));
    }
    
    /**
     * Property values preserve the type in which they were specified in the facade.
     * Enumerated property such as AutoClear has different representation in kernel
     * (as int) and in facade (as enum). The test verifies that {@link EntityManager#getProperties()} 
     * return enum type rather than an integer.
     */
    public void testEMFPropertyValueTypeIsPreserved() {
        Map<String, Object> props = emf.getProperties();
        
        Object autoClear = props.get("openjpa.AutoClear");
        print("EMF Properties ", props);
        assertTrue("AutoClear " + autoClear + " is of " + autoClear.getClass(), autoClear instanceof AutoClearType);
        assertEquals(AutoClearType.DATASTORE, autoClear);
        
        Object ignoreChanges = props.get("openjpa.IgnoreChanges");
        assertTrue(ignoreChanges instanceof Boolean);
    }

    public void testEMFPluginPropertyParameterIsPreserved() {
        Map<String,Object> props = emf.getProperties();
        Object val = props.get("openjpa.ProxyManager");
        assertNotNull(val);
        assertTrue(val instanceof String);
        String proxyManager = (String)val;
        assertEquals("default(TrackChanges=false)", proxyManager);
    }
    
    /**
     * Certain logical property such as ConnectionUserName can appear under different
     * keys such as openjpa.ConnectionUserName or javax.persistence.jdbc.user.
     * The key under which the property value appears depends on the key under which
     * property value was loaded into the configuration.  
     * 
     */
    // Not run because that makes these tests database specific
    public void xtestLoadKeyWithEquivalentPropertyKey() {
        Map<String, Object> props = emf.getProperties();

        // This property was loaded with equivalent javax. key
        assertFalse(props.containsKey("openjpa.ConnectionURL"));
        assertTrue(props.containsKey("javax.persistence.jdbc.url"));
    }
    
    // Not run because that makes these tests database specific
    public void xtestLoadKeyWithPrimaryPropertyKey() {
        Map<String, Object> props = emf.getProperties();

        // This property was loaded with primary openjpa. key
        assertTrue(props.containsKey("openjpa.ConnectionUserName"));
        assertFalse(props.containsKey("javax.persistence.jdbc.user"));
    }
    
    /**
     * Property values preserve the type in which they were specified in the facade.
     * Enumerated property such as AutoClear has different representation in kernel
     * (as int) and in facade (as enum). The test verifies that {@link EntityManager#getProperties()} 
     * return enum type rather than an integer.
     */
    public void testPropertyValueTypeIsPreserved() {
        Map<String, Object> props = em.getProperties();
        assertProperty("openjpa.AutoClear", props, AutoClearType.DATASTORE);
        assertProperty("openjpa.IgnoreChanges", props, Boolean.FALSE);
    }

    public void testPluginPropertyParameterIsPreserved() {
        Map<String,Object> props = emf.getProperties();
        assertProperty("openjpa.ProxyManager", props, "default(TrackChanges=false)");
    }
    
    /**
     * Test that property value changes are reflected when mutated directly.
     */
    public void testChangePropertyValue() {
        Map<String, Object> props = em.getProperties();
        Boolean original = (Boolean)props.get("openjpa.IgnoreChanges");
        assertNotNull(original);
        
        Boolean invert = !original.booleanValue();
        em.setIgnoreChanges(invert);
        
        assertProperty("openjpa.IgnoreChanges", em.getProperties(), invert);
    }
    
    /**
     * Test that property value changes are reflected when instantiated with configuration.
     */
    public void testConfiguredPropertyValue() {
        Map<String, Object> props = em.getProperties();
        Boolean original = (Boolean)props.get("openjpa.IgnoreChanges");
        assertNotNull(original);
        
        Map<String,Boolean> config = new HashMap<String, Boolean>();
        Boolean invert = !original.booleanValue();
        config.put("openjpa.IgnoreChanges", invert);
        
        EntityManager em2 = emf.createEntityManager(config);
        assertProperty("openjpa.IgnoreChanges", em2.getProperties(), invert);
    }

    
    public void testEquivalentKeysArePresentInSupportedProperties() {
        Set<String> keys = em.getSupportedProperties();
        assertTrue(keys.contains("openjpa.ConnectionURL"));
        assertTrue(keys.contains("javax.persistence.jdbc.url"));
    }
    
    public void testPasswordValuesAreInvisible() {
        Map<String, Object> props = em.getProperties();
        assertProperty("openjpa.Connection2Password", props, Value.INVISIBLE);
    }
    
    void assertProperty(String prop, Map props) {
        assertProperty(prop, props, null);
    }
    
    void assertProperty(String prop, Map props, Object expected) {
        assertTrue(prop + " not present", props.containsKey(prop));
        Object actual = props.get(prop);
        if (expected != null) {
            assertTrue(prop + ": Actual " + actual.getClass() + " does not match expected " + expected.getClass(),
                expected.getClass().isAssignableFrom(actual.getClass()));
            assertEquals(prop + " value does not match", expected, actual);
        }
    }
    
    void print(String message, Map<String, Object> props) {
        System.err.println(message);
        for (Map.Entry<String, Object> e : props.entrySet()) {
            System.err.println(e.getKey() + ":" + e.getValue() + " [" + (e.getValue() == null ?
                    "" : e.getValue().getClass().getSimpleName()) + "]");
        }
    }
    void print(String message, Set<String> props) {
        System.err.println(message);
        for (String p : props) {
            System.err.println(p);
        }
    }
    

}

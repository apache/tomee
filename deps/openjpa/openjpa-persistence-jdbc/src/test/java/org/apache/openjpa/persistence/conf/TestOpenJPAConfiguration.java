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
package org.apache.openjpa.persistence.conf;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.common.utils.BufferedLogFactory;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.PluginValue;
import org.apache.openjpa.lib.conf.Value;

/**
 * <p>Tests the JDO configuration classes.</p>
 *
 * @author Marc Prud'hommeaux
 */
@AllowFailure(message="excluded")
public class TestOpenJPAConfiguration
    extends AbstractTestCase {

    public TestOpenJPAConfiguration(String test) {
        super(test, "confcactusapp");
    }

    /**
     * Test that you can set the connection factory and other properties as
     * objects.
     */
    public void testSetObjects() {
        Map map = new HashMap();
        Object cfactory = new Object();
        Object cfactory2 = new Object();
        map.put("openjpa.ConnectionFactory", cfactory);
        map.put("openjpa.ConnectionFactory2", cfactory2);
        map.put("openjpa.Optimistic", Boolean.FALSE);
        map.put("openjpa.LockTimeout", new Integer(503));
        map.put("javax.persistence.query.timeout", new Integer(1500));

        // use new conf so no unexpected restrictions on type of connection
        // factory
        OpenJPAConfiguration conf = new OpenJPAConfigurationImpl(true, false);
        conf.fromProperties(map);
        assertEquals(cfactory, conf.getConnectionFactory());
        assertEquals(cfactory2, conf.getConnectionFactory2());
        assertEquals(false, conf.getOptimistic());
        assertEquals(false, conf.getPostLoadOnMerge());
        assertEquals(503, conf.getLockTimeout());
        assertEquals(1500, conf.getQueryTimeout());

        OpenJPAConfiguration conf2 = new OpenJPAConfigurationImpl(true, false);
        conf2.fromProperties(map);
        assertEquals(conf, conf2);

        Map p = conf.toProperties(false);
        assertTrue(!p.containsKey("openjpa.ConnectionFactory"));
        assertTrue(!p.containsKey("openjpa.ConnectionFactory2"));
        assertEquals("false", p.get("openjpa.Optimistic"));
        assertEquals("503", p.get("openjpa.LockTimeout"));
        assertEquals(p, conf2.toProperties(false));

        map.put("openjpa.LockTimeout", new Integer(504));
        OpenJPAConfiguration conf3 = new OpenJPAConfigurationImpl(true, false);
        conf3.fromProperties(map);
        assertNotEquals(conf, conf3);
    }

    public void testClassAliases()
        throws Exception {
        OpenJPAConfiguration conf = getConfiguration();
        if (!(conf instanceof OpenJPAConfigurationImpl))
            return;

        Value[] values = ((OpenJPAConfigurationImpl) conf).getValues();
        String[] aliases;
        String clsName;
        List failures = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            if (!(values[i] instanceof PluginValue))
                continue;

            aliases = values[i].getAliases();
            for (int j = 0; j < aliases.length; j += 2) {
                try {
                    clsName = Configurations.getClassName(aliases[j + 1]);
                    if (clsName != null)
                        Class.forName(clsName);
                } catch (ClassNotFoundException cnfe) {
                    failures.add("Key: " + aliases[j] + " for property "
                        + values[i].getProperty() + " does not list a valid "
                        + "class: " + aliases[j + 1]);
                } catch (UnsupportedClassVersionError ucve) {
                    //### JDK 5 plugin; ignore
                }
            }
        }
        if (failures.size() != 0)
            fail(failures.toString());
    }

    public void testBeanAccessors()
        throws Exception {
        OpenJPAConfiguration conf = getConfiguration();
        OpenJPAConfigurationImpl simp = (OpenJPAConfigurationImpl) conf.clone();

        Value[] values = simp.getValues();
        PropertyDescriptor[] pds = simp.getPropertyDescriptors();
        List failures = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            try {
                assertNotNull(pds[i].getShortDescription());
                assertNotNull(pds[i].getDisplayName());
                
                // skip the EntityManagerFactory property added by
                // product derivation code as it has no accessor methods
                if ("EntityManagerFactory".equals(values[i].getProperty()))
                        continue;
                        
                Method getter = pds[i].getReadMethod();
                Method setter = pds[i].getWriteMethod();
                assertNotNull(getter);
                assertNotNull(setter);

                assertNotNull("Missing attribute ("
                    + Configuration.ATTRIBUTE_TYPE
                    + ") for property " + pds[i].getName(),
                    pds[i].getValue(Configuration.ATTRIBUTE_TYPE));

                assertNotNull("Missing attribute ("
                    + Configuration.ATTRIBUTE_CATEGORY
                    + ") for property " + pds[i].getName(),
                    pds[i].getValue(Configuration.ATTRIBUTE_CATEGORY));

                assertNotNull("Missing attribute ("
                    + Configuration.ATTRIBUTE_ORDER
                    + ") for property " + pds[i].getName(),
                    pds[i].getValue(Configuration.ATTRIBUTE_ORDER));

                pds[i].getReadMethod().invoke(simp, (Object[]) null);

            } catch (Exception e) {
                failures.add(pds[i].getName());
            }
        }
        if (failures.size() != 0)
            fail("not all properties had valid comments / setters / getters."
                + " failed props: " + failures);
    }

    /**
     * Tests that invalid plug-in values throw the appropriate exception
     * type.
     */
    public void testInvalidPlugins() {
        OpenJPAConfiguration config = new OpenJPAConfigurationImpl();
        config.setLog("log3j");
        try {
            config.getLogFactory().getLog("Foo");
            fail("getting the Foo log should have failed");
        } catch (RuntimeException re) {
            // as expected ... make sure the exception suggests the
            // name "log4j" in the message
            assertTrue(-1 != re.getMessage().indexOf("log4j"));
        }
    }

    public void testInvalidConfigurationWarnings() {
        Properties props = new Properties();
        props.setProperty("openjpa.MaxxFetchDepth", "1");

        OpenJPAConfiguration config = new OpenJPAConfigurationImpl();
        // track the messages
        BufferedLogFactory log = new BufferedLogFactory();
        config.setLogFactory(log);

        config.fromProperties(props);

        // make sure we got a warning that contains the string with the
        // bad property name and a hint for the valid property name.
        log.assertLogMessage("*\"openjpa.MaxxFetchDepth\"*");
        log.assertLogMessage("*\"openjpa.MaxFetchDepth\"*");

        log.clear();

        // now make sure we do *not* try to validate sub-configurations (such
        // as openjpa.jdbc.Foo).
        props.clear();
        props.setProperty("openjpa.jdbc.Foo", "XXX");
        props.setProperty("oponjpa", "XXX");
        config.fromProperties(props);
        log.assertNoLogMessage("*\"openjpa.jdbc.Foo\"*");
        log.assertNoLogMessage("*\"oponjpa\"*");
    }

    /**
     * Tests that invalid fixed-list values throw the appropriate exception
     * type.
     */
    public void testInvalidNonPluginValues() {
        OpenJPAConfiguration config = new OpenJPAConfigurationImpl();
        try {
            config.setConnectionFactoryMode("aoeu");
            fail("setting the ConnectionFactoryMode to aoeu should fail");
        } catch (RuntimeException re) {
            // as expected ... make sure the exception suggests the
            // valid names in the message.
            assertTrue(-1 != re.getMessage().indexOf("managed"));
            assertTrue(-1 != re.getMessage().indexOf("local"));
            assertTrue(-1 != re.getMessage().indexOf("true"));
            assertTrue(-1 != re.getMessage().indexOf("false"));
        }
    }
}

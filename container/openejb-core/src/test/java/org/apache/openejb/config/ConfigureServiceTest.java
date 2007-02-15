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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ConfigureServiceTest extends TestCase {

    public void testConfigureService() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        // We should be able to create a default definition
        StatelessSessionContainerInfo defaultStatelessContainer = factory.configureService(StatelessSessionContainerInfo.class);
        assertNotNull(defaultStatelessContainer);
        assertNotNull(defaultStatelessContainer.id);
        assertNotNull(defaultStatelessContainer.className);
        assertNotNull(defaultStatelessContainer.constructorArgs);
        assertNotNull(defaultStatelessContainer.properties);

        // We should be able to create one of these with a different name
        String containerId = "My Stateless Container";
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(StatelessSessionContainerInfo.class, containerId, null, null, null);

        assertNotNull(myStatelessContainer);
        assertEquals(containerId, myStatelessContainer.id);
        assertEquals(defaultStatelessContainer.className, myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);

    }

    public void testConfigureService2() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        // We should be able to create one of these with a different name
        String containerId = "MyContainer";
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(StatelessSessionContainerInfo.class, containerId, null, "org.acme#CheddarContainer", null);

        assertNotNull(myStatelessContainer);
        assertEquals(containerId, myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Yummy Cheese", myStatelessContainer.properties.getProperty("myProperty"));
    }

    public void testConfigureServiceOverriddenProperty() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        Properties properties = new Properties();
        properties.setProperty("myProperty", "Cheese is good");

        String containerId = "MyContainer";
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(StatelessSessionContainerInfo.class, containerId, properties, "org.acme#CheddarContainer", null);

        assertNotNull(myStatelessContainer);
        assertEquals(containerId, myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Cheese is good", myStatelessContainer.properties.getProperty("myProperty"));
    }

    public void testConfigureServiceAddedProperty() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        Properties properties = new Properties();
        properties.setProperty("anotherProperty", "Cheese is good");

        String containerId = "MyContainer";
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(StatelessSessionContainerInfo.class, containerId, properties, "org.acme#CheddarContainer", null);

        assertNotNull(myStatelessContainer);
        assertEquals(containerId, myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Yummy Cheese", myStatelessContainer.properties.getProperty("myProperty"));
        assertNotNull(myStatelessContainer.properties.getProperty("anotherProperty"));
        assertEquals("Cheese is good", myStatelessContainer.properties.getProperty("anotherProperty"));
    }


}

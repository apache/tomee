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
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Resource;

import java.net.URI;

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
        Container container = new Container("My Stateless Container");
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(container, StatelessSessionContainerInfo.class);

        assertNotNull(myStatelessContainer);
        assertEquals("My Stateless Container", myStatelessContainer.id);
        assertEquals(defaultStatelessContainer.className, myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);

    }

    public void testConfigureService2() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        // We should be able to create one of these with a different name

        Container container = new Container("MyContainer", "STATELESS", "org.acme#CheddarContainer");
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(container, StatelessSessionContainerInfo.class);

        assertNotNull(myStatelessContainer);
        assertEquals("MyContainer", myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Yummy Cheese", myStatelessContainer.properties.getProperty("myProperty"));
    }

    public void testConfigureServiceOverriddenProperty() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        Container container = new Container("MyContainer", "STATELESS", "org.acme#CheddarContainer");
        container.getProperties().setProperty("myProperty", "Cheese is good");

        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(container, StatelessSessionContainerInfo.class);

        assertNotNull(myStatelessContainer);
        assertEquals("MyContainer", myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Cheese is good", myStatelessContainer.properties.getProperty("myProperty"));
    }

    public void testConfigureServiceAddedProperty() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        Container container = new Container("MyContainer", "STATELESS", "org.acme#CheddarContainer");
        container.getProperties().setProperty("anotherProperty", "Cheese is good");
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(container,  StatelessSessionContainerInfo.class);

        assertNotNull(myStatelessContainer);
        assertEquals("MyContainer", myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Yummy Cheese", myStatelessContainer.properties.getProperty("myProperty"));
        assertNotNull(myStatelessContainer.properties.getProperty("anotherProperty"));
        assertEquals("Cheese is good", myStatelessContainer.properties.getProperty("anotherProperty"));
    }

    public void testConfigureByType() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        Container container = new Container("MyContainer", "STATELESS", null);
        container.getProperties().setProperty("anotherProperty", "Cheese is good");
        ContainerInfo myStatelessContainer = factory.configureService(container,  ContainerInfo.class);

        assertNotNull(myStatelessContainer);
        assertEquals("org.apache.openejb.core.stateless.StatelessContainer", myStatelessContainer.className);
    }

    public void testConfigureServiceAddedPropertyViaURI() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        URI uri = new URI("new://Container?type=STATELESS&provider=org.acme%23CheddarContainer");

        Container container = (Container) factory.toConfigDeclaration("MyContainer", uri);

        container.getProperties().setProperty("anotherProperty", "Cheese is good");
        StatelessSessionContainerInfo myStatelessContainer = factory.configureService(container,  StatelessSessionContainerInfo.class);

        assertNotNull(myStatelessContainer);
        assertEquals("MyContainer", myStatelessContainer.id);
        assertEquals("org.acme.SuperContainer", myStatelessContainer.className);
        assertNotNull(myStatelessContainer.constructorArgs);
        assertNotNull(myStatelessContainer.properties);
        assertNotNull(myStatelessContainer.properties.getProperty("myProperty"));
        assertEquals("Yummy Cheese", myStatelessContainer.properties.getProperty("myProperty"));
        assertNotNull(myStatelessContainer.properties.getProperty("anotherProperty"));
        assertEquals("Cheese is good", myStatelessContainer.properties.getProperty("anotherProperty"));
    }

    public void testQueue() throws Exception {
        ConfigurationFactory factory = new ConfigurationFactory();

        ResourceInfo resourceInfo = factory.configureService(new Resource("myQueue", "Queue"), ResourceInfo.class);

        assertNotNull(resourceInfo);
        assertEquals("myQueue", resourceInfo.id);
        assertNotNull(resourceInfo.constructorArgs);
        assertNotNull(resourceInfo.properties);
        assertEquals("myQueue", resourceInfo.properties.getProperty("destination"));
    }


}


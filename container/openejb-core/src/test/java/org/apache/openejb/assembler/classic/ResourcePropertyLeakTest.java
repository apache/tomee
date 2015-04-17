/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.NamingException;
import java.util.Properties;


/**
 * This test ensures that additional properties are not leaked into the properties map when creating a resource
 * using class-name, without a provider or type. When using "SkipImpliedAttributes" only the properties the
 * user specified in their configuration should be available in the map.
 *
 * With "SkipImpliedAttributes" set to false (the default), ServiceId (the id of the resource itself) and transactionManager
 * (the Geronimo Transaction Manager) are the only additional properties that should be available for resources to consume.
 */
@RunWith(ApplicationComposer.class)
public class ResourcePropertyLeakTest {
    @Test
    public void testResourceProperties() throws NamingException {

        final MyResource r1 = (MyResource) SystemInstance.get()
                .getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb/Resource/r1");

        Assert.assertEquals(4, r1.properties.size());
        Assert.assertTrue(r1.properties.containsKey("prop1"));
        Assert.assertTrue(r1.properties.containsKey("prop2"));
        Assert.assertTrue(r1.properties.containsKey("ServiceId"));
        Assert.assertTrue(r1.properties.containsKey("transactionManager"));

        Assert.assertEquals("value1", r1.properties.get("prop1"));
        Assert.assertEquals("value2", r1.properties.get("prop2"));
        Assert.assertEquals("r1", r1.properties.get("ServiceId"));
        Assert.assertTrue(GeronimoTransactionManager.class.isInstance(r1.properties.get("transactionManager")));

        // Resource 2 should not contain implied attributes
        final MyResource r2 = (MyResource) SystemInstance.get()
                .getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb/Resource/r2");

        Assert.assertEquals(2, r2.properties.size());
        Assert.assertTrue(r2.properties.containsKey("prop1"));
        Assert.assertTrue(r2.properties.containsKey("prop2"));
        Assert.assertEquals("value1", r1.properties.get("prop1"));
        Assert.assertEquals("value2", r1.properties.get("prop2"));

    }

    @Module
    public AppModule application() {
        final EjbModule ejbModule = new EjbModule(new EjbJar());

        final AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), null);
        appModule.getEjbModules().add(ejbModule);

        return appModule;
    }

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
            .p("r1", "new://Resource?class-name=org.apache.openejb.assembler.classic.ResourcePropertyLeakTest$MyResource")
            .p("r1.prop1", "value1")
            .p("r1.prop2", "value2")
            .p("r2", "new://Resource?class-name=org.apache.openejb.assembler.classic.ResourcePropertyLeakTest$MyResource")
            .p("r2.SkipImplicitAttributes", "true")
            .p("r2.prop1", "value1")
            .p("r2.prop2", "value2")
            .build();
    }

    public static class MyResource {
        private Properties properties;

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(final Properties properties) {
            this.properties = properties;
        }
    }
}

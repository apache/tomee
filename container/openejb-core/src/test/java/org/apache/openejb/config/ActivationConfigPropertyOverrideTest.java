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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.activemq.ActivationContainerOverwriteBothConfigurationTest;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.core.builder.AppModuleBuilder;
import org.apache.openejb.core.builder.MdbBuilder;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
1  -D<deploymentId>.activation.<property>=<value>
2. -D<ejbName>.activation.<property>=<value>
3. -D<message-listener-interface>.activation.<property>=<value>
4. -Dmdb.activation.<property>=<value>
Order: 4 is overriden by 3 (and so on)
*/
@RunWith(ApplicationComposer.class)
public class ActivationConfigPropertyOverrideTest{

    @Module
    public MessageDrivenBean jar() {
        return new MessageDrivenBean(ActivationContainerOverwriteBothConfigurationTest.Listener.class);
    }


    /**
     * Test internal method used in ActivationConfigPropertyOverride
     */
    @Test
    public void testGetOverridesShouldTrimAwayPrefixesCorrectly() {
        final Properties properties = new Properties();
        properties.put("ENTERPRISEBEAN.mdb.activation.destinationType", "something");
        final Properties properties2 = ConfigurationFactory.getOverrides(properties, "mdb.activation", "EnterpriseBean");
        assertNotNull(properties2.getProperty("destinationType"));
    }

    /**
     * System property set should override activationConfigProperty
     *
     * @throws OpenEJBException
     */
    @Test
    public void testOverrideActivationConfigProperty() throws OpenEJBException {

        // set overrides for destinationType and check
        System.setProperty("ENTERPRISEBEAN.mdb.activation.destinationType", "testString");
        final MessageDrivenBean mdb = new MdbBuilder().anMdb().withActivationProperty("destinationType", "stringToBeOverriden").build();
        final ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        final AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "testString"));
        assertTrue(mdb.getActivationConfig().getActivationConfigProperty().size() == 1);
        System.clearProperty("ENTERPRISEBEAN.mdb.activation.destinationType");
    }

    /**
     * If activation property was not present initially, then add the specified
     * one.
     *
     * @throws OpenEJBException
     */
    @Test
    public void testAddActivationConfigPropertyIfNotAlreadyPresent() throws OpenEJBException {

        // set overrides
        System.setProperty("ENTERPRISEBEAN.mdb.activation.destinationType", "testString");

        // deploy with an mdb that has no "destinationType" activationConfigProp
        final MessageDrivenBean mdb = new MdbBuilder().anMdb().build();
        final AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        final ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "testString"));
        assertTrue(mdb.getActivationConfig().getActivationConfigProperty().size() == 1);

        System.clearProperty("ENTERPRISEBEAN.mdb.activation.destinationType");
    }

    private boolean containsActivationKeyValuePair(final MessageDrivenBean mdbBeingInspected, final String activationPropKey, final String activationPropValue) {

        for (final ActivationConfigProperty activationConfigProp : mdbBeingInspected.getActivationConfig().getActivationConfigProperty()) {
            if (activationConfigProp.getActivationConfigPropertyName().equals(activationPropKey)) {
                if (activationConfigProp.getActivationConfigPropertyValue().equals(activationPropValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void testNoOverrideSetShouldNotOverride() throws OpenEJBException {
        if (SystemInstance.get().getProperties().containsKey("ENTERPRISEBEAN.mdb.activation.destinationType")) {
            SystemInstance.get().getProperties().remove("ENTERPRISEBEAN.mdb.activation.destinationType");
        }
        System.clearProperty("ENTERPRISEBEAN.mdb.activation.destinationType");

        final MessageDrivenBean mdb = new MdbBuilder().anMdb().withActivationProperty("destinationType", "shouldNotBeOverriddenString").build();
        final AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        final ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "shouldNotBeOverriddenString"));
    }

    @Test
    public void testNotOverridden() throws Exception {
        SystemInstance.reset();
        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(Orange.class));
        ejbJar.addEnterpriseBean(new MessageDrivenBean(Yellow.class));
        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        assertEquals(2, ejbJarInfo.enterpriseBeans.size());
        final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
        final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

        assertEquals("7", orange.activationProperties.get("maxSessions"));
        assertEquals("4", orange.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
        assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

        assertEquals("5", yellow.activationProperties.get("maxSessions"));
        assertEquals("10", yellow.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("jakarta.jms.Topic", yellow.activationProperties.get("destinationType"));
        assertEquals("YELLOW.TOPIC", yellow.activationProperties.get("destination"));
    }

    @Test
    public void testMdbOverrideSystem() throws Exception {
        SystemInstance.reset();
        final Properties systProps = SystemInstance.get().getProperties();
        final Properties properties = new Properties();
        properties.setProperty("mdb.activation.maxSessions", "20");
        properties.setProperty("mdb.activation.maxMessagesPerSessions", "100");
        properties.setProperty("mdb.activation.destinationType", "jakarta.jms.Queue");
        properties.setProperty("mdb.activation.destination", "OVERRIDDEN.QUEUE");
        systProps.putAll(properties);

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean(Orange.class));
        ejbJar.addEnterpriseBean(new MessageDrivenBean(Yellow.class));
        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        assertEquals(2, ejbJarInfo.enterpriseBeans.size());
        final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
        final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

        assertEquals("20", orange.activationProperties.get("maxSessions"));
        assertEquals("100", orange.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
        assertEquals("OVERRIDDEN.QUEUE", orange.activationProperties.get("destination"));

        assertEquals("20", yellow.activationProperties.get("maxSessions"));
        assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("jakarta.jms.Queue", yellow.activationProperties.get("destinationType"));
        assertEquals("OVERRIDDEN.QUEUE", yellow.activationProperties.get("destination"));

        for (final String n : properties.stringPropertyNames()) {
            systProps.remove(n);
        }
    }

    @Test
    public void testMdbOverrideOpenejbJar() throws Exception {
        SystemInstance.reset();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        {
            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Orange.class));
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Yellow.class));

            final OpenejbJar openejbJar = new OpenejbJar();
            final Properties properties = openejbJar.getProperties();
            properties.setProperty("mdb.activation.maxSessions", "20");
            properties.setProperty("mdb.activation.maxMessagesPerSessions", "100");
            properties.setProperty("mdb.activation.destinationType", "jakarta.jms.Queue");
            properties.setProperty("mdb.activation.destination", "OVERRIDDEN.QUEUE");

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);

            final EjbJarInfo ejbJarInfo = config.configureApplication(ejbModule);

            assertEquals(2, ejbJarInfo.enterpriseBeans.size());
            final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
            final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

            assertEquals("20", orange.activationProperties.get("maxSessions"));
            assertEquals("100", orange.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("OVERRIDDEN.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("20", yellow.activationProperties.get("maxSessions"));
            assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Queue", yellow.activationProperties.get("destinationType"));
            assertEquals("OVERRIDDEN.QUEUE", yellow.activationProperties.get("destination"));
        }

        // Verify the openejb-jar level overrides do not affect other apps
        {
            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Orange.class));
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Yellow.class));
            final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

            assertEquals(2, ejbJarInfo.enterpriseBeans.size());
            final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
            final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

            assertEquals("7", orange.activationProperties.get("maxSessions"));
            assertEquals("4", orange.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("5", yellow.activationProperties.get("maxSessions"));
            assertEquals("10", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Topic", yellow.activationProperties.get("destinationType"));
            assertEquals("YELLOW.TOPIC", yellow.activationProperties.get("destination"));
        }

    }

    @Test
    public void testEjbNameOverrideSystem() throws Exception {
        SystemInstance.reset();
        final Properties properties = SystemInstance.get().getProperties();
        properties.setProperty("Orange.activation.maxSessions", "20");
        properties.setProperty("Orange.activation.maxMessagesPerSessions", "100");
        properties.setProperty("Orange.activation.destinationType", "jakarta.jms.Queue");
        properties.setProperty("Orange.activation.destination", "OVERRIDDEN.QUEUE");

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean("Yellow", Orange.class)); // just to make sure class name is not used
        ejbJar.addEnterpriseBean(new MessageDrivenBean("Orange", Yellow.class)); // just to make sure class name is not used
        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        assertEquals(2, ejbJarInfo.enterpriseBeans.size());
        final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
        final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

        assertEquals("7", orange.activationProperties.get("maxSessions"));
        assertEquals("4", orange.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
        assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

        assertEquals("20", yellow.activationProperties.get("maxSessions"));
        assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("jakarta.jms.Queue", yellow.activationProperties.get("destinationType"));
        assertEquals("OVERRIDDEN.QUEUE", yellow.activationProperties.get("destination"));
    }

    @Test
    public void testEjbNameOverrideOpenejbJar() throws Exception {
        SystemInstance.reset();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        {
            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Orange.class));
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Yellow.class));

            final OpenejbJar openejbJar = new OpenejbJar();
            final Properties properties = openejbJar.getProperties();
            properties.setProperty("mdb.activation.maxSessions", "20");
            properties.setProperty("mdb.activation.maxMessagesPerSessions", "100");
            properties.setProperty("mdb.activation.destinationType", "jakarta.jms.Queue");
            properties.setProperty("mdb.activation.destination", "OVERRIDDEN.QUEUE");

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);

            final EjbJarInfo ejbJarInfo = config.configureApplication(ejbModule);

            assertEquals(2, ejbJarInfo.enterpriseBeans.size());
            final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
            final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

            assertEquals("20", orange.activationProperties.get("maxSessions"));
            assertEquals("100", orange.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("OVERRIDDEN.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("20", yellow.activationProperties.get("maxSessions"));
            assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Queue", yellow.activationProperties.get("destinationType"));
            assertEquals("OVERRIDDEN.QUEUE", yellow.activationProperties.get("destination"));
        }

        // Verify the openejb-jar level overrides do not affect other apps
        {
            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Orange.class));
            ejbJar.addEnterpriseBean(new MessageDrivenBean(Yellow.class));
            final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

            assertEquals(2, ejbJarInfo.enterpriseBeans.size());
            final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
            final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

            assertEquals("7", orange.activationProperties.get("maxSessions"));
            assertEquals("4", orange.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("5", yellow.activationProperties.get("maxSessions"));
            assertEquals("10", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("jakarta.jms.Topic", yellow.activationProperties.get("destinationType"));
            assertEquals("YELLOW.TOPIC", yellow.activationProperties.get("destination"));
        }

    }

    @Test
    public void testOverrideFromContainerDefinedInAppModule() throws Exception {
        SystemInstance.reset();

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new MessageDrivenBean("Yellow", Orange.class));
        ejbJar.addEnterpriseBean(new MessageDrivenBean("Orange", Yellow.class));

        final AppModule appModule = new AppModule(new EjbModule(ejbJar));
        appModule.setModuleId("mymodule");

        final Container container = new Container();
        container.setId("mycontainer");
        container.setCtype("MESSAGE");
        container.getProperties().setProperty("activation.DeliveryActive", "false");
        appModule.getContainers().add(container);


        final AppInfo appInfo = config.configureApplication(appModule);
        assertEquals(1, appInfo.ejbJars.size());
        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);

        assertEquals(2, ejbJarInfo.enterpriseBeans.size());
        final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
        final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

        assertEquals("false", orange.activationProperties.get("DeliveryActive"));
        assertEquals("false", yellow.activationProperties.get("DeliveryActive"));
    }


    @MessageDriven(activationConfig = {
        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "7"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "4"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "ORANGE.QUEUE")
    })
    public static class Orange implements MessageListener {

        @Override
        public void onMessage(final Message message) {
        }
    }

    @MessageDriven(activationConfig = {
        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "5"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "10"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
        @jakarta.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "YELLOW.TOPIC")
    })
    public static class Yellow implements MessageListener {

        @Override
        public void onMessage(final Message message) {
        }
    }

}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.builder.AppModuleBuilder;
import org.apache.openejb.core.builder.MdbBuilder;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/*
1  -D<deploymentId>.activation.<property>=<value>
2. -D<ejbName>.activation.<property>=<value>
3. -D<message-listener-interface>.activation.<property>=<value>
4. -Dmdb.activation.<property>=<value>
Order: 4 is overriden by 3 (and so on)
*/
public class ActivationConfigPropertyOverrideTest extends TestCase {


    /**
     * Test internal method used in ActivationConfigPropertyOverride
     */
    public void testGetOverridesShouldTrimAwayPrefixesCorrectly() {
        Properties properties = new Properties();
        properties.put("ENTERPRISEBEAN.mdb.activation.destinationType", "something");
        Properties properties2 = ConfigurationFactory.getOverrides(properties, "mdb.activation", "EnterpriseBean");
        assertNotNull(properties2.getProperty("destinationType"));
    }
    /**
     * 
     * System property set should override activationConfigProperty
     * 
     * @throws OpenEJBException
     */
    public void testOverrideActivationConfigProperty() throws OpenEJBException {

        // set overrides for destinationType and check
        System.setProperty("ENTERPRISEBEAN.mdb.activation.destinationType", "testString");
        MessageDrivenBean mdb = new MdbBuilder().anMdb().withActivationProperty("destinationType", "stringToBeOverriden").build();
        ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "testString"));
        assertTrue(mdb.getActivationConfig().getActivationConfigProperty().size() == 1);
    }

    /**
     * If activation property was not present initially, then add the specified
     * one.
     * 
     * @throws OpenEJBException
     */

    public void testAddActivationConfigPropertyIfNotAlreadyPresent() throws OpenEJBException {

        // set overrides
        System.setProperty("ENTERPRISEBEAN.mdb.activation.destinationType", "testString");
        // deploy with an mdb that has no "destinationType" activationConfigProp
        MessageDrivenBean mdb = new MdbBuilder().anMdb().build();
        AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "testString"));
        assertTrue(mdb.getActivationConfig().getActivationConfigProperty().size() == 1);
    }

    private boolean containsActivationKeyValuePair(MessageDrivenBean mdbBeingInspected, String activationPropKey, String activationPropValue) {

        for (ActivationConfigProperty activationConfigProp : mdbBeingInspected.getActivationConfig().getActivationConfigProperty()) {
            if (activationConfigProp.getActivationConfigPropertyName().equals(activationPropKey)) {
                if (activationConfigProp.getActivationConfigPropertyValue().equals(activationPropValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void testNoOverrideSetShouldNotOverride() throws OpenEJBException {
        if (SystemInstance.get().getProperties().containsKey("ENTERPRISEBEAN.mdb.activation.destinationType")) {
            SystemInstance.get().getProperties().remove("ENTERPRISEBEAN.mdb.activation.destinationType");
        }
        System.clearProperty("ENTERPRISEBEAN.mdb.activation.destinationType");

        MessageDrivenBean mdb = new MdbBuilder().anMdb().withActivationProperty("destinationType", "shouldNotBeOverriddenString").build();
        AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "shouldNotBeOverriddenString"));
    }

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
        assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
        assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

        assertEquals("5", yellow.activationProperties.get("maxSessions"));
        assertEquals("10", yellow.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("javax.jms.Topic", yellow.activationProperties.get("destinationType"));
        assertEquals("YELLOW.TOPIC", yellow.activationProperties.get("destination"));
    }

    public void testMdbOverrideSystem() throws Exception {
        SystemInstance.reset();
        final Properties properties = SystemInstance.get().getProperties();
        properties.setProperty("mdb.activation.maxSessions", "20");
        properties.setProperty("mdb.activation.maxMessagesPerSessions", "100");
        properties.setProperty("mdb.activation.destinationType", "javax.jms.Queue");
        properties.setProperty("mdb.activation.destination", "OVERRIDDEN.QUEUE");

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
        assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
        assertEquals("OVERRIDDEN.QUEUE", orange.activationProperties.get("destination"));

        assertEquals("20", yellow.activationProperties.get("maxSessions"));
        assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("javax.jms.Queue", yellow.activationProperties.get("destinationType"));
        assertEquals("OVERRIDDEN.QUEUE", yellow.activationProperties.get("destination"));
    }

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
            properties.setProperty("mdb.activation.destinationType", "javax.jms.Queue");
            properties.setProperty("mdb.activation.destination", "OVERRIDDEN.QUEUE");

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);

            final EjbJarInfo ejbJarInfo = config.configureApplication(ejbModule);

            assertEquals(2, ejbJarInfo.enterpriseBeans.size());
            final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
            final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

            assertEquals("20", orange.activationProperties.get("maxSessions"));
            assertEquals("100", orange.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("OVERRIDDEN.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("20", yellow.activationProperties.get("maxSessions"));
            assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("javax.jms.Queue", yellow.activationProperties.get("destinationType"));
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
            assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("5", yellow.activationProperties.get("maxSessions"));
            assertEquals("10", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("javax.jms.Topic", yellow.activationProperties.get("destinationType"));
            assertEquals("YELLOW.TOPIC", yellow.activationProperties.get("destination"));
        }

    }

    public void testEjbNameOverrideSystem() throws Exception {
        SystemInstance.reset();
        final Properties properties = SystemInstance.get().getProperties();
        properties.setProperty("Orange.activation.maxSessions", "20");
        properties.setProperty("Orange.activation.maxMessagesPerSessions", "100");
        properties.setProperty("Orange.activation.destinationType", "javax.jms.Queue");
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
        assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
        assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

        assertEquals("20", yellow.activationProperties.get("maxSessions"));
        assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
        assertEquals("javax.jms.Queue", yellow.activationProperties.get("destinationType"));
        assertEquals("OVERRIDDEN.QUEUE", yellow.activationProperties.get("destination"));
    }

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
            properties.setProperty("mdb.activation.destinationType", "javax.jms.Queue");
            properties.setProperty("mdb.activation.destination", "OVERRIDDEN.QUEUE");

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);

            final EjbJarInfo ejbJarInfo = config.configureApplication(ejbModule);

            assertEquals(2, ejbJarInfo.enterpriseBeans.size());
            final MessageDrivenBeanInfo orange = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
            final MessageDrivenBeanInfo yellow = (MessageDrivenBeanInfo) ejbJarInfo.enterpriseBeans.get(1);

            assertEquals("20", orange.activationProperties.get("maxSessions"));
            assertEquals("100", orange.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("OVERRIDDEN.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("20", yellow.activationProperties.get("maxSessions"));
            assertEquals("100", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("javax.jms.Queue", yellow.activationProperties.get("destinationType"));
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
            assertEquals("javax.jms.Queue", orange.activationProperties.get("destinationType"));
            assertEquals("ORANGE.QUEUE", orange.activationProperties.get("destination"));

            assertEquals("5", yellow.activationProperties.get("maxSessions"));
            assertEquals("10", yellow.activationProperties.get("maxMessagesPerSessions"));
            assertEquals("javax.jms.Topic", yellow.activationProperties.get("destinationType"));
            assertEquals("YELLOW.TOPIC", yellow.activationProperties.get("destination"));
        }

    }


    @MessageDriven(activationConfig = {
            @javax.ejb.ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "7"),
            @javax.ejb.ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "4"),
            @javax.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @javax.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "ORANGE.QUEUE")
    })
    public static class Orange implements MessageListener {

        @Override
        public void onMessage(Message message) {
        }
    }

    @MessageDriven(activationConfig = {
            @javax.ejb.ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "5"),
            @javax.ejb.ActivationConfigProperty(propertyName = "maxMessagesPerSessions", propertyValue = "10"),
            @javax.ejb.ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
            @javax.ejb.ActivationConfigProperty(propertyName = "destination", propertyValue = "YELLOW.TOPIC")
    })
    public static class Yellow implements MessageListener {

        @Override
        public void onMessage(Message message) {
        }
    }

}

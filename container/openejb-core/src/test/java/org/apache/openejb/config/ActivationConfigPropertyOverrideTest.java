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

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.builder.AppModuleBuilder;
import org.apache.openejb.core.builder.MdbBuilder;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.MessageDrivenBean;
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

        System.clearProperty("ENTERPRISEBEAN.mdb.activation.destinationType");

        MessageDrivenBean mdb = new MdbBuilder().anMdb().withActivationProperty("destinationType", "shouldNotBeOverriddenString").build();
        AppModule appModule = new AppModuleBuilder().anAppModule().withAnMdb(mdb).build();
        ActivationConfigPropertyOverride activationPropertyOverride = new ActivationConfigPropertyOverride();
        activationPropertyOverride.deploy(appModule);

        assertTrue(containsActivationKeyValuePair(mdb, "destinationType", "shouldNotBeOverriddenString"));

    }

}

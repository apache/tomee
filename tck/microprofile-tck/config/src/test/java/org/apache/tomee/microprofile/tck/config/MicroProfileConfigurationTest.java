/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.tck.config;

import org.eclipse.microprofile.config.Config;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import static org.testng.Assert.assertEquals;

@Test
public class MicroProfileConfigurationTest extends Arquillian {
    @Deployment
    public static WebArchive deploy() {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "microprofile-configuration.jar")
                .addClass(ConfigurationBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .as(JavaArchive.class);

        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "microprofile-configuration.war")
                .addAsLibrary(testJar);

        return war;
    }

    @Inject
    private ConfigurationBean configurationBean;

    @Test
    public void testConfiguration() throws Exception {
        assertEquals("SUCCESS", configurationBean.getConfig());
    }

    @Inject
    private Config config;

    @Test
    public void testDynamicValueInPropertyConfigSource() {
        String configKey = "tck.config.test.systemproperty.dynamic.value";
        String configValue = "myDynamicValue;";

        System.setProperty(configKey, configValue);
        Assert.assertEquals(config.getValue(configKey, String.class), configValue);
    }
}

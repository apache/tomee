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
package org.apache.openejb.arquillian.tests.jaxws.config;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.openejb.server.cxf.EndpointConfigurator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.jws.WebService;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CustomEndpointConfiguratorTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "configurator.war")
                .addClasses(CustomEndpointConfiguratorTest.class, MyWebservice.class, CustomConfigurator.class)
                .addAsWebInfResource(new StringAsset(
                        "<openejb-jar>\n" +
                        "  <ejb-deployment ejb-name=\"MyWebservice\">\n" +
                        "    <properties>\n" +
                        "      openejb.endpoint.configurator = " + CustomConfigurator.class.getName() + "\n" +
                        "    </properties>\n" +
                        "  </ejb-deployment>\n" +
                        "</openejb-jar>\n"), "openejb-jar.xml");
    }

    @Test
    public void checkConfiguratorWasCalled() {
        assertTrue(CustomConfigurator.ok);
    }

    @LocalBean
    @Singleton
    @WebService
    public static class MyWebservice {
        // not needed for this test
    }

    public static class CustomConfigurator implements EndpointConfigurator {
        public static boolean ok = false;

        @Override
        public void configure(final Endpoint endpoint, final Properties inProps) {
            ok = true;
        }
    }
}

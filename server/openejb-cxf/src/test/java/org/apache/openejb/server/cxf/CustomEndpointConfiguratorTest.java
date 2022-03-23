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
package org.apache.openejb.server.cxf;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.jws.WebService;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class CustomEndpointConfiguratorTest {
    @Configuration
    public Properties configuration() {
        return new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        }};
    }

    @Module
    public EjbModule module() {
        final EjbModule module = new EjbModule(new EjbJar());
        module.setOpenejbJar(new OpenejbJar());

        final SingletonBean bean = new SingletonBean(MyWebservice.class);
        bean.setLocalBean(new Empty());

        final EjbDeployment deployment = new EjbDeployment(bean);
        deployment.getProperties().setProperty("openejb.endpoint.configurator", CustomConfigurator.class.getName());

        module.getOpenejbJar().addEjbDeployment(deployment);
        module.getEjbJar().addEnterpriseBean(bean);

        return module;
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

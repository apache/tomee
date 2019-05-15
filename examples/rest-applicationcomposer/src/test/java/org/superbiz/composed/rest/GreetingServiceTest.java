/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.composed.rest;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class GreetingServiceTest {

    @Configuration
    public Properties configuration() {
        return new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, Boolean.TRUE.toString());
        }};
    }

    @Module
    public EjbModule app() {
        final SingletonBean bean = (SingletonBean) new SingletonBean(GreetingService.class).localBean();
        bean.setRestService(true);

        // now create an ejbjar and an openejb-jar to hold the provider config

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        final OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment(ejbJar.getEnterpriseBeans()[0]));

        final Properties properties = openejbJar.getEjbDeployment().iterator().next().getProperties();
        properties.setProperty("cxf.jaxrs.providers", IllegalArgumentExceptionMapper.class.getName());

        // link all and return this module

        final EjbModule module = new EjbModule(ejbJar);
        module.setOpenejbJar(openejbJar);

        return module;
    }

    @Test
    public void checkProviderIsUsed() throws IOException {
        final String message = IO.slurp(new URL("http://localhost:4204/GreetingServiceTest/greeting/"));
        assertEquals("this exception is handled by an exception mapper", message);
    }
}

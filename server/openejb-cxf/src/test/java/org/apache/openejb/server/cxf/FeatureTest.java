/**
 *
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

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.server.cxf.fault.AuthenticatorServiceBean;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class FeatureTest {
    @Configuration
    public Properties config() {
        return new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
            setProperty(DeploymentLoader.OPENEJB_ALTDD_PREFIX, "feature");
        }};
    }

    @Module
    public EjbModule app() {
        final EjbJar jar = new EjbJar();
        jar.addEnterpriseBean(new SingletonBean(AuthenticatorServiceBean.class).localBean());

        final OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment(jar.getEnterpriseBeans()[0]));
        final Properties properties = openejbJar.getEjbDeployment().iterator().next().getProperties();
        properties.setProperty(CxfService.OPENEJB_JAXWS_CXF_FEATURES, MyFeature.class.getName());
        properties.setProperty("cxf.jaxws.features", "my-feature");
        properties.setProperty("cxf.jaxws.properties", "my-props");

        final EjbModule module = new EjbModule(jar);
        module.setOpenejbJar(openejbJar);

        final Resources resources = new Resources();

        final Service service = new Service("my-feature", null, null, null);
        service.setClassName(MyFeature.class.getName());
        resources.add(service);

        final Service myProps = new Service("my-props", null, null, null);
        myProps.setClassName(Properties.class.getName());
        myProps.getProperties().setProperty("faultStackTraceEnabled", "true");
        resources.add(myProps);

        module.initResources(resources);

        return module;
    }

    @Test
    public void run() {
        assertTrue(MyFeature.ok);
    }

    public static class MyFeature extends AbstractFeature {
        public static boolean ok = false;

        @Override
        public void initialize(Server server, Bus bus) {
            ok = true;
        }
    }
}

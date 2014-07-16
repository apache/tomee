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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.rs.beans.MySecondRestClass;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
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
        }};
    }

    @Module
    public EjbModule app() {
        final StatelessBean bean = (StatelessBean) new StatelessBean(MySecondRestClass.class).localBean();
        bean.setRestService(true);

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        final OpenejbJar openejbJar = new OpenejbJar();
        final PojoDeployment e = new PojoDeployment();
        openejbJar.getPojoDeployment().add(e);
        e.setClassName("jaxrs-application");
        final Properties properties = e.getProperties();
        properties.setProperty(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.FEATURES, "my-feature");

        final EjbModule module = new EjbModule(ejbJar);
        module.setOpenejbJar(openejbJar);

        final Resources resources = new Resources();

        final Service feature = new Service("my-feature", null);
        feature.setClassName(MyFeature.class.getName());
        resources.getService().add(feature);

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

/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.util.Properties;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.XmlRootElement;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class AdvancedProviderConfigTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("httpejbd.port", Integer.toString(port))
            .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
            .build();
    }

    @Module
    public static EjbModule service() throws Exception {
        final EjbModule module = new EjbModule(new EjbJar(), new OpenejbJar());
        final EnterpriseBean bean = new SingletonBean(AdvancedBean.class).localBean();
        module.getEjbJar().addEnterpriseBean(bean);

        final Resources resources = new Resources();

        final Service feature = new Service("xml", null);
        feature.setClassName(JAXBElementProvider.class.getName());
        feature.getProperties().put("eventHandler", "$handler");
        resources.getService().add(feature);

        final Service handler = new Service("handler", null);
        handler.setClassName(MyValidator.class.getName());
        resources.getService().add(handler);

        module.initResources(resources);

        final PojoDeployment e = new PojoDeployment();
        e.setClassName("jaxrs-application");
        e.getProperties().setProperty("cxf.jaxrs.providers", "xml");
        module.getOpenejbJar().getPojoDeployment().add(e);

        return module;
    }

    @Test
    public void check() throws Exception {
        assertEquals("true", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port + "/AdvancedProviderConfigTest")
                .path("advanced-provider-config")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));
    }

    @Singleton
    @Path("advanced-provider-config")
    public static class AdvancedBean {
        @Context
        private Providers providers;

        @GET
        public boolean providers() {
            final JAXBElementProvider<?> mbr = JAXBElementProvider.class.cast(providers.getMessageBodyReader(Pojo.class, Pojo.class, new Annotation[0], MediaType.APPLICATION_XML_TYPE));
            return MyValidator.class.isInstance(Reflections.get(mbr, "eventHandler"));
        }

    }

    @XmlRootElement
    public static class Pojo {

    }

    public static class MyValidator implements ValidationEventHandler {
        @Override
        public boolean handleEvent(final ValidationEvent event) {
            return false;
        }
    }
}

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
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.message.Message;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.PojoDeployment;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Properties;
import jakarta.ejb.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class CustomContextTest {

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

        final SingletonBean bean = new SingletonBean(CustomContextInjectedBean.class);
        bean.setLocalBean(new Empty());

        module.getEjbJar().addEnterpriseBean(bean);

        final PojoDeployment e = new PojoDeployment();
        e.setClassName("jaxrs-application");
        e.getProperties().setProperty("cxf.jaxrs.providers", CustomProvider.class.getName());
        module.getOpenejbJar().getPojoDeployment().add(e);

        return module;
    }

    @Test
    public void rest() throws IOException {
        final String response = ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port + "/CustomContextTest")
                .path("custom-context/check")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
        assertEquals("true", response);
    }

    @Singleton
    @Path("/custom-context")
    public static class CustomContextInjectedBean {
        @Context
        private IFoo foo;

        @GET
        @Path("/check")
        public boolean check() {
            return foo != null && foo.getMsg() != null;
        }
    }

    public static interface IFoo {
        Message getMsg();
    }

    public static class Foo implements IFoo {
        private final Message msg;

        public Foo(final Message message) {
            msg = message;
        }

        public Message getMsg() {
            return msg;
        }
    }

    @Provider
    public static class CustomProvider implements ContextProvider<IFoo> {
        @Override
        public IFoo createContext(final Message message) {
            return new Foo(message);
        }
    }
}

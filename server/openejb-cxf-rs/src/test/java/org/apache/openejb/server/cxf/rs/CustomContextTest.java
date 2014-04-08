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
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class CustomContextTest {
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

    @Configuration
    public static Properties configuration() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        return properties;
    }

    @Test
    public void rest() throws IOException {
        final String response = IO.slurp(new URL("http://127.0.0.1:4204/CustomContextTest/custom-context/check"));
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

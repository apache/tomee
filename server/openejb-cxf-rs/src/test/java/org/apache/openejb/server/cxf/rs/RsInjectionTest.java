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

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class RsInjectionTest {
    @Module
    public static SingletonBean service() throws Exception {
        final SingletonBean bean = new SingletonBean(RsInjection.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Configuration
    public static Properties configuration() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        return properties;
    }

    @Test
    public void rest() throws IOException {
        final String response = IO.slurp(new URL("http://127.0.0.1:4204/RsInjectionTest/injections/check"));
        assertEquals("true", response);
    }

    @Singleton
    @Path("/injections")
    public static class RsInjection {
        @Context
        private Providers providers;

        @Context
        private HttpServletResponse response;

        @GET
        @Path("/check")
        public boolean check() {
            return providers != null && response != null;
        }
    }
}

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
import org.apache.openejb.core.security.SecurityServiceImpl;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Component;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import javax.security.auth.login.LoginException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class SecurityContextIsUsableTest {
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
    @Classes(innerClassesAsBean = true, cdi = true)
    public static WebApp service() throws Exception {
        return new WebApp();
    }

    @Component
    public SecurityService securityService() {
        return new SecurityServiceImpl() {
            @Override
            public Principal getCallerPrincipal() {
                return new User("rest");
            }
        };
    }

    @Test
    public void rest() throws IOException {
        final String response = IO.slurp(new URL("http://127.0.0.1:" + port + "/openejb/SecurityContextIsUsableTest"));
        assertEquals("rest", response);
    }

    @Path("SecurityContextIsUsableTest")
    @Singleton
    public static class RsImpl {
        @Context
        private SecurityContext sc;

        @GET
        public String check() {
            return sc.getUserPrincipal().getName();
        }
    }
}

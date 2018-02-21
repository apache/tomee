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

import org.apache.cxf.jaxrs.model.ApplicationInfo;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.rs.event.ExtensionProviderRegistration;
import org.apache.openejb.server.cxf.rs.event.ServerCreated;
import org.apache.openejb.server.rest.InternalApplication;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.JaxrsProviders;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.Objects;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class MPJWTSecurityContextTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p("observer", "new://Service?class-name=" + Observer.class.getName()) // properly packaged and auto registered
                .build();
    }

    @Module
    @Classes({ Res.class, RestApplication.class})
    public WebApp war() {
        return new WebApp()
            .contextRoot("foo");
    }

    @Test
    public void check() throws IOException {
        assertEquals("true", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port)
                .path("foo/sc")
                .queryParam("role", "therole")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));

        assertEquals("false", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port)
                .path("foo/sc")
                .queryParam("role", "another")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));
    }

    @LoginConfig(authMethod = "MP-JWT")
    @ApplicationPath("/api")
    public static class RestApplication extends Application {
        // auto discovered
    }

    @Path("sc")
    public static class Res {
        @Context
        private SecurityContext sc;

        @GET
        public boolean f() {
            return sc.isUserInRole("therole");
        }
    }

    // this should go into a proper artifact for MP-JWT and have the property file so it gets discovered automatically
    public static class Observer {

        public void obs(@Observes final ServerCreated event) {
            System.out.println("Observer.obs");
            final ApplicationInfo appInfo = (ApplicationInfo) event.getServer().getEndpoint().get("javax.ws.rs.core.Application");
            final Application application = InternalApplication.class.isInstance(appInfo.getProvider())
                            ? InternalApplication.class.cast(appInfo.getProvider()).getOriginal()
                            : appInfo.getProvider();

            final LoginConfig annotation = application.getClass().getAnnotation(LoginConfig.class);
            if (annotation != null && "MP-JWT".equals(annotation.authMethod())) {
                // add the ContainerRequestFilter on the fly
                if (InternalApplication.class.isInstance(appInfo.getProvider())) {
                    InternalApplication.class.cast(appInfo.getProvider()).getClasses().add(MySecuCtx.class);
                }
            }
        }
    }

    // this should also be packaged into the same module and delegate to the security service
    @Provider
    public static class MySecuCtx implements ContainerRequestFilter {

        private final SecurityService securityService;

        public MySecuCtx() {
            securityService = SystemInstance.get().getComponent(SecurityService.class);
            Objects.requireNonNull(securityService, "A security context needs to be properly configured to enforce security in REST services");
        }

        @Override
        public void filter(final ContainerRequestContext containerRequestContext) throws IOException {

            containerRequestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return securityService.getCallerPrincipal();
                }

                @Override
                public boolean isUserInRole(final String s) {
                    return securityService.isCallerInRole(s);
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "MP-JWT";
                }
            });
        }
    }

}

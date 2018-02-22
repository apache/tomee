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

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.model.ApplicationInfo;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.rs.event.ServerCreated;
import org.apache.openejb.server.rest.InternalApplication;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testng.PropertiesBuilder;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-rs")
@RunWith(ApplicationComposer.class)
public class MPJWTSecurityContextTest {
    @RandomPort("http")
    private int port;

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
                .p("observer", "new://Service?class-name=" + Observer.class.getName()) // properly packaged and auto registered
                .build();
    }

    @Module
    @Classes(value = {Res.class, RestApplication.class, MPFilter.class, MPContext.class}, cdi = true)
    public WebApp war() {
        return new WebApp()
                .contextRoot("foo");
    }

    @Test
    public void check() {
        // todo: close the client (just to stay clean even in tests and avoid to potentially leak)
        assertEquals("true", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port)
                .path("foo/api/sc")
                .queryParam("role", "therole")
                .request()
                .accept(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class));

        assertEquals("false", ClientBuilder.newClient()
                .target("http://127.0.0.1:" + port)
                .path("foo/api/sc")
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
    @ApplicationScoped
    public static class Res {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public boolean f(@Context final SecurityContext ctx, @QueryParam("role") final String role) {
            return ctx.isUserInRole(role);
        }
    }

    // this should go into a proper artifact for MP-JWT and have the property file so it gets discovered automatically
    public static class Observer {

        public void obs(@Observes final ServerCreated event) {
            System.out.println("Observer.obs");
            final Server server = event.getServer();
            final Bus bus = (Bus) server.getEndpoint().get("org.apache.cxf.Bus");
            final ApplicationInfo appInfo = (ApplicationInfo) server.getEndpoint().get("javax.ws.rs.core.Application");
            final Application application = InternalApplication.class.isInstance(appInfo.getProvider())
                    ? InternalApplication.class.cast(appInfo.getProvider()).getOriginal()
                    : appInfo.getProvider();

            final LoginConfig annotation = application.getClass().getAnnotation(LoginConfig.class);
            if (annotation != null && "MP-JWT".equals(annotation.authMethod())) {
                // add the ContainerRequestFilter on the fly
            }
        }
    }

    // todo: industrialize that but idea is to fill that during startup to let it be usable at runtime
    // note: the bean must be added through an extension in a real impl
    @ApplicationScoped
    public static class MPContext {
        // todo: login config model, not the raw annot
        private Map<String, LoginConfig> configs = new HashMap<>();

        @PostConstruct
        private void init() {
            // todo: drop and replace by actual init
            configs.put("/api", new LoginConfig() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return LoginConfig.class;
                }

                @Override
                public String authMethod() {
                    return "MP-JWT";
                }

                @Override
                public String realmName() {
                    return "";
                }
            });
        }

        public Map<String, LoginConfig> getConfigs() {
            return configs;
        }
    }

    @WebFilter(asyncSupported = true, urlPatterns = "/*") // addbefore from an initializer
    public static class MPFilter implements Filter {
        @Inject
        private MPContext context;

        @Override
        public void init(final FilterConfig filterConfig) throws ServletException {
            // no-op
        }

        @Override
        public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
                throws IOException, ServletException {
            final HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(request);
            final String uri = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());

            // todo: better handling of conflicting app paths?
            final Optional<Map.Entry<String, LoginConfig>> first = context.getConfigs()
                    .entrySet()
                    .stream()
                    .filter(new Predicate<Map.Entry<String, LoginConfig>>() {
                        @Override
                        public boolean test(final Map.Entry<String, LoginConfig> e) {
                            return uri.startsWith(e.getKey());
                        }
                    })
                    .findFirst();

            if (first.isPresent()) {
                chain.doFilter(new HttpServletRequestWrapper(httpServletRequest) {
                    private final MPPcp pcp = new MPPcp();

                    @Override
                    public Principal getUserPrincipal() {
                        return pcp;
                    }

                    @Override
                    public boolean isUserInRole(final String role) {
                        return pcp.getGroups().contains(role);
                    }
                }, response);

            } else {
                chain.doFilter(request, response);

            }
        }

        @Override
        public void destroy() {
            // no-op
        }
    }

    // todo
    public static class MPPcp implements JsonWebToken {

        @Override
        public String getName() {
            return "mp";
        }

        @Override
        public Set<String> getClaimNames() {
            return Collections.singleton("test");
        }

        @Override
        public <T> T getClaim(String claimName) {
            return (T) "foo";
        }

        @Override
        public Set<String> getGroups() {
            return Collections.singleton("therole");
        }
    }
}
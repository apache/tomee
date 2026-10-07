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
package org.apache.tomee.microprofile.jwt.jaxrs;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MPJWTSecurityAnnotationsInterceptorsFeatureTest {

    public static class BaseResource {
        public String get() {
            return "ok";
        }

        @RolesAllowed("override")
        public String overridden() {
            return "ok";
        }

        @PermitAll
        public String open() {
            return "ok";
        }

        @DenyAll
        public String closed() {
            return "ok";
        }

        public String unprotected() {
            return "ok";
        }
    }

    @RolesAllowed("admin")
    public static class AdminResource extends BaseResource {
    }

    @RolesAllowed("user")
    public static class UserResource extends BaseResource {
    }

    @RolesAllowed({})
    public static class NobodyResource extends BaseResource {
    }

    @PermitAll
    public static class PublicResource extends BaseResource {
    }

    @DenyAll
    public static class LockedResource extends BaseResource {
    }

    public static class NoAnnotationResource extends BaseResource {
    }

    public static class EmptyMethodRolesResource {
        @RolesAllowed({})
        public String get() {
            return "ok";
        }
    }

    public static class ConflictingResource {
        @RolesAllowed("admin")
        @PermitAll
        public String get() {
            return "ok";
        }
    }

    @Test
    public void inheritedMethodKeepsRolesPerResourceClass() throws Exception {
        final MPJWTSecurityAnnotationsInterceptorsFeature feature = new MPJWTSecurityAnnotationsInterceptorsFeature();
        final ContainerRequestFilter admin = configure(feature, AdminResource.class, "get");
        final ContainerRequestFilter user = configure(feature, UserResource.class, "get");

        assertForbidden(admin, "user");
        assertAllowed(admin, "admin");
        assertForbidden(user, "admin");
        assertAllowed(user, "user");
    }

    @Test
    public void emptyRolesAllowedDeniesEveryone() throws Exception {
        final MPJWTSecurityAnnotationsInterceptorsFeature feature = new MPJWTSecurityAnnotationsInterceptorsFeature();

        final ContainerRequestFilter classLevel = configure(feature, NobodyResource.class, "get");
        assertForbidden(classLevel);
        assertForbidden(classLevel, "admin", "user");

        final ContainerRequestFilter methodLevel = configure(feature, EmptyMethodRolesResource.class, "get");
        assertForbidden(methodLevel);
        assertForbidden(methodLevel, "admin", "user");
    }

    @Test
    public void methodLevelOverridesClassLevel() throws Exception {
        final MPJWTSecurityAnnotationsInterceptorsFeature feature = new MPJWTSecurityAnnotationsInterceptorsFeature();

        final ContainerRequestFilter overridden = configure(feature, AdminResource.class, "overridden");
        assertForbidden(overridden, "admin");
        assertAllowed(overridden, "override");

        assertAllowed(configure(feature, AdminResource.class, "open"));
        assertForbidden(configure(feature, AdminResource.class, "closed"), "admin");

        assertAllowed(configure(feature, LockedResource.class, "open"));
        assertForbidden(configure(feature, PublicResource.class, "closed"));
    }

    @Test
    public void permitAllAndDenyAll() throws Exception {
        final MPJWTSecurityAnnotationsInterceptorsFeature feature = new MPJWTSecurityAnnotationsInterceptorsFeature();

        assertAllowed(configure(feature, PublicResource.class, "get"));
        assertForbidden(configure(feature, LockedResource.class, "get"), "admin");
        assertAllowed(configure(feature, NoAnnotationResource.class, "open"));
        assertForbidden(configure(feature, NoAnnotationResource.class, "closed"), "admin");
    }

    @Test
    public void unprotectedResourceHasNoInterceptor() throws Exception {
        final MPJWTSecurityAnnotationsInterceptorsFeature feature = new MPJWTSecurityAnnotationsInterceptorsFeature();
        assertNull(configure(feature, NoAnnotationResource.class, "unprotected"));
    }

    @Test(expected = IllegalStateException.class)
    public void moreThanOneSecurityAnnotationIsRejected() throws Exception {
        configure(new MPJWTSecurityAnnotationsInterceptorsFeature(), ConflictingResource.class, "get");
    }

    private static ContainerRequestFilter configure(final MPJWTSecurityAnnotationsInterceptorsFeature feature,
                                                    final Class<?> resourceClass, final String methodName) throws Exception {
        final Method method = resourceClass.getMethod(methodName);
        final ResourceInfo resourceInfo = new ResourceInfo() {
            @Override
            public Method getResourceMethod() {
                return method;
            }

            @Override
            public Class<?> getResourceClass() {
                return resourceClass;
            }
        };

        final AtomicReference<ContainerRequestFilter> registered = new AtomicReference<>();
        final FeatureContext context = proxy(FeatureContext.class, (p, m, args) -> {
            if ("register".equals(m.getName()) && args != null && args[0] instanceof ContainerRequestFilter) {
                registered.set((ContainerRequestFilter) args[0]);
            }
            return p;
        });

        feature.configure(resourceInfo, context);
        return registered.get();
    }

    private static void assertAllowed(final ContainerRequestFilter filter, final String... roles) throws Exception {
        assertNotNull(filter);
        assertNull(filter(filter, roles));
    }

    private static void assertForbidden(final ContainerRequestFilter filter, final String... roles) throws Exception {
        assertNotNull(filter);
        final Response response = filter(filter, roles);
        assertNotNull(response);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, response.getStatus());
    }

    private static Response filter(final ContainerRequestFilter filter, final String... roles) throws Exception {
        final Set<String> userRoles = new HashSet<>(Arrays.asList(roles));
        final SecurityContext securityContext = proxy(SecurityContext.class, (p, m, args) -> {
            if ("isUserInRole".equals(m.getName())) {
                return userRoles.contains((String) args[0]);
            }
            return null;
        });

        final AtomicReference<Response> aborted = new AtomicReference<>();
        final ContainerRequestContext requestContext = proxy(ContainerRequestContext.class, (p, m, args) -> {
            if ("getSecurityContext".equals(m.getName())) {
                return securityContext;
            }
            if ("abortWith".equals(m.getName())) {
                aborted.set((Response) args[0]);
            }
            return null;
        });

        filter.filter(requestContext);
        return aborted.get();
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(MPJWTSecurityAnnotationsInterceptorsFeatureTest.class.getClassLoader(),
                new Class<?>[]{type}, handler);
    }
}

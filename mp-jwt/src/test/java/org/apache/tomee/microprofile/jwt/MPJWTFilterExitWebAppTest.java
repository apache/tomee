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
package org.apache.tomee.microprofile.jwt;

import jakarta.enterprise.inject.Instance;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.core.StandardServer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.tomee.catalina.TomcatSecurityService;
import org.apache.tomee.loader.TomcatHelper;
import org.apache.tomee.microprofile.jwt.config.JWTAuthConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MPJWTFilterExitWebAppTest {

    private RecordingSecurityService securityService;

    @Before
    public void setUp() {
        SystemInstance.reset();
        TomcatHelper.setServer(new StandardServer());
        securityService = new RecordingSecurityService();
        SystemInstance.get().setComponent(SecurityService.class, securityService);
    }

    @After
    public void tearDown() {
        securityService.clearRunAsStack();
        SystemInstance.reset();
    }

    @Test
    public void exitWebAppRunsWhenChainThrows() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final HttpServletRequest request = request(attributes);
        final IllegalStateException failure = new IllegalStateException("boom");

        final FilterChain chain = (req, res) -> {
            enterWebApp(req);
            throw failure;
        };

        try {
            filter().doFilter(request, response(), chain);
            fail("the chain exception must be propagated");
        } catch (final IllegalStateException e) {
            assertSame(failure, e);
        }

        assertExited(attributes);
    }

    @Test
    public void exitWebAppRunsWhenChainThrowsCheckedException() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final HttpServletRequest request = request(attributes);

        final FilterChain chain = (req, res) -> {
            enterWebApp(req);
            throw new ServletException("boom");
        };

        try {
            filter().doFilter(request, response(), chain);
            fail("the chain exception must be propagated");
        } catch (final ServletException e) {
            assertEquals("boom", e.getMessage());
        }

        assertExited(attributes);
    }

    @Test
    public void exitWebAppRunsOnSuccess() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final HttpServletRequest request = request(attributes);

        filter().doFilter(request, response(), (req, res) -> enterWebApp(req));

        assertExited(attributes);
    }

    private void enterWebApp(final jakarta.servlet.ServletRequest req) {
        // simulates what ValidateJSonWebToken does once the token has been validated
        final Object state = securityService.enterWebApp(null, null, "admin");
        req.setAttribute(MPJWTFilter.PRE_LOGIN_STATE, state);
        assertEquals(1, securityService.runAsStackSize());
    }

    private void assertExited(final Map<String, Object> attributes) {
        assertEquals(1, securityService.exited.size());
        assertEquals(0, securityService.runAsStackSize());
        assertNull(attributes.get(MPJWTFilter.PRE_LOGIN_STATE));
    }

    private static MPJWTFilter filter() throws Exception {
        final JWTAuthConfiguration configuration = new JWTAuthConfiguration(LinkedHashMap::new, "https://server.example.com",
                false, new String[0], LinkedHashMap::new, "Authorization", null, null, null, null, 0);

        final Instance<?> instance = (Instance<?>) Proxy.newProxyInstance(
                MPJWTFilterExitWebAppTest.class.getClassLoader(), new Class<?>[]{Instance.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isUnsatisfied" -> false;
                    case "get" -> configuration;
                    default -> throw new UnsupportedOperationException(method.getName());
                });

        final MPJWTFilter filter = new MPJWTFilter();
        final Field field = MPJWTFilter.class.getDeclaredField("authContextInfo");
        field.setAccessible(true);
        field.set(filter, instance);
        return filter;
    }

    private static HttpServletRequest request(final Map<String, Object> attributes) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                MPJWTFilterExitWebAppTest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> attributes.get((String) args[0]);
                    case "setAttribute" -> attributes.put((String) args[0], args[1]);
                    case "removeAttribute" -> attributes.remove((String) args[0]);
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static HttpServletResponse response() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                MPJWTFilterExitWebAppTest.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static class RecordingSecurityService extends TomcatSecurityService {

        private final List<Object> exited = new ArrayList<>();

        @Override
        public void exitWebApp(final Object state) {
            exited.add(state);
            super.exitWebApp(state);
        }

        int runAsStackSize() {
            return RUN_AS_STACK.get().size();
        }

        void clearRunAsStack() {
            RUN_AS_STACK.remove();
        }
    }
}

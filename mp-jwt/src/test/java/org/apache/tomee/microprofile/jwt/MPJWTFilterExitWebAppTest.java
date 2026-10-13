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
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MPJWTFilterExitWebAppTest {

    private RecordingSecurityService securityService;
    private ExecutorService asyncThread;

    @Before
    public void setUp() {
        SystemInstance.reset();
        TomcatHelper.setServer(new StandardServer());
        securityService = new RecordingSecurityService();
        SystemInstance.get().setComponent(SecurityService.class, securityService);
        asyncThread = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() {
        asyncThread.shutdownNow();
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

    @Test
    public void asyncRequestValidatedOnFilterThreadIsRestoredOnce() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final FakeAsyncContext asyncContext = new FakeAsyncContext();
        final HttpServletRequest request = request(attributes, asyncContext);

        filter().doFilter(request, response(), (req, res) -> enterWebApp(req));

        assertExited(attributes);
        assertEquals(1, asyncContext.listeners.size());

        asyncContext.fireOnComplete();
        assertEquals(1, securityService.exited.size());
    }

    @Test
    public void asyncRequestValidatedLaterIsRestoredOnComplete() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final FakeAsyncContext asyncContext = new FakeAsyncContext();
        final HttpServletRequest request = request(attributes, asyncContext);

        filter().doFilter(request, response(), (req, res) -> {
        });

        assertTrue(securityService.exited.isEmpty());
        assertEquals(1, asyncContext.listeners.size());

        // the token is validated lazily on the thread handling the async request
        assertEquals(Integer.valueOf(0), onAsyncThread(() -> {
            enterWebApp(request);
            asyncContext.fireOnComplete();
            return securityService.runAsStackSize();
        }));

        assertEquals(1, securityService.exited.size());
        assertNull(attributes.get(MPJWTFilter.PRE_LOGIN_STATE));
    }

    @Test
    public void asyncRequestValidatedLaterIsRestoredOnErrorAndTimeout() throws Exception {
        for (final String event : new String[]{"error", "timeout"}) {
            securityService.exited.clear();

            final Map<String, Object> attributes = new HashMap<>();
            final FakeAsyncContext asyncContext = new FakeAsyncContext();
            final HttpServletRequest request = request(attributes, asyncContext);

            filter().doFilter(request, response(), (req, res) -> {
            });

            assertEquals(Integer.valueOf(0), onAsyncThread(() -> {
                enterWebApp(request);
                if ("error".equals(event)) {
                    asyncContext.fireOnError();
                } else {
                    asyncContext.fireOnTimeout();
                }
                return securityService.runAsStackSize();
            }));

            assertEquals(event, 1, securityService.exited.size());
            assertNull(event, attributes.get(MPJWTFilter.PRE_LOGIN_STATE));
        }
    }

    @Test
    public void stateIsNotRestoredOnAnotherThread() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final FakeAsyncContext asyncContext = new FakeAsyncContext();
        final HttpServletRequest request = request(attributes, asyncContext);

        // validated on another thread while the filter is still running
        filter().doFilter(request, response(), (req, res) -> {
            try {
                onAsyncThread(() -> {
                    enterWebApp(req);
                    return null;
                });
            } catch (final Exception e) {
                throw new ServletException(e);
            }
        });

        assertTrue(securityService.exited.isEmpty());
        assertNotNull(attributes.get(MPJWTFilter.PRE_LOGIN_STATE));

        // completion notified on a thread which did not push the state
        asyncContext.fireOnComplete();
        assertTrue(securityService.exited.isEmpty());
        assertEquals(0, securityService.runAsStackSize());

        assertEquals(Integer.valueOf(0), onAsyncThread(() -> {
            asyncContext.fireOnComplete();
            return securityService.runAsStackSize();
        }));
        assertEquals(1, securityService.exited.size());
        assertNull(attributes.get(MPJWTFilter.PRE_LOGIN_STATE));
    }

    @Test
    public void listenerIsKeptWhenAsyncIsStartedAgain() throws Exception {
        final Map<String, Object> attributes = new HashMap<>();
        final FakeAsyncContext asyncContext = new FakeAsyncContext();
        final HttpServletRequest request = request(attributes, asyncContext);

        filter().doFilter(request, response(), (req, res) -> {
        });

        asyncContext.fireOnStartAsync();
        assertEquals(1, asyncContext.listeners.size());

        assertEquals(Integer.valueOf(0), onAsyncThread(() -> {
            enterWebApp(request);
            asyncContext.fireOnComplete();
            return securityService.runAsStackSize();
        }));
        assertEquals(1, securityService.exited.size());
    }

    private <T> T onAsyncThread(final Callable<T> task) throws Exception {
        return asyncThread.submit(task).get();
    }

    private void enterWebApp(final ServletRequest req) {
        // simulates what ValidateJSonWebToken does once the token has been validated
        final Object state = securityService.enterWebApp(null, null, "admin");
        req.setAttribute(MPJWTFilter.PRE_LOGIN_STATE, new MPJWTFilter.PreLoginState(securityService, state));
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
        return request(attributes, null);
    }

    private static HttpServletRequest request(final Map<String, Object> attributes, final AsyncContext asyncContext) {
        final Map<String, Object> synchronizedAttributes = Collections.synchronizedMap(attributes);
        return (HttpServletRequest) Proxy.newProxyInstance(
                MPJWTFilterExitWebAppTest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> synchronizedAttributes.get((String) args[0]);
                    case "setAttribute" -> synchronizedAttributes.put((String) args[0], args[1]);
                    case "removeAttribute" -> synchronizedAttributes.remove((String) args[0]);
                    case "isAsyncStarted" -> asyncContext != null;
                    case "getAsyncContext" -> {
                        if (asyncContext == null) {
                            throw new IllegalStateException("not async");
                        }
                        yield asyncContext;
                    }
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

    private static class FakeAsyncContext implements AsyncContext {

        private final List<AsyncListener> listeners = new ArrayList<>();

        void fireOnComplete() throws Exception {
            for (final AsyncListener listener : new ArrayList<>(listeners)) {
                listener.onComplete(new AsyncEvent(this));
            }
        }

        void fireOnError() throws Exception {
            for (final AsyncListener listener : new ArrayList<>(listeners)) {
                listener.onError(new AsyncEvent(this));
            }
        }

        void fireOnTimeout() throws Exception {
            for (final AsyncListener listener : new ArrayList<>(listeners)) {
                listener.onTimeout(new AsyncEvent(this));
            }
        }

        void fireOnStartAsync() throws Exception {
            // the container drops the listeners before notifying them
            final List<AsyncListener> current = new ArrayList<>(listeners);
            listeners.clear();
            for (final AsyncListener listener : current) {
                listener.onStartAsync(new AsyncEvent(this));
            }
        }

        @Override
        public void addListener(final AsyncListener listener) {
            listeners.add(listener);
        }

        @Override
        public void addListener(final AsyncListener listener, final ServletRequest request,
                                final ServletResponse response) {
            listeners.add(listener);
        }

        @Override
        public ServletRequest getRequest() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletResponse getResponse() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasOriginalRequestAndResponse() {
            return true;
        }

        @Override
        public void dispatch() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void dispatch(final String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void dispatch(final ServletContext context, final String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void complete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void start(final Runnable run) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends AsyncListener> T createListener(final Class<T> clazz) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTimeout(final long timeout) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTimeout() {
            return 0;
        }
    }

    private static class RecordingSecurityService extends TomcatSecurityService {

        private final List<Object> exited = new CopyOnWriteArrayList<>();

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

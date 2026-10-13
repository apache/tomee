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
package org.apache.openejb.server.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.continuations.SuspendedInvocationException;
import org.apache.cxf.interceptor.AbstractAttributedInterceptorProvider;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistryImpl;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.cxf.transport.HttpDestination;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.spi.SecurityService;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.UsernameToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CxfWsContainerSecurityStateTest {
    private TestSecurityService securityService;
    private Bus bus;

    @Before
    public void setUp() {
        securityService = new TestSecurityService();
        SystemInstance.get().setComponent(SecurityService.class, securityService);
        bus = new ExtensionManagerBus();
    }

    @After
    public void tearDown() {
        securityService.disassociate();
        securityService.destroyResource();
        bus.shutdown(true);
        SystemInstance.reset();
    }

    @Test
    public void callerIsClearedAfterRequest() throws Exception {
        final AtomicReference<Principal> caller = new AtomicReference<>();
        final HttpRequest request = request();

        container(new Invocation() {
            @Override
            public void invoke(final HttpServletRequest req) throws Exception {
                login(req);
                caller.set(securityService.getCallerPrincipal());
            }
        }).onMessage(request, null);

        assertNotNull("caller should be associated during the invocation", caller.get());
        assertEquals("jonathan", caller.get().getName());
        assertNull(securityService.currentState());
        assertNull(request.getAttribute(OpenEJBLoginValidator.LOGINS));
        assertEquals(1, securityService.loggedOut.size());
    }

    @Test
    public void callerIsClearedWhenInvocationFails() throws Exception {
        try {
            container(new Invocation() {
                @Override
                public void invoke(final HttpServletRequest req) throws Exception {
                    login(req);
                    throw new IOException("broken");
                }
            }).onMessage(request(), null);
            fail();
        } catch (final IOException expected) {
            // ok
        }

        assertNull(securityService.currentState());
        assertEquals(1, securityService.loggedOut.size());
    }

    @Test
    public void previousStateIsRestored() throws Exception {
        final UUID outer = securityService.login("outer", "outer");
        securityService.associate(outer);
        final Object outerState = securityService.currentState();

        container(new Invocation() {
            @Override
            public void invoke(final HttpServletRequest req) throws Exception {
                login(req);
                assertEquals("jonathan", securityService.getCallerPrincipal().getName());
            }
        }).onMessage(request(), null);

        assertSame(outerState, securityService.currentState());
        assertEquals(1, securityService.loggedOut.size());
    }

    @Test
    public void everyTokenOfTheRequestIsLoggedOut() throws Exception {
        final HttpRequest request = request();

        container(new Invocation() {
            @Override
            public void invoke(final HttpServletRequest req) throws Exception {
                final Message message = message(req);
                login(message);
                login(message);
                login(message);
            }
        }).onMessage(request, null);

        assertNull(securityService.currentState());
        assertEquals(3, securityService.loggedIn.size());
        assertEquals(securityService.loggedIn, securityService.loggedOut);
        assertNull(request.getAttribute(OpenEJBLoginValidator.LOGINS));
    }

    @Test
    public void chainReleasesItsLoginsWithoutContainer() {
        final AtomicReference<Principal> caller = new AtomicReference<>();
        final PhaseInterceptorChain chain = inChain(
                new Step(Phase.PRE_PROTOCOL, m -> {
                    login(m);
                    login(m);
                }),
                new Step(Phase.INVOKE, m -> caller.set(securityService.getCallerPrincipal())));

        chain.doIntercept(message(null, chain));

        assertEquals("jonathan", caller.get().getName());
        assertNull(securityService.currentState());
        assertEquals(2, securityService.loggedOut.size());
        assertEquals(securityService.loggedIn, securityService.loggedOut);
    }

    @Test
    public void resumedChainReleasesLoginsOnItsOwnThread() throws Exception {
        final AtomicReference<Principal> caller = new AtomicReference<>();
        final AtomicReference<Object> stateAfterChain = new AtomicReference<>();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final boolean[] suspended = {false};
        final PhaseInterceptorChain chain = inChain(
                new Step(Phase.RECEIVE, m -> {
                    if (!suspended[0]) {
                        suspended[0] = true;
                        m.getInterceptorChain().suspend();
                    }
                }),
                new Step(Phase.PRE_PROTOCOL, m -> login(m)),
                new Step(Phase.INVOKE, m -> caller.set(securityService.getCallerPrincipal())));

        try {
            chain.doIntercept(message(null, chain));
            fail();
        } catch (final SuspendedInvocationException expected) {
            // the destination keeps the message for the continuation
        }
        assertEquals(InterceptorChain.State.PAUSED, chain.getState());
        assertNull(caller.get());

        // the continuation resumes the chain on another thread, outside of CxfWsContainer.onMessage
        final Thread resumer = new Thread(() -> {
            try {
                chain.resume();
                stateAfterChain.set(securityService.currentState());
            } catch (final Throwable t) {
                error.set(t);
            }
        });
        resumer.start();
        resumer.join(TimeUnit.MINUTES.toMillis(1));

        assertNull(error.get());
        assertEquals("jonathan", caller.get().getName());
        assertNull("resuming thread must not keep the caller", stateAfterChain.get());
        assertEquals(1, securityService.loggedOut.size());
        assertEquals(securityService.loggedIn, securityService.loggedOut);
    }

    @Test
    public void faultChainReleasesLogins() {
        final PhaseInterceptorChain outFault = new PhaseInterceptorChain(new PhaseManagerImpl().getOutPhases());
        outFault.add(WSSLoginCleanupInterceptor.OUT_FAULT);

        final AtomicReference<Object> stateAfterFault = new AtomicReference<>();
        final PhaseInterceptorChain chain = inChain(
                new Step(Phase.PRE_PROTOCOL, m -> login(m)),
                new Step(Phase.INVOKE, m -> {
                    throw new Fault(new IllegalStateException("broken"));
                }));
        chain.setFaultObserver(m -> {
            final Message fault = new MessageImpl();
            fault.setExchange(m.getExchange());
            outFault.doIntercept(fault);
            stateAfterFault.set(securityService.currentState());
        });

        chain.doIntercept(message(null, chain));

        assertNull(stateAfterFault.get());
        assertEquals(1, securityService.loggedOut.size());
        assertEquals(securityService.loggedIn, securityService.loggedOut);
    }

    @Test
    public void securedEndpointGetsFaultCleanup() {
        final AbstractAttributedInterceptorProvider endpoint = new AbstractAttributedInterceptorProvider() {
        };
        final Map<String, Object> in = new HashMap<>();
        in.put("action", "UsernameToken");
        ConfigureCxfSecurity.setupWSS4JChain(endpoint, in, null);

        assertTrue(endpoint.getOutFaultInterceptors().contains(WSSLoginCleanupInterceptor.OUT_FAULT));
    }

    private PhaseInterceptorChain inChain(final Step... steps) {
        final PhaseInterceptorChain chain = new PhaseInterceptorChain(new PhaseManagerImpl().getInPhases());
        for (final Step step : steps) {
            chain.add(step);
        }
        return chain;
    }

    private static Message message(final HttpServletRequest req) {
        return message(req, null);
    }

    private static Message message(final HttpServletRequest req, final InterceptorChain chain) {
        final Message message = new MessageImpl();
        message.setInterceptorChain(chain);
        final Exchange exchange = new ExchangeImpl();
        exchange.setInMessage(message);
        if (req != null) {
            message.put(AbstractHTTPDestination.HTTP_REQUEST, req);
        }
        return message;
    }

    private void login(final HttpServletRequest req) throws Exception {
        login(message(req));
    }

    private void login(final Message message) {
        final Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        final UsernameToken token = new UsernameToken(false, doc, WSConstants.PASSWORD_DIGEST);
        token.setName("jonathan");
        token.addNonce(doc);
        token.addCreated(false, doc);
        token.setPassword("secret");

        final RequestData data = new RequestData();
        data.setMsgContext(message);
        data.setCallbackHandler(callbacks -> {
            for (final Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback pwd && "jonathan".equals(pwd.getIdentifier())) {
                    pwd.setPassword("secret");
                }
            }
        });

        try {
            new OpenEJBLoginValidator().verifyDigestPassword(token, data);
        } catch (final WSSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private CxfWsContainer container(final Invocation invocation) throws IOException {
        final EndpointInfo endpointInfo = new EndpointInfo();
        endpointInfo.setAddress("http://localhost/ws");

        final CxfWsContainer container = new CxfWsContainer(bus, null, null, null) {
            @Override
            protected CxfEndpoint createEndpoint() {
                return null;
            }

            @Override
            protected ObjectName registerMBean() {
                return null;
            }

            @Override
            protected void setWsldUrl(final String wsdl) {
                // no-op
            }
        };
        container.destination = new HttpDestination(bus, new DestinationRegistryImpl(), endpointInfo, "/ws") {
            @Override
            public void invoke(final ServletConfig config, final ServletContext context,
                               final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    invocation.invoke(req);
                } catch (final IOException | RuntimeException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        return container;
    }

    private static HttpRequest request() {
        final Map<String, Object> attributes = new HashMap<>();
        return (HttpRequest) Proxy.newProxyInstance(CxfWsContainerSecurityStateTest.class.getClassLoader(),
                new Class<?>[]{HttpRequest.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getAttribute":
                            return attributes.get(args[0]);
                        case "setAttribute":
                            attributes.put((String) args[0], args[1]);
                            return null;
                        case "removeAttribute":
                            attributes.remove(args[0]);
                            return null;
                        default:
                            return null;
                    }
                });
    }

    private static final class Step extends AbstractPhaseInterceptor<Message> {
        private final Consumer<Message> action;

        private Step(final String phase, final Consumer<Message> action) {
            super(UUID.randomUUID().toString(), phase);
            this.action = action;
        }

        @Override
        public void handleMessage(final Message message) {
            action.accept(message);
        }
    }

    private interface Invocation {
        void invoke(HttpServletRequest req) throws Exception;
    }

    private static class TestSecurityService extends AbstractSecurityService {
        private final List<UUID> loggedIn = new CopyOnWriteArrayList<>();
        private final List<UUID> loggedOut = new CopyOnWriteArrayList<>();

        @Override
        public UUID login(final String securityRealm, final String user, final String pass) throws LoginException {
            if (!user.equals(pass) && !"secret".equals(pass)) {
                throw new LoginException("bad password");
            }
            final UUID token = registerSubject(createSubject(user, user));
            if (!"outer".equals(user)) {
                loggedIn.add(token);
            }
            return token;
        }

        @Override
        public void logout(final UUID securityIdentity) throws LoginException {
            super.logout(securityIdentity);
            loggedOut.add(securityIdentity);
        }
    }
}

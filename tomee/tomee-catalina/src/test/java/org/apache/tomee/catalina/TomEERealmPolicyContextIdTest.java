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
package org.apache.tomee.catalina;

import jakarta.security.jacc.Policy;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyFactory;
import jakarta.security.jacc.WebResourcePermission;
import jakarta.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.buf.MessageBytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link TomEERealm#evaluatePolicyFactory(Request)} establishes the per-webapp JACC
 * policy context identifier on the calling thread before consulting the {@link PolicyFactory}
 * and restores the previous identifier on exit.
 *
 * <p>Per Jakarta Authorization 3.0, {@code PolicyFactory.getPolicy()} (no-arg) selects the
 * policy from {@code PolicyContext.getContextID()}, and the spec requires the container to
 * associate the identifier with the thread before invoking decision interfaces. Tomcat reuses
 * worker threads, so without an explicit set/restore the request would be evaluated against
 * whatever id leaked from a prior request (or {@code null}). Equivalent to Eclipse Exousia's
 * {@code AuthorizationService.checkPermissionScoped} in the Jakarta Authorization 3.0 RI.</p>
 */
public class TomEERealmPolicyContextIdTest {

    private PolicyFactory previousFactory;
    private String previousContextId;

    @Before
    public void installRecordingFactory() {
        previousFactory = PolicyFactory.getPolicyFactory();
        previousContextId = PolicyContext.getContextID();
    }

    @After
    public void restoreFactory() {
        PolicyFactory.setPolicyFactory(previousFactory);
        // Surefire forks may reuse threads across tests within the same fork.
        try {
            PolicyContext.setContextID(previousContextId);
        } catch (RuntimeException ignored) {
            // Some JACC impls reject null; ignore -- this is best-effort cleanup for the next test.
        }
    }

    @Test
    public void contextIdIsSetForRequestAndRestoredAfterCall() throws Exception {
        // Before the call: contextID is whatever the caller had (possibly null or from another webapp).
        final String outerContextId = "outer-webapp /something-else";
        PolicyContext.setContextID(outerContextId);

        final RecordingPolicyFactory factory = new RecordingPolicyFactory();
        PolicyFactory.setPolicyFactory(factory);

        final Request request = stubRequestFor("the-server", "/myapp", "/secret", "GET");
        final TomEERealm realm = new TomEERealm();

        final Object verdict = invokeEvaluatePolicyFactory(realm, request);

        // The factory observed the per-webapp id during getPolicy() AND during implies().
        assertEquals("the-server /myapp", factory.contextIdAtGetPolicy.get());
        assertEquals("the-server /myapp", factory.policy.contextIdAtImplies.get());
        // After the call: the outer context id is restored.
        assertEquals(outerContextId, PolicyContext.getContextID());

        // Verdict comes from our recording policy (false here).
        assertEquals(Boolean.FALSE, verdict);
    }

    @Test
    public void contextIdIsRestoredEvenWhenPolicyImpliesThrows() throws Exception {
        final String outerContextId = "ejb-context";
        PolicyContext.setContextID(outerContextId);

        final ThrowingPolicyFactory factory = new ThrowingPolicyFactory();
        PolicyFactory.setPolicyFactory(factory);

        final Request request = stubRequestFor("a-host", "/ctx", "/x", "POST");
        final TomEERealm realm = new TomEERealm();

        final Object verdict = invokeEvaluatePolicyFactory(realm, request);

        // The realm swallows the runtime exception and returns null so Catalina falls back.
        assertNull(verdict);
        // The previous context id MUST be restored even on exception path -- otherwise the next
        // request on this thread would inherit "a-host /ctx".
        assertEquals(outerContextId, PolicyContext.getContextID());
    }

    @Test
    public void contextIdNotMutatedWhenRequestHasNoContext() throws Exception {
        // If we can't resolve a context (e.g. request not bound yet), we must NOT clobber the
        // outer context id with null -- doing so would lose an enclosing EJB caller's id.
        final String outerContextId = "enclosing-ejb-context";
        PolicyContext.setContextID(outerContextId);

        final RecordingPolicyFactory factory = new RecordingPolicyFactory();
        PolicyFactory.setPolicyFactory(factory);

        final Request request = mock(Request.class);
        when(request.getContext()).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestPathMB()).thenReturn(MessageBytes.newInstance());
        when(request.getRequest()).thenReturn(null);

        final TomEERealm realm = new TomEERealm();
        invokeEvaluatePolicyFactory(realm, request);

        // The factory still observes whatever contextID was on the thread (the outer id) --
        // the realm did not overwrite it with null.
        assertEquals(outerContextId, factory.contextIdAtGetPolicy.get());
        // And of course the outer id is still on the thread after the call.
        assertEquals(outerContextId, PolicyContext.getContextID());
    }

    @Test
    public void differentRequestsRouteToDifferentPolicies() throws Exception {
        // Simulates the bug: two webapps share the same Tomcat worker thread. The second request
        // must NOT see the first webapp's policy. Demonstrates that we set the right context id
        // per request, so getPolicy() returns the right policy each time.
        final RecordingPolicyFactory factory = new RecordingPolicyFactory();
        PolicyFactory.setPolicyFactory(factory);

        final TomEERealm realm = new TomEERealm();

        invokeEvaluatePolicyFactory(realm, stubRequestFor("host", "/app-A", "/x", "GET"));
        final String firstObserved = factory.contextIdAtGetPolicy.get();

        invokeEvaluatePolicyFactory(realm, stubRequestFor("host", "/app-B", "/y", "GET"));
        final String secondObserved = factory.contextIdAtGetPolicy.get();

        assertEquals("host /app-A", firstObserved);
        assertEquals("host /app-B", secondObserved);
        assertTrue(!firstObserved.equals(secondObserved));
    }

    private static Object invokeEvaluatePolicyFactory(final TomEERealm realm, final Request request) throws Exception {
        final Method m = TomEERealm.class.getDeclaredMethod("evaluatePolicyFactory", Request.class);
        m.setAccessible(true);
        return m.invoke(realm, request);
    }

    private static Request stubRequestFor(final String virtualServer, final String contextPath,
                                          final String requestPath, final String method) {
        final Request request = mock(Request.class);
        final Context context = mock(Context.class);
        final ServletContext servletContext = mock(ServletContext.class);

        when(request.getContext()).thenReturn(context);
        when(context.getServletContext()).thenReturn(servletContext);
        when(servletContext.getVirtualServerName()).thenReturn(virtualServer);
        when(servletContext.getContextPath()).thenReturn(contextPath);

        final MessageBytes pathBytes = MessageBytes.newInstance();
        pathBytes.setString(requestPath);
        when(request.getRequestPathMB()).thenReturn(pathBytes);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequest()).thenReturn(null);
        return request;
    }

    /** Custom factory that captures the JACC context id observed during getPolicy and implies. */
    private static class RecordingPolicyFactory extends PolicyFactory {
        final AtomicReference<String> contextIdAtGetPolicy = new AtomicReference<>();
        final RecordingPolicy policy = new RecordingPolicy();

        @Override
        public Policy getPolicy(final String contextId) {
            // Record what PolicyContext.getContextID() returns on this thread when the realm
            // calls the no-arg getPolicy(). Per spec the no-arg form delegates here with that id.
            contextIdAtGetPolicy.set(PolicyContext.getContextID());
            return policy;
        }

        @Override
        public void setPolicy(final String contextId, final Policy policy) {
            // not exercised by this test
        }
    }

    private static class RecordingPolicy implements Policy {
        final AtomicReference<String> contextIdAtImplies = new AtomicReference<>();

        @Override
        public boolean implies(final Permission permission, final Subject subject) {
            contextIdAtImplies.set(PolicyContext.getContextID());
            return false;
        }

        @Override
        public PermissionCollection getPermissionCollection(final Subject subject) {
            return null;
        }
    }

    /** A factory whose implies() throws -- exercise the finally-restore path. */
    private static class ThrowingPolicyFactory extends PolicyFactory {
        @Override
        public Policy getPolicy(final String contextId) {
            return new ThrowingPolicy();
        }

        @Override
        public void setPolicy(final String contextId, final Policy policy) {
            // not used
        }
    }

    private static class ThrowingPolicy implements Policy {
        @Override
        public boolean implies(final Permission permission, final Subject subject) {
            throw new RuntimeException("boom");
        }

        @Override
        public PermissionCollection getPermissionCollection(final Subject subject) {
            return null;
        }
    }
}

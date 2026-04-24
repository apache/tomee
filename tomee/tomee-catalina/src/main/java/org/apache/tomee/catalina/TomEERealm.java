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

import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyFactory;
import jakarta.security.jacc.WebResourcePermission;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.CombinedRealm;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.core.security.JaccProvider;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.openejb.threads.task.CUTask;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.ietf.jgss.GSSContext;

import javax.security.auth.Subject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.security.cert.X509Certificate;

public class TomEERealm extends CombinedRealm {
    public static final String SECURITY_NOTE = TomEERealm.class.getName() + ".securityContext";

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SECURITY, TomEERealm.class);

    /**
     * Jakarta Authorization 3.0 exposes {@link PolicyContext#setHandlerData(Object)} but not a
     * matching getter. To avoid clobbering outer callers' handler data when we set our own, we
     * read the private thread-local reflectively. If reflection fails (e.g. Jakarta API changes
     * again or a SecurityManager denies access), we fall back to restoring {@code null} which
     * preserves the prior behaviour. This is only a best-effort improvement over the previous
     * hard null-out.
     */
    private static final ThreadLocal<Object> HANDLER_DATA_TL = lookupHandlerDataThreadLocal();

    private TomcatSecurityService securityService;

    @SuppressWarnings("unchecked")
    private static ThreadLocal<Object> lookupHandlerDataThreadLocal() {
        try {
            final Field f = PolicyContext.class.getDeclaredField("threadLocalHandlerData");
            f.setAccessible(true);
            return (ThreadLocal<Object>) f.get(null);
        } catch (final ReflectiveOperationException | RuntimeException e) {
            final Logger init = Logger.getInstance(LogCategory.OPENEJB_SECURITY, TomEERealm.class);
            if (init.isDebugEnabled()) {
                init.debug("Unable to access PolicyContext.threadLocalHandlerData reflectively; "
                        + "handler data will be cleared (not restored) after evaluation", e);
            }
            return null;
        }
    }

    @Override
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        this.securityService = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
    }

    @Override
    public Principal authenticate(final String username, final String password) {
        return logInTomEE(super.authenticate(username, password));
    }

    @Override
    public Principal authenticate(final X509Certificate[] certs) {
        return logInTomEE(super.authenticate(certs));
    }

    @Override
    public Principal authenticate(final String username, final String digest, final String nonce,
                                  final String nc, final String cnonce, final String qop, final String realm,
                                  final String digestA2, final String algorithm) {
        return logInTomEE(super.authenticate(username, digest, nonce, nc, cnonce, qop, realm, digestA2, algorithm));
    }

    @Override
    public Principal authenticate(final GSSContext gssContext, final boolean storeCreds) {
        return logInTomEE(super.authenticate(gssContext, storeCreds));
    }

    @Override
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String rawRole) {
        String role = rawRole;

        // Check for a role alias defined in a <security-role-ref> element
        if (wrapper != null) {
            final String realRole = wrapper.findSecurityReference(role);
            if (realRole != null) {
                role = realRole;
            }
        }

        if (principal == null || role == null) {
            return false;
        }

        if (principal instanceof  GenericPrincipal) {
            return ((GenericPrincipal) principal).hasRole(role);
        }

        for (final Realm realm : realms) { // when used implicitly (always?) realms.size == 1 so no need of a strategy
            if (realm.hasRole(wrapper, principal, rawRole)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasResourcePermission(final Request request, final Response response,
                                         final SecurityConstraint[] constraints,
                                         final Context context) throws IOException {
        if (constraints == null || constraints.length == 0) {
            return true;
        }

        final Boolean verdict = evaluatePolicyFactory(request);
        if (verdict != null) {
            return verdict;
        }

        return super.hasResourcePermission(request, response, constraints, context);
    }

    private Boolean evaluatePolicyFactory(final Request request) {
        final Object previousHandlerData = HANDLER_DATA_TL != null ? HANDLER_DATA_TL.get() : null;
        try {
            PolicyContext.setHandlerData(request.getRequest());

            final PolicyFactory policyFactory = PolicyFactory.getPolicyFactory();
            final jakarta.security.jacc.Policy policy = policyFactory == null ? null : policyFactory.getPolicy();
            if (policy == null || JaccProvider.isSentinelPolicy(policy)) {
                return null;
            }

            final Subject subject = getCurrentSubject();
            final WebResourcePermission permission =
                    new WebResourcePermission(requestPath(request), request.getMethod());
            return policy.implies(permission, subject);
        } catch (final RuntimeException ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PolicyFactory evaluation failed for " + requestPath(request)
                        + " (" + request.getMethod() + "); falling back to Catalina check", ex);
            }
            return null;
        } finally {
            PolicyContext.setHandlerData(previousHandlerData);
        }
    }

    private Subject getCurrentSubject() {
        if (securityService == null) {
            securityService = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
        }
        return securityService != null ? securityService.getCurrentSubject() : new Subject();
    }

    private String requestPath(final Request request) {
        String uri = request.getRequestPathMB().toString();
        if (uri == null || uri.isEmpty()) {
            uri = "/";
        }
        return uri;
    }

    private Principal logInTomEE(final Principal pcp) {
        if (pcp == null) {
            return null;
        }
        if (securityService == null) { // tomee-embedded get it later than startInternals so we need it this way
            securityService = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
        }

        // normally we don't care about old state because the listener already contains one
        // which is the previous one
        // so no need to clean twice here
        final Request request = OpenEJBSecurityListener.requests.get();
        if (request != null) {
            final Object securityContext = securityService.enterWebApp(this, pcp, OpenEJBSecurityListener.requests.get().getWrapper().getRunAs());
            request.setNote(SECURITY_NOTE, securityContext);
        } else {
            final CUTask.Context context = CUTask.Context.CURRENT.get();
            if (context != null) {
                final Object state = securityService.enterWebApp(this, pcp, null);
                context.pushExitTask(new Runnable() {
                    @Override
                    public void run() {
                        securityService.exitWebApp(state);
                    }
                });
            } else {
                final Logger instance = Logger.getInstance(LogCategory.OPENEJB_SECURITY, TomEERealm.class);
                if (instance.isDebugEnabled()) {
                    instance.debug(
                        "No request or concurrency-utilities context so skipping login context propagation, " +
                        "thread=" + Thread.currentThread().getName());
                }
            }
        }
        return pcp;
    }
}

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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.CombinedRealm;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.threads.task.CUTask;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.ietf.jgss.GSSContext;

import java.security.Principal;
import java.security.cert.X509Certificate;

public class TomEERealm extends CombinedRealm {
    public static final String SECURITY_NOTE = TomEERealm.class.getName() + ".securityContext";

    private TomcatSecurityService securityService;

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
    public Principal authenticate(final String username, final String clientDigest,
                                  final String nonce, final String nc, final String cnonce, final String qop,
                                  final String realmName, final String md5a2) {
        return logInTomEE(super.authenticate(username, clientDigest, nonce, nc, cnonce, qop, realmName, md5a2));
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

        for (final Realm realm : realms) { // when used implicitely (always?) realms.size == 1 so no need of a strategy
            if (realm.hasRole(wrapper, principal, rawRole)) {
                return true;
            }
        }
        return false;
    }

    private Principal logInTomEE(final Principal pcp) {
        if (pcp == null) {
            return null;
        }
        if (securityService == null) { // tomee-embedded get it later than startInternals so we need it this way
            securityService = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
        }

        // normally we don't care about oldstate because the listener already contains one
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

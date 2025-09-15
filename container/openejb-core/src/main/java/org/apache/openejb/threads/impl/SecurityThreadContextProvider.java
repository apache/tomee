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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.ClientSecurity;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.Map;

public class SecurityThreadContextProvider implements ThreadContextProvider, Serializable {
    public static final SecurityThreadContextProvider INSTANCE = new SecurityThreadContextProvider();

    private static final SecurityService SECURITY_SERVICE = SystemInstance.get().getComponent(SecurityService.class);

    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        boolean associate = false;
        Object state = SECURITY_SERVICE.currentState();

        if (state == null) {
            state = ClientSecurity.getIdentity();
            associate = state != null;
        }

        final Object securityServiceState = state;
        final AbstractSecurityService.SecurityContext sc = getSecurityContext();

        return new SecurityThreadContextSnapshot(associate, securityServiceState, sc);
    }

    private AbstractSecurityService.SecurityContext getSecurityContext() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();

        if (threadContext == null) {
            return null;
        }

        if (threadContext.getBeanContext() == null) {
            return threadContext.get(AbstractSecurityService.SecurityContext.class);
        }

        final BeanContext beanContext = threadContext.getBeanContext();
        if (beanContext.getRunAs() == null && beanContext.getRunAsUser() == null) {
            return threadContext.get(AbstractSecurityService.SecurityContext.class);
        }

        final AbstractSecurityService securityService = AbstractSecurityService.class.cast(SECURITY_SERVICE);
        return new AbstractSecurityService.SecurityContext(securityService.getRunAsSubject(beanContext));
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return new SecurityThreadContextSnapshot(false, null, null);
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.SECURITY;
    }

    public static class SecurityThreadContextSnapshot implements ThreadContextSnapshot, Serializable {

        private final boolean associate;
        private final Object securityServiceState;
        private final AbstractSecurityService.SecurityContext sc;

        public SecurityThreadContextSnapshot(final boolean associate, final Object securityServiceState, final AbstractSecurityService.SecurityContext sc) {
            this.associate = associate;
            this.securityServiceState = securityServiceState;
            this.sc = sc;
        }

        @Override
        public ThreadContextRestorer begin() {
            final Object threadState;

            if (associate) {
                try {
                    SECURITY_SERVICE.associate(securityServiceState);
                } catch (final LoginException e) {
                    throw new IllegalStateException(e);
                }
                threadState = null;
            } else {
                threadState = SECURITY_SERVICE.currentState();
                SECURITY_SERVICE.setState(securityServiceState);
            }

            final ThreadContext threadContext = ThreadContext.getThreadContext();
            final ThreadContext oldCtx;
            if (threadContext != null) {
                final ThreadContext newContext = new ThreadContext(threadContext);
                oldCtx = ThreadContext.enter(newContext);
                if (sc != null) {
                    newContext.set(AbstractSecurityService.SecurityContext.class, sc);
                }
            } else {
                oldCtx = null;
            }

            return new SecurityThreadContextRestorer(associate, oldCtx, threadState);
        }

        @Override
        public String toString() {
            return "SecurityThreadContextSnapshot@" + System.identityHashCode(this) +
                    "{associate=" + associate +
                    "{securityServiceState=" + securityServiceState +
                    "{sc=" + sc +
                    '}';
        }

    }

    public static class SecurityThreadContextRestorer implements ThreadContextRestorer {

        private final boolean associate;
        private final ThreadContext oldCtx;
        private final Object threadState;

        public SecurityThreadContextRestorer(final boolean associate, final ThreadContext oldCtx, final Object threadState) {
            this.associate = associate;
            this.oldCtx = oldCtx;
            this.threadState = threadState;
        }

        @Override
        public void endContext() throws IllegalStateException {
            if (oldCtx != null) {
                ThreadContext.exit(oldCtx);
            }

            if (!associate) {
                SECURITY_SERVICE.setState(threadState);
            } else {
                SECURITY_SERVICE.disassociate();
            }
        }

        @Override
        public String toString() {
            return "SecurityThreadContextSnapshot@" + System.identityHashCode(this) +
                    "{associate=" + associate +
                    "{oldCtx=" + oldCtx +
                    "{threadState=" + threadState +
                    '}';
        }

    }
}

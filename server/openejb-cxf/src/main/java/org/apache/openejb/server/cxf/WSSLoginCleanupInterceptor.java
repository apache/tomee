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

import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Undoes the logins done by {@link OpenEJBLoginValidator} for a message once it has been processed:
 * the caller identity is removed from the thread which did the login and every login token is logged out.
 * It runs at the end of the in chain (after the response has been sent) and at the end of the out fault chain,
 * so it doesn't depend on the thread which received the request (continuations can resume the chain elsewhere).
 */
public class WSSLoginCleanupInterceptor extends AbstractPhaseInterceptor<Message> {
    public static final WSSLoginCleanupInterceptor IN = new WSSLoginCleanupInterceptor(Phase.POST_INVOKE);
    public static final WSSLoginCleanupInterceptor OUT_FAULT = new WSSLoginCleanupInterceptor(Phase.SETUP_ENDING);

    public WSSLoginCleanupInterceptor(final String phase) {
        super(WSSLoginCleanupInterceptor.class.getName() + "." + phase, phase);
        if (Phase.POST_INVOKE.equals(phase)) {
            addAfter(OutgoingChainInterceptor.class.getName());
        }
    }

    @Override
    public void handleMessage(final Message message) {
        final Logins logins = Logins.of(message, false);
        if (logins != null) {
            logins.release(false);
        }
    }

    @Override
    public void handleFault(final Message message) {
        handleMessage(message);
    }

    static void register(final Message message, final Object token, final Object previousState, final Object state) {
        Logins.of(message, true).add(token, previousState, state);

        final InterceptorChain chain = message.getInterceptorChain();
        if (chain != null && !contains(chain)) {
            chain.add(IN);
        }
    }

    private static boolean contains(final InterceptorChain chain) {
        for (final Object interceptor : chain) {
            if (interceptor == IN) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logins done for a message, shared through the exchange so the fault chain sees them too.
     */
    public static final class Logins {
        public static final String KEY = Logins.class.getName();

        private final List<Login> logins = new ArrayList<>();

        static Logins of(final Message message, final boolean create) {
            final Exchange exchange = message.getExchange();
            final Object existing = exchange != null ? exchange.get(KEY) : message.get(KEY);
            if (existing instanceof Logins found) {
                return found;
            }
            if (!create) {
                return null;
            }
            final Logins logins = new Logins();
            if (exchange != null) {
                exchange.put(KEY, logins);
            } else {
                message.put(KEY, logins);
            }
            return logins;
        }

        synchronized void add(final Object token, final Object previousState, final Object state) {
            logins.add(new Login(token, Thread.currentThread(), previousState, state));
        }

        public synchronized boolean isEmpty() {
            return logins.isEmpty();
        }

        /**
         * Restores the caller of the current thread and logs out the tokens.
         *
         * @param currentThreadOnly only release the logins done by the current thread,
         *                          the other ones are released by their own thread or at the end of the chain
         */
        public void release(final boolean currentThreadOnly) {
            final List<Login> released = new ArrayList<>();
            final Thread current = Thread.currentThread();
            synchronized (this) {
                for (final Iterator<Login> it = logins.iterator(); it.hasNext(); ) {
                    final Login login = it.next();
                    if (!currentThreadOnly || login.thread == current) {
                        released.add(login);
                        it.remove();
                    }
                }
            }
            if (released.isEmpty()) {
                return;
            }

            final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            if (securityService == null) {
                return;
            }

            // latest first so the state before the first login wins
            for (int i = released.size() - 1; i >= 0; i--) {
                final Login login = released.get(i);
                if (login.thread == current && securityService.currentState() == login.state) {
                    securityService.setState(login.previousState);
                }
            }
            for (final Login login : released) {
                try {
                    securityService.logout(login.token);
                } catch (final LoginException e) {
                    // already logged out
                }
            }
        }
    }

    private record Login(Object token, Thread thread, Object previousState, Object state) {
    }
}

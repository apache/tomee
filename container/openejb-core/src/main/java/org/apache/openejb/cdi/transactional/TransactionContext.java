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
package org.apache.openejb.cdi.transactional;

import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.context.creational.BeanInstanceBag;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class TransactionContext extends AbstractContext implements Synchronization {
    private static final Class<?>[] MAP = new Class<?>[]{Map.class};
    private final TransactionManager transactionManager = OpenEJB.getTransactionManager();
    private final boolean geronimoTxMgr = GeronimoTransactionManager.class.isInstance(OpenEJB.getTransactionManager());

    public TransactionContext() {
        super(TransactionScoped.class);
    }

    @Override
    public boolean isActive() {
        try {
            final int status = transactionManager.getTransaction().getStatus();
            return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK
                || status == Status.STATUS_PREPARED || status == Status.STATUS_PREPARING
                || status == Status.STATUS_COMMITTING || status == Status.STATUS_ROLLING_BACK
                || status == Status.STATUS_UNKNOWN;
        } catch (final Throwable e) {
            return false;
        }
    }

    @Override
    protected void checkActive() {
        if (!isActive()) {
            throw new ContextNotActiveException("Context with scope annotation @" + getScope().getName() + " is not active");
        }
    }

    @Override
    protected void setComponentInstanceMap() {
        // TODO: think if not using a proxy could be more clever ;)
        componentInstanceMap = Map.class.cast(Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), MAP,
            new TransactionalMapHandler(
                this, SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class))));
    }

    @Override
    public void beforeCompletion() {
        if (!geronimoTxMgr) {
            // too early but we don't know how to do otherwise ATM
            destroy();
        }
    }

    @Override
    public void afterCompletion(final int status) {
        if (geronimoTxMgr) {
            destroy();
        }
    }

    private static class TransactionalMapHandler implements InvocationHandler {
        private static final String KEY = "@Transactional#OpenEJB.map";
        private final TransactionSynchronizationRegistry registry;
        private final TransactionContext context;

        public TransactionalMapHandler(final TransactionContext transactionContext, final TransactionSynchronizationRegistry registry) {
            this.context = transactionContext;
            this.registry = registry;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(findMap(), args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }

        private Map<Contextual<?>, BeanInstanceBag<?>> findMap() {
            final Object resource;
            try { // we can't call registry.getResource(KEY) in afterCompletion
                resource = context.geronimoTxMgr ?
                    TransactionImpl.class.cast(context.transactionManager.getTransaction()).getResource(KEY):
                    registry.getResource(KEY);
            } catch (final SystemException e) {
                throw new IllegalStateException(e);
            }

            if (resource == null) {
                final Map<Contextual<?>, BeanInstanceBag<?>> map = new HashMap<>();
                registry.putResource(KEY, map);
                registry.registerInterposedSynchronization(context);
                return map;
            }
            return Map.class.cast(resource);
        }
    }
}

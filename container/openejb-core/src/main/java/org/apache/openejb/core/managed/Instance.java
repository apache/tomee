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

package org.apache.openejb.core.managed;

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.transaction.BeanTransactionPolicy.SuspendedTransaction;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.PojoSerialization;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transaction;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Instance implements Serializable {
    private static final long serialVersionUID = 2862563626506556542L;
    public final BeanContext beanContext;
    public final Object primaryKey;
    public final Object bean;
    public final CreationalContext creationalContext;
    public final Map<String, Object> interceptors;

    private boolean inUse;
    private SuspendedTransaction beanTransaction;
    private final Stack<Transaction> transaction = new Stack<>();
    private final ReentrantLock lock = new ReentrantLock();

    // todo if we keyed by an entity manager factory id we would not have to make this transient and rebuild the index below
    // This would require that we crete an id and that we track it
    // alternatively, we could use ImmutableArtifact with some read/write replace magic
    private Map<EntityManagerFactory, JtaEntityManagerRegistry.EntityManagerTracker> entityManagers;
    private final JtaEntityManagerRegistry.EntityManagerTracker[] entityManagerArray;

    public Instance(final BeanContext beanContext, final Object primaryKey, final Object bean, final Map<String, Object> interceptors, final CreationalContext creationalContext, final Map<EntityManagerFactory, JtaEntityManagerRegistry.EntityManagerTracker> entityManagers) {
        this.beanContext = beanContext;
        this.primaryKey = primaryKey;
        this.bean = bean;
        this.interceptors = interceptors;
        this.creationalContext = creationalContext;
        this.entityManagers = entityManagers;
        this.entityManagerArray = null;
    }

    public Instance(final Object deploymentId, final Object primaryKey, final Object bean, final Map<String, Object> interceptors, final CreationalContext creationalContext, final JtaEntityManagerRegistry.EntityManagerTracker[] entityManagerArray) {
        this.beanContext = SystemInstance.get().getComponent(ContainerSystem.class).getBeanContext(deploymentId);
        if (beanContext == null) {
            throw new IllegalArgumentException("Unknown deployment " + deploymentId);
        }
        this.primaryKey = primaryKey;
        this.bean = bean;
        this.interceptors = interceptors;
        this.creationalContext = creationalContext;
        this.entityManagerArray = entityManagerArray;
    }

    public Duration getTimeOut() {
        return beanContext.getStatefulTimeout();
    }

    public synchronized boolean isInUse() {
        return inUse;
    }

    public synchronized void setInUse(final boolean inUse) {
        this.inUse = inUse;
    }

    public synchronized SuspendedTransaction getBeanTransaction() {
        return beanTransaction;
    }

    public synchronized void setBeanTransaction(final SuspendedTransaction beanTransaction) {
        this.beanTransaction = beanTransaction;
    }

    public synchronized Transaction getTransaction() {
        return transaction.size() > 0 ? transaction.peek() : null;
    }

    public Lock getLock() {
        return lock;
    }

    public synchronized void setTransaction(final Transaction transaction) {
        if (this.transaction.size() == 0 && transaction != null) {
            lock.lock();
            this.transaction.push(transaction);
        } else if (this.transaction.size() != 0 && transaction == null) {
            this.transaction.pop();
            lock.unlock();
        } else if (transaction != null) {
            this.transaction.push(transaction);
        }
    }

    public synchronized void releaseLock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public synchronized Map<EntityManagerFactory, JtaEntityManagerRegistry.EntityManagerTracker> getEntityManagers(
            final Index<EntityManagerFactory, BeanContext.EntityManagerConfiguration> factories) {
        if (entityManagers == null && entityManagerArray != null) {
            entityManagers = new HashMap<>();
            for (int i = 0; i < entityManagerArray.length; i++) {
                final EntityManagerFactory entityManagerFactory = factories.getKey(i);
                final JtaEntityManagerRegistry.EntityManagerTracker entityManager = entityManagerArray[i];
                entityManagers.put(entityManagerFactory, entityManager);
            }
        }
        return entityManagers;
    }

    protected Object writeReplace() throws ObjectStreamException {
        if (inUse) {
            throw new IllegalStateException("Bean is still in use");
        }
        if (beanTransaction != null) {
            throw new IllegalStateException("Bean is associated with a bean-managed transaction");
        }
        return new Serialization(this);
    }

    private static class Serialization implements Serializable {
        private static final long serialVersionUID = 6002078080752564395L;
        public final Object deploymentId;
        public final Object primaryKey;
        public final Object bean;
        public final Map<String, Object> interceptors;
        public final CreationalContext creationalContext;
        public final JtaEntityManagerRegistry.EntityManagerTracker[] entityManagerArray;

        public Serialization(final Instance i) {
            deploymentId = i.beanContext.getDeploymentID();
            primaryKey = i.primaryKey;
            bean = toSerializable(i.bean);
            creationalContext = i.creationalContext;

            interceptors = new HashMap<>(i.interceptors.size());
            for (final Map.Entry<String, Object> e : i.interceptors.entrySet()) {
                if (e.getValue() == i.bean) {
                    // need to use the same wrapped reference or well get two copies.
                    interceptors.put(e.getKey(), bean);
                } else {
                    interceptors.put(e.getKey(), toSerializable(e.getValue()));
                }
            }

            if (i.entityManagerArray != null) {
                entityManagerArray = i.entityManagerArray;
            } else if (i.entityManagers != null) {
                entityManagerArray = i.entityManagers.values().toArray(new JtaEntityManagerRegistry.EntityManagerTracker[i.entityManagers.values().size()]);
            } else {
                entityManagerArray = null;
            }
        }

        private static Object toSerializable(final Object obj) {
            if (obj instanceof Serializable) {
                return obj;
            } else {
                return new PojoSerialization(obj);
            }
        }

        protected Object readResolve() throws ObjectStreamException {
            // Anything wrapped with PojoSerialization will have been automatically
            // unwrapped via it's own readResolve so passing in the raw bean
            // and interceptors variables is totally fine.
            return new Instance(deploymentId, primaryKey, bean, interceptors, creationalContext, entityManagerArray);
        }
    }
}

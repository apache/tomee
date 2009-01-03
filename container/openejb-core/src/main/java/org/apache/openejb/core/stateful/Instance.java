/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.stateful;

import java.io.Serializable;
import java.io.ObjectStreamException;
import java.util.Map;
import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.transaction.BeanTransactionPolicy.SuspendedTransaction;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.PojoSerialization;

public class Instance implements Serializable {
    private static final long serialVersionUID = 2862563626506556542L;
    public final CoreDeploymentInfo deploymentInfo;
    public final Object primaryKey;
    public final Object bean;
    public final Map<String, Object> interceptors;

    private boolean inUse;
    private SuspendedTransaction beanTransaction;

    // todo if we keyed by an entity manager factory id we would not have to make this transient and rebuild the index below
    // This would require that we crete an id and that we track it
    // alternatively, we could use ImmutableArtifact with some read/write replace magic
    private Map<EntityManagerFactory, EntityManager> entityManagers;
    private final EntityManager[] entityManagerArray;

    public Instance(CoreDeploymentInfo deploymentInfo, Object primaryKey, Object bean, Map<String, Object> interceptors, Map<EntityManagerFactory, EntityManager> entityManagers) {
        this.deploymentInfo = deploymentInfo;
        this.primaryKey = primaryKey;
        this.bean = bean;
        this.interceptors = interceptors;
        this.entityManagers = entityManagers;
        this.entityManagerArray = null;
    }

    public Instance(Object deploymentId, Object primaryKey, Object bean, Map<String, Object> interceptors, EntityManager[] entityManagerArray) {
        this.deploymentInfo = (CoreDeploymentInfo) SystemInstance.get().getComponent(ContainerSystem.class).getDeploymentInfo(deploymentId);
        if (deploymentInfo == null) {
            throw new IllegalArgumentException("Unknown deployment " + deploymentId);
        }
        this.primaryKey = primaryKey;
        this.bean = bean;
        this.interceptors = interceptors;
        this.entityManagerArray = entityManagerArray;
    }

    public synchronized boolean isInUse() {
        return inUse;
    }

    public synchronized void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public synchronized SuspendedTransaction getBeanTransaction() {
        return beanTransaction;
    }

    public synchronized void setBeanTransaction(SuspendedTransaction beanTransaction) {
        this.beanTransaction = beanTransaction;
    }

    public synchronized Map<EntityManagerFactory, EntityManager> getEntityManagers(Index<EntityManagerFactory, Map> factories) {
        if (entityManagers == null && entityManagerArray != null) {
            entityManagers = new HashMap<EntityManagerFactory, EntityManager>();
            for (int i = 0; i < entityManagerArray.length; i++) {
                EntityManagerFactory entityManagerFactory = factories.getKey(i);
                EntityManager entityManager = entityManagerArray[i];
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
        public final EntityManager[] entityManagerArray;

        public Serialization(Instance i) {
            deploymentId = i.deploymentInfo.getDeploymentID();
            primaryKey = i.primaryKey;
            if (i.bean instanceof Serializable) {
                bean = i.bean;
            } else {
                bean = new PojoSerialization(i.bean);
            }

            interceptors = new HashMap(i.interceptors.size());
            for (Map.Entry<String, Object> e : i.interceptors.entrySet()) {
                if (e.getValue() == i.bean) {
                    // need to use the same wrapped reference or well get two copies.
                    interceptors.put(e.getKey(), bean);
                } else if (!(e.getValue() instanceof Serializable)) {
                    interceptors.put(e.getKey(), new PojoSerialization(e.getValue()));
                }
            }

            if (i.entityManagerArray != null) {
                entityManagerArray = i.entityManagerArray;
            } else if (i.entityManagers != null) {
                entityManagerArray = i.entityManagers.values().toArray(new EntityManager[i.entityManagers.values().size()]);
            } else {
                entityManagerArray = null;
            }
        }

        protected Object readResolve() throws ObjectStreamException {
            // Anything wrapped with PojoSerialization will have been automatically
            // unwrapped via it's own readResolve so passing in the raw bean
            // and interceptors variables is totally fine.
            return new Instance(deploymentId, primaryKey, bean, interceptors, entityManagerArray);
        }
    }
}

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
package org.apache.openejb.persistence;


import java.util.Map;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TransactionRequiredException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * The JtaEntityManagerRegistry tracks JTA entity managers for transation and extended scoped
 * entity managers.  A signle instance of this object should be created and shared by all
 * JtaEntityManagers in the server instance.  Failure to do this will result in multiple entity
 * managers being created for a single persistence until, and that will result in cache
 * incoherence.
 */
public class JtaEntityManagerRegistry {
    /**
     * Registry of transaction associated entity managers.
     */
    private final TransactionSynchronizationRegistry transactionRegistry;

    /**
     * Registry of entended context entity managers.
     */
    private final ThreadLocal<ExtendedRegistry> extendedRegistry = new ThreadLocal<ExtendedRegistry>() {
        protected ExtendedRegistry initialValue() {
            return new ExtendedRegistry();
        }
    };

    /**
     * Creates a JtaEntityManagerRegistry using the specified transactionSynchronizationRegistry for the registry
     * if transaction associated entity managers.
     */
    public JtaEntityManagerRegistry(TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionRegistry = transactionSynchronizationRegistry;
    }

    /**
     * Gets an entity manager instance from the transaction registry, extended regitry or for a transaction scoped
     * entity manager, creates a new one when an exisitng instance is not found.
     * </p>
     * It is important that a component adds extended scoped entity managers to this registry when the component is
     * entered and removes them when exited.  If this registration is not preformed, an IllegalStateException will
     * be thrown when entity manger is fetched.
     * @param entityManagerFactory the entity manager factory from which an entity manager is required
     * @param properties the properties passed to the entity manager factory when an entity manager is created
     * @param extended is the entity manager an extended context
     * @return the new entity manager
     * @throws IllegalStateException if the entity manger is extended and there is not an existing entity manager
     * instance already registered
     */
    public EntityManager getEntityManager(EntityManagerFactory entityManagerFactory, Map properties, boolean extended) throws IllegalStateException {
        EntityManagerTxKey txKey = new EntityManagerTxKey(entityManagerFactory);
        boolean transactionActive = isTransactionActive();

        // if we have an active transaction, check the tx registry
        if (transactionActive) {
            EntityManager entityManager = (EntityManager) transactionRegistry.getResource(txKey);
            if (entityManager != null) {
                return entityManager;
            }
        }

        // if extended context, there must be an entity manager already registered with the tx
        if (extended) {
            EntityManager entityManager = getInheritedEntityManager(entityManagerFactory);
            if (entityManager == null) {
                throw new IllegalStateException("InternalError: an entity manager should already be registered for this entended persistence unit");
            }

            // if transaction is active, we need to register the entity manager with the transaction manager
            if (transactionActive) {
                entityManager.joinTransaction();
                transactionRegistry.putResource(txKey, entityManager);
            }

            return entityManager;
        } else {
            // create a new entity manager
            EntityManager entityManager;
            if (properties != null) {
                entityManager = entityManagerFactory.createEntityManager(properties);
            } else {
                entityManager = entityManagerFactory.createEntityManager();
            }

            // if we are in a transaction associate the entity manager with the transaction; otherwise it is
            // expected the caller will close this entity manager after use
            if (transactionActive) {
                transactionRegistry.registerInterposedSynchronization(new CloseEntityManager(entityManager));
                transactionRegistry.putResource(txKey, entityManager);
            }
            return entityManager;
        }
    }

    /**
     * Adds the entity managers for the specified component to the registry.  This should be called when the component
     * is entered.
     * @param deploymentId the id of the component
     * @param entityManagers the entity managers to register
     * @throws EntityManagerAlreadyRegisteredException if an entity manager is already registered with the transaction
     * for one of the supplied entity manager factories; for EJBs this should be caught and rethown as an EJBException
     */
    public void addEntityManagers(String deploymentId, Object primaryKey, Map<EntityManagerFactory, EntityManager> entityManagers) throws EntityManagerAlreadyRegisteredException {
        extendedRegistry.get().addEntityManagers(new InstanceId(deploymentId, primaryKey), entityManagers);
    }

    /**
     * Removed the registered entity managers for the specified component.
     * @param deploymentId the id of the component
     */
    public void removeEntityManagers(String deploymentId, Object primaryKey) {
        extendedRegistry.get().removeEntityManagers(new InstanceId(deploymentId, primaryKey));
    }

    /**
     * Gets an exiting extended entity manager created by a component down the call stack.
     * @param entityManagerFactory the entity manager factory from which an entity manager is needed
     * @return the existing entity manager or null if one is not found
     */
    public EntityManager getInheritedEntityManager(EntityManagerFactory entityManagerFactory) {
        return extendedRegistry.get().getInheritedEntityManager(entityManagerFactory);
    }

    /**
     * Notifies the registry that a user transaction has been started or the specified component.  When a transaction
     * is started for a component with registered extended entity managers, the entity managers are enrolled in the
     * transaction.
     * @param deploymentId the id of the component
     */
    public void transactionStarted(String deploymentId, Object primaryKey) {
        extendedRegistry.get().transactionStarted(new InstanceId(deploymentId, primaryKey));
    }

    /**
     * Is a transaction active?
     * @return true if a transaction is active; false otherwise
     */
    public boolean isTransactionActive() {
        int txStatus = transactionRegistry.getTransactionStatus();
        boolean transactionActive = txStatus == Status.STATUS_ACTIVE || txStatus == Status.STATUS_MARKED_ROLLBACK;
        return transactionActive;
    }

    private class ExtendedRegistry {
        private final Map<InstanceId, Map<EntityManagerFactory, EntityManager>> entityManagersByDeploymentId =
                new TreeMap<InstanceId, Map<EntityManagerFactory, EntityManager>>();

        private void addEntityManagers(InstanceId instanceId, Map<EntityManagerFactory, EntityManager> entityManagers) throws EntityManagerAlreadyRegisteredException {
            if (instanceId == null) throw new NullPointerException("instanceId is null");
            if (entityManagers == null) throw new NullPointerException("entityManagers is null");

            if (isTransactionActive()) {
                for (Map.Entry<EntityManagerFactory, EntityManager> entry : entityManagers.entrySet()) {
                    EntityManagerFactory entityManagerFactory = entry.getKey();
                    EntityManager entityManager = entry.getValue();
                    EntityManagerTxKey txKey = new EntityManagerTxKey(entityManagerFactory);

                    if (transactionRegistry.getResource(txKey) == null) {
                        throw new EntityManagerAlreadyRegisteredException("Another entity manager is already registered for this persistence unit");
                    }

                    entityManager.joinTransaction();
                    transactionRegistry.putResource(txKey, entityManager);
                }
            }
            entityManagersByDeploymentId.put(instanceId, entityManagers);
        }

        private void removeEntityManagers(InstanceId instanceId) {
            if (instanceId == null) {
                throw new NullPointerException("InstanceId is null");
            }

            entityManagersByDeploymentId.remove(instanceId);
        }

        private EntityManager getInheritedEntityManager(EntityManagerFactory entityManagerFactory) {
            if (entityManagerFactory == null) throw new NullPointerException("entityManagerFactory is null");

            for (Map<EntityManagerFactory, EntityManager> entityManagers : entityManagersByDeploymentId.values()) {
                EntityManager entityManager = entityManagers.get(entityManagerFactory);
                if (entityManager != null) {
                    return entityManager;
                }
            }
            return null;
        }

        private void transactionStarted(InstanceId instanceId) {
            if (instanceId == null) {
                throw new NullPointerException("instanceId is null");
            }
            if (!isTransactionActive()) {
                throw new TransactionRequiredException();
            }

            Map<EntityManagerFactory, EntityManager> entityManagers = entityManagersByDeploymentId.get(instanceId);
            if (entityManagers == null) {
                return;
            }

            for (EntityManager entityManager : entityManagers.values()) {
                entityManager.joinTransaction();
            }
        }
    }

    private static class InstanceId {
        private final String deploymentId;
        private final Object primaryKey;

        public InstanceId(String deploymentId, Object primaryKey) {
            if (deploymentId == null) {
                throw new NullPointerException("deploymentId is null");
            }
            if (primaryKey == null) {
                throw new NullPointerException("primaryKey is null");
            }
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final InstanceId that = (InstanceId) o;
            return deploymentId.equals(that.deploymentId) &&
                    primaryKey.equals(that.primaryKey);

        }

        public int hashCode() {
            int result;
            result = deploymentId.hashCode();
            result = 29 * result + primaryKey.hashCode();
            return result;
        }
    }

    private static class CloseEntityManager implements Synchronization {
        private final EntityManager entityManager;

        public CloseEntityManager(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int i) {
            entityManager.close();
        }
    }
}

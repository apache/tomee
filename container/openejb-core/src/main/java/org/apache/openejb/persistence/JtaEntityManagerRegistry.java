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

package org.apache.openejb.persistence;


import org.apache.openejb.util.Geronimo;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.TransactionRequiredException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.util.HashMap;
import java.util.Map;

/**
 * The JtaEntityManagerRegistry tracks JTA entity managers for transaction and extended scoped
 * entity managers.  A single instance of this object should be created and shared by all
 * JtaEntityManagers in the server instance.  Failure to do this will result in multiple entity
 * managers being created for a single persistence until, and that will result in cache
 * incoherence.
 */
public class JtaEntityManagerRegistry {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("persistence"), JtaEntityManager.class);

    /**
     * Registry of transaction associated entity managers.
     */
    private final TransactionSynchronizationRegistry transactionRegistry;

    /**
     * Registry of extended context entity managers.
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
    public JtaEntityManagerRegistry(final TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionRegistry = transactionSynchronizationRegistry;
    }

    /**
     * Gets an entity manager instance from the transaction registry, extended registry or for a transaction scoped
     * entity manager, creates a new one when an existing instance is not found.
     *
     * It is important that a component adds extended scoped entity managers to this registry when the component is
     * entered and removes them when exited.  If this registration is not performed, an IllegalStateException will
     * be thrown when entity manger is fetched.
     *
     * @param entityManagerFactory the entity manager factory from which an entity manager is required
     * @param properties           the properties passed to the entity manager factory when an entity manager is created
     * @param extended             is the entity manager an extended context
     * @param unitName
     * @return the new entity manager
     * @throws IllegalStateException if the entity manger is extended and there is not an existing entity manager
     *                               instance already registered
     */
    @Geronimo
    public EntityManager getEntityManager(final EntityManagerFactory entityManagerFactory,
                                          final Map properties, final boolean extended, final String unitName,
                                          final SynchronizationType synchronizationType) throws IllegalStateException {
        if (entityManagerFactory == null) {
            throw new NullPointerException("entityManagerFactory is null");
        }
        final EntityManagerTxKey txKey = new EntityManagerTxKey(entityManagerFactory);
        final boolean transactionActive = isTransactionActive();

        // if we have an active transaction, check the tx registry
        if (transactionActive) {
            final EntityManager entityManager = (EntityManager) transactionRegistry.getResource(txKey);
            if (entityManager != null) {
                return entityManager;
            }
        }

        // if extended context, there must be an entity manager already registered with the tx
        if (extended) {
            final EntityManagerTracker entityManagerTracker = getInheritedEntityManager(entityManagerFactory);
            if (entityManagerTracker == null || entityManagerTracker.getEntityManager() == null) {
                throw new IllegalStateException("InternalError: an entity manager should already be registered for this extended persistence unit");
            }
            final EntityManager entityManager = entityManagerTracker.getEntityManager();

            // if transaction is active, we need to register the entity manager with the transaction manager
            if (transactionActive) {
                if (entityManagerTracker.autoJoinTx) {
                    entityManager.joinTransaction();
                }
                transactionRegistry.putResource(txKey, entityManager);
            }

            return entityManager;
        } else {

            // create a new entity manager
            final EntityManager entityManager;
            if (synchronizationType != null) {
                if (properties != null) {
                    entityManager = entityManagerFactory.createEntityManager(synchronizationType, properties);
                } else {
                    entityManager = entityManagerFactory.createEntityManager(synchronizationType);
                }
            } else if (properties != null) {
                entityManager = entityManagerFactory.createEntityManager(properties);
            } else {
                entityManager = entityManagerFactory.createEntityManager();
            }

            logger.debug("Created EntityManager(unit=" + unitName + ", hashCode=" + entityManager.hashCode() + ")");

            // if we are in a transaction associate the entity manager with the transaction; otherwise it is
            // expected the caller will close this entity manager after use
            if (transactionActive) {
                transactionRegistry.registerInterposedSynchronization(new CloseEntityManager(entityManager, unitName));
                transactionRegistry.putResource(txKey, entityManager);
            }
            return entityManager;
        }
    }

    /**
     * Adds the entity managers for the specified component to the registry.  This should be called when the component
     * is entered.
     *
     * @param deploymentId   the id of the component
     * @param entityManagers the entity managers to register
     * @throws EntityManagerAlreadyRegisteredException if an entity manager is already registered with the transaction
     *                                                 for one of the supplied entity manager factories; for EJBs this should be caught and rethown as an EJBException
     */
    public void addEntityManagers(final String deploymentId, final Object primaryKey, final Map<EntityManagerFactory, EntityManagerTracker> entityManagers) throws EntityManagerAlreadyRegisteredException {
        extendedRegistry.get().addEntityManagers(new InstanceId(deploymentId, primaryKey), entityManagers);
    }

    /**
     * Removed the registered entity managers for the specified component.
     *
     * @param deploymentId the id of the component
     * @return EntityManager map we are removing
     */
    public Map<EntityManagerFactory, EntityManagerTracker> removeEntityManagers(final String deploymentId, final Object primaryKey) {
        return extendedRegistry.get().removeEntityManagers(new InstanceId(deploymentId, primaryKey));
    }

    /**
     * Gets an exiting extended entity manager created by a component down the call stack.
     *
     * @param entityManagerFactory the entity manager factory from which an entity manager is needed
     * @return the existing entity manager or null if one is not found
     */
    public EntityManagerTracker getInheritedEntityManager(final EntityManagerFactory entityManagerFactory) {
        return extendedRegistry.get().getInheritedEntityManager(entityManagerFactory);
    }

    /**
     * Notifies the registry that a user transaction has been started or the specified component.  When a transaction
     * is started for a component with registered extended entity managers, the entity managers are enrolled in the
     * transaction.
     *
     * @param deploymentId the id of the component
     */
    public void transactionStarted(final String deploymentId, final Object primaryKey) {
        extendedRegistry.get().transactionStarted(new InstanceId(deploymentId, primaryKey));
    }

    /**
     * Is a transaction active?
     *
     * @return true if a transaction is active; false otherwise
     */
    public boolean isTransactionActive() {
        final int txStatus = transactionRegistry.getTransactionStatus();
        final boolean transactionActive = txStatus == Status.STATUS_ACTIVE || txStatus == Status.STATUS_MARKED_ROLLBACK;
        return transactionActive;
    }

    private class ExtendedRegistry {
        private final Map<InstanceId, Map<EntityManagerFactory, EntityManagerTracker>> entityManagersByDeploymentId =
            new HashMap<>();

        private void addEntityManagers(final InstanceId instanceId, final Map<EntityManagerFactory, EntityManagerTracker> entityManagers)
            throws EntityManagerAlreadyRegisteredException {
            if (instanceId == null) {
                throw new NullPointerException("instanceId is null");
            }
            if (entityManagers == null) {
                throw new NullPointerException("entityManagers is null");
            }

            if (isTransactionActive()) {
                for (final Map.Entry<EntityManagerFactory, EntityManagerTracker> entry : entityManagers.entrySet()) {
                    final EntityManagerFactory entityManagerFactory = entry.getKey();
                    final EntityManagerTracker tracker = entry.getValue();
                    final EntityManager entityManager = tracker.getEntityManager();
                    final EntityManagerTxKey txKey = new EntityManagerTxKey(entityManagerFactory);
                    final EntityManager oldEntityManager = (EntityManager) transactionRegistry.getResource(txKey);
                    if (entityManager == oldEntityManager) {
                        break;
                    }
                    if (oldEntityManager != null) {
                        throw new EntityManagerAlreadyRegisteredException("Another entity manager is already registered for this persistence unit");
                    }

                    if (tracker.autoJoinTx) {
                        entityManager.joinTransaction();
                    }
                    transactionRegistry.putResource(txKey, entityManager);
                }
            }
            entityManagersByDeploymentId.put(instanceId, entityManagers);
        }

        private Map<EntityManagerFactory, EntityManagerTracker> removeEntityManagers(final InstanceId instanceId) {
            if (instanceId == null) {
                throw new NullPointerException("InstanceId is null");
            }

            return entityManagersByDeploymentId.remove(instanceId);
        }

        private EntityManagerTracker getInheritedEntityManager(final EntityManagerFactory entityManagerFactory) {
            if (entityManagerFactory == null) {
                throw new NullPointerException("entityManagerFactory is null");
            }

            for (final Map<EntityManagerFactory, EntityManagerTracker> entityManagers : entityManagersByDeploymentId.values()) {
                final EntityManagerTracker entityManagerTracker = entityManagers.get(entityManagerFactory);
                if (entityManagerTracker != null) {
                    return entityManagerTracker;
                }
            }
            return null;
        }

        private void transactionStarted(final InstanceId instanceId) {
            if (instanceId == null) {
                throw new NullPointerException("instanceId is null");
            }
            if (!isTransactionActive()) {
                throw new TransactionRequiredException();
            }

            final Map<EntityManagerFactory, EntityManagerTracker> entityManagers = entityManagersByDeploymentId.get(instanceId);
            if (entityManagers == null) {
                return;
            }

            for (final Map.Entry<EntityManagerFactory, EntityManagerTracker> entry : entityManagers.entrySet()) {
                final EntityManagerFactory entityManagerFactory = entry.getKey();
                final EntityManagerTracker value = entry.getValue();
                final EntityManager entityManager = value.getEntityManager();
                if (value.autoJoinTx) {
                    entityManager.joinTransaction();
                }
                final EntityManagerTxKey txKey = new EntityManagerTxKey(entityManagerFactory);
                transactionRegistry.putResource(txKey, entityManager);
            }
        }
    }

    private static class InstanceId {
        private final String deploymentId;
        private final Object primaryKey;

        public InstanceId(final String deploymentId, final Object primaryKey) {
            if (deploymentId == null) {
                throw new NullPointerException("deploymentId is null");
            }
            if (primaryKey == null) {
                throw new NullPointerException("primaryKey is null");
            }
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(final Object o) {
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

    /**
     * This object is used track all EntityManagers inherited in order
     * to effectively close it when the latest Extended persistence context
     * is no more accessed.
     */
    public static class EntityManagerTracker {
        // must take care of the first inheritance level
        private transient int counter;
        private final EntityManager entityManager;
        private final boolean autoJoinTx;

        public EntityManagerTracker(final EntityManager entityManager, final boolean autoJoinTx) {
            if (entityManager == null) {
                throw new NullPointerException("entityManager is null.");
            }

            this.counter = 0;
            this.entityManager = entityManager;
            this.autoJoinTx = autoJoinTx;
        }

        public int incCounter() {
            return counter++;
        }

        public int decCounter() {
            return counter--;
        }

        public EntityManager getEntityManager() {
            return entityManager;
        }
    }

    private static class CloseEntityManager implements Synchronization {
        private final EntityManager entityManager;
        private final String unitName;

        public CloseEntityManager(final EntityManager entityManager, final String unitName) {
            this.entityManager = entityManager;
            this.unitName = unitName;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(final int i) {
            entityManager.close();
            logger.debug("Closed EntityManager(unit=" + unitName + ", hashCode=" + entityManager.hashCode() + ")");
        }
    }
}

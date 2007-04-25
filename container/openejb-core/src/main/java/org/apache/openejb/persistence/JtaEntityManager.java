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
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TransactionRequiredException;

/**
 * The JtaEntityManager is a wrapper around an entity manager that automatically creates and closes entity managers
 * for each transaction in which it is accessed.  This implementation supports both transaction and extended scoped
 * JTA entity managers.
 * </p>
 * It is important that extended scoped entity managers add entity managers to the JtaEntityManagerRegistry when the
 * component is entered and remove them when exited.  If this registration is not preformed, an IllegalStateException
 * will be thrown when entity manger is used.
 * It is important that a component adds extended scoped entity managers to the JtaEntityManagerRegistry when the
 * component is entered and removes them when exited.  If this registration is not preformed, an IllegalStateException will
 * be thrown when entity manger is accessed.
 */
public class JtaEntityManager implements EntityManager {
    private final JtaEntityManagerRegistry registry;
    private final EntityManagerFactory entityManagerFactory;
    private final Map properties;
    private final boolean extended;

    public JtaEntityManager(JtaEntityManagerRegistry registry, EntityManagerFactory entityManagerFactory, Map properties) {
        this(registry, entityManagerFactory, properties, false);

    }
    public JtaEntityManager(JtaEntityManagerRegistry registry, EntityManagerFactory entityManagerFactory, Map properties, boolean extended) {
        if (registry == null) throw new NullPointerException("registry is null");
        if (entityManagerFactory == null) throw new NullPointerException("entityManagerFactory is null");
        this.registry = registry;
        this.entityManagerFactory = entityManagerFactory;
        this.properties = properties;
        this.extended = extended;
    }

    private EntityManager getEntityManager() {
        return registry.getEntityManager(entityManagerFactory, properties, extended);
    }

    private boolean isTransactionActive() {
        return registry.isTransactionActive();
    }

    /**
     * This method assures that a non-extended entity managers has an acive transaction.  This is
     * required for some operations on the entity manager.
     * @throws TransactionRequiredException if non-extended and a transaction is not active
     */
    private void assertTransactionActive() throws TransactionRequiredException {
        if (!extended && !isTransactionActive()) {
            throw new TransactionRequiredException();
        }
    }

    /**
     * Closes a non-extended entity manager if no transaction is active.  For methods on an
     * entity manager that do not require an active transaction, a temp entity manager is created
     * for the operation and then closed.
     * @param entityManager the entity manager to close if non-extended and a transaction is not active
     */
    private void closeIfNoTx(EntityManager entityManager) {
        if (!extended && !isTransactionActive()) {
            entityManager.close();
        }
    }

    public EntityManager getDelegate() {
        return getEntityManager();
    }

    public void persist(Object entity) {
        assertTransactionActive();
        getEntityManager().persist(entity);
    }

    public <T>T merge(T entity) {
        assertTransactionActive();
        return getEntityManager().merge(entity);
    }

    public void remove(Object entity) {
        assertTransactionActive();
        getEntityManager().remove(entity);
    }

    public <T>T find(Class<T> entityClass, Object primaryKey) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.find(entityClass, primaryKey);
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public <T>T getReference(Class<T> entityClass, Object primaryKey) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.getReference(entityClass, primaryKey);
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public void flush() {
        assertTransactionActive();
        getEntityManager().flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.setFlushMode(flushMode);
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public FlushModeType getFlushMode() {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.getFlushMode();
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public void lock(Object entity, LockModeType lockMode) {
        assertTransactionActive();
        getEntityManager().lock(entity, lockMode);
    }

    public void refresh(Object entity) {
        assertTransactionActive();
        getEntityManager().refresh(entity);
    }

    public void clear() {
        if (!isTransactionActive()) {
            return;
        }
        getEntityManager().clear();
    }

    public boolean contains(Object entity) {
        return isTransactionActive() && getEntityManager().contains(entity);
    }

    public Query createQuery(String qlString) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createQuery(qlString);
        return proxyIfNoTx(entityManager, query);
    }

    public Query createNamedQuery(String name) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNamedQuery(name);
        return proxyIfNoTx(entityManager, query);
    }

    public Query createNativeQuery(String sqlString) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery(sqlString);
        return proxyIfNoTx(entityManager, query);
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery(sqlString, resultClass);
        return proxyIfNoTx(entityManager, query);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery(sqlString, resultSetMapping);
        return proxyIfNoTx(entityManager, query);
    }

    private Query proxyIfNoTx(EntityManager entityManager, Query query) {
        if (!extended && !isTransactionActive()) {
            return new JtaQuery(entityManager, query);
        }
        return query;
    }

    public void joinTransaction() {
    }

    public void close() {
    }

    public boolean isOpen() {
        return true;
    }

    public EntityTransaction getTransaction() {
        throw new IllegalStateException("A JTA Entity Manager can not use an entity transaction");
    }
}

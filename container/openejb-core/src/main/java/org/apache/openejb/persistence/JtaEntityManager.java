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

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import java.util.Map;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

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

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("persistence"), JtaEntityManager.class);

    private final JtaEntityManagerRegistry registry;
    private final EntityManagerFactory entityManagerFactory;
    private final Map properties;
    private final boolean extended;
    private final String unitName;

    public JtaEntityManager(JtaEntityManagerRegistry registry, EntityManagerFactory entityManagerFactory, Map properties, String unitName) {
        this(unitName, registry, entityManagerFactory, properties, false);
    }
    
    public JtaEntityManager(String unitName, JtaEntityManagerRegistry registry, EntityManagerFactory entityManagerFactory, Map properties, boolean extended) {
        if (registry == null) throw new NullPointerException("registry is null");
        if (entityManagerFactory == null) throw new NullPointerException("entityManagerFactory is null");
        this.unitName = unitName;
        this.registry = registry;
        this.entityManagerFactory = entityManagerFactory;
        this.properties = properties;
        this.extended = extended;
    }

    private EntityManager getEntityManager() {
        return registry.getEntityManager(entityManagerFactory, properties, extended, unitName);
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
            logger.debug("Closed EntityManager(unit=" + unitName + ", hashCode=" + entityManager.hashCode() + ")");
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
        if (!extended && !isTransactionActive()) {
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
        throw new IllegalStateException("A JTA EntityManager can not use the EntityTransaction API.  See JPA 1.0 section 5.5");
    }
    
    // JPA 2.0
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createNamedQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.createNamedQuery(name, resultClass);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createQuery(javax.persistence.criteria.CriteriaQuery)
     */
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.createQuery(criteriaQuery);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.createQuery(qlString, resultClass);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#detach(java.lang.Object)
     */
    public void detach(Object entity) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.detach(entity);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, java.util.Map)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.find(entityClass, primaryKey, properties);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.find(entityClass, entityManager, lockMode);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.find(entityClass, entityManager, lockMode, properties);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getEntityManagerFactory()
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getLockMode(java.lang.Object)
     */
    public LockModeType getLockMode(Object entity) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.getLockMode(entity);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getMetamodel()
     */
    public Metamodel getMetamodel() {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.getMetamodel();
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getProperties()
     */
    public Map<String, Object> getProperties() {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.getProperties();
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getCriteriaBuilder()
     */
    public CriteriaBuilder getCriteriaBuilder() {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.getCriteriaBuilder();
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.lock(entityManager, lockMode, properties);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, java.util.Map)
     */
    public void refresh(Object entity, Map<String, Object> properties) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.refresh(entityManager, properties);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType)
     */
    public void refresh(Object entity, LockModeType lockMode) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.refresh(entityManager, lockMode);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.refresh(entityManager, lockMode, properties);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String name, Object value) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.setProperty(name, value);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> cls) {
        EntityManager entityManager = getEntityManager();
        try {
            return entityManager.unwrap(cls);
        } finally {
            closeIfNoTx(entityManager);
        }
    }
}

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

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
public class JtaEntityManager implements EntityManager, Serializable {

    private static final Logger baseLogger = Logger.getInstance(LogCategory.OPENEJB.createChild("persistence"), JtaEntityManager.class);

    private final JtaEntityManagerRegistry registry;
    private final EntityManagerFactory entityManagerFactory;
    private final Map properties;
    private final boolean extended;
    private final String unitName;
    private final Logger logger;

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
        logger = (unitName == null) ? baseLogger : baseLogger.getChildLogger(unitName);
    }

    EntityManager getEntityManager() {
        return registry.getEntityManager(entityManagerFactory, properties, extended, unitName);
    }

    boolean isTransactionActive() {
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
    void closeIfNoTx(EntityManager entityManager) {
        if (!extended && !isTransactionActive()) {
            entityManager.close();
            logger.debug("Closed EntityManager(unit=" + unitName + ", hashCode=" + entityManager.hashCode() + ")");
        }
    }

    public EntityManager getDelegate() {
        final Timer timer = Op.getDelegate.start(this);
        try {
            EntityManager em = getEntityManager();
            em.getDelegate(); // exception if not open etc... to respect the spec
            return em;
        } finally {
            timer.stop();
        }
    }

    public void persist(Object entity) {
        assertTransactionActive();
        final Timer timer = Op.persist.start(this);
        try {
            getEntityManager().persist(entity);
        } finally {
            timer.stop();
        }
    }

    public <T>T merge(T entity) {
        assertTransactionActive();
        final Timer timer = Op.merge.start(this);
        try {
            return getEntityManager().merge(entity);
        } finally {
            timer.stop();
        }
    }

    public void remove(Object entity) {
        assertTransactionActive();
        final Timer timer = Op.remove.start(this);
        try {
            getEntityManager().remove(entity);
        } finally {
            timer.stop();
        }
    }

    public <T>T find(Class<T> entityClass, Object primaryKey) {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.find.start(this);
            try {
                return entityManager.find(entityClass, primaryKey);
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public <T>T getReference(Class<T> entityClass, Object primaryKey) {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getReference.start(this);
            try {
                return entityManager.getReference(entityClass, primaryKey);
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public void flush() {
        assertTransactionActive();
        final Timer timer = Op.flush.start(this);
        try {
            getEntityManager().flush();
        } finally {
            timer.stop();
        }
    }

    public void setFlushMode(FlushModeType flushMode) {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.setFlushMode.start(this);
            try {
                entityManager.setFlushMode(flushMode);
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public FlushModeType getFlushMode() {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getFlushMode.start(this);
            try {
                return entityManager.getFlushMode();
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public void lock(Object entity, LockModeType lockMode) {
        assertTransactionActive();
        final Timer timer = Op.lock.start(this);
        try {
            getEntityManager().lock(entity, lockMode);
        } finally {
            timer.stop();
        }
    }

    public void refresh(Object entity) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this);
        try {
            getEntityManager().refresh(entity);
        } finally {
            timer.stop();
        }
    }

    public void clear() {
        if (!extended && !isTransactionActive()) {
            return;
        }
        final Timer timer = Op.clear.start(this);
        try {
            getEntityManager().clear();
        } finally {
            timer.stop();
        }
    }

    public boolean contains(Object entity) {
        final Timer timer = Op.contains.start(this);
        try {
            return !(!extended && !isTransactionActive()) && getEntityManager().contains(entity);
        } finally {
            timer.stop();
        }
    }

    public Query createQuery(String qlString) {
        final Timer timer = Op.createQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            Query query = entityManager.createQuery(qlString);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }

    public Query createNamedQuery(String name) {
        final Timer timer = Op.createNamedQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            Query query = entityManager.createNamedQuery(name);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }

    public Query createNativeQuery(String sqlString) {
        final Timer timer = Op.createNativeQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            Query query = entityManager.createNativeQuery(sqlString);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        final Timer timer = Op.createNativeQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            Query query = entityManager.createNativeQuery(sqlString, resultClass);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        final Timer timer = Op.createNativeQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            Query query = entityManager.createNativeQuery(sqlString, resultSetMapping);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }

    private Query proxyIfNoTx(EntityManager entityManager, Query query) {
        if (!extended && !isTransactionActive()) {
            return new JtaQuery(entityManager, this, query);
        }
        return query;
    }
    
    private <T> TypedQuery<T> proxyIfNoTx(EntityManager entityManager, TypedQuery<T> query) {
        if (!extended && !isTransactionActive()) {
            return new JtaTypedQuery<T>(entityManager, this, query);
        }
        return query;
    }

    public void joinTransaction() {
        final Timer timer = Op.joinTransaction.start(this);
        try {
            getDelegate().joinTransaction();
        } finally {
            timer.stop();
        }
    }

    /**
     * close throws an IllegalStateException if the em is container managed otherwise (emf.newEM()) it is delegated to the user
     */
    public void close() {
        throw new IllegalStateException("PersistenceUnit(name=" + unitName + ") - entityManager.close() call - See JPA 2.0 section 7.9.1", new Exception().fillInStackTrace());
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
        final Timer timer = Op.createNamedQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            TypedQuery<T> query = entityManager.createNamedQuery(name, resultClass);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createQuery(javax.persistence.criteria.CriteriaQuery)
     */
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        final Timer timer = Op.createQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            TypedQuery<T> query = entityManager.createQuery(criteriaQuery);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#createQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        final Timer timer = Op.createQuery.start(this);
        try {
            EntityManager entityManager = getEntityManager();
            TypedQuery<T> query = entityManager.createQuery(qlString, resultClass);
            return proxyIfNoTx(entityManager, query);
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#detach(java.lang.Object)
     */
    public void detach(Object entity) {
        final Timer timer = Op.detach.start(this);
        try {
            if (!extended && isTransactionActive()) {
                getEntityManager().detach(entity);
            }
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#find(java.lang.Class, java.lang.Object, java.util.Map)
     */
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.find.start(this);
            try {
                return entityManager.find(entityClass, primaryKey, properties);
            } finally {
                timer.stop();
            }
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
            final Timer timer = Op.find.start(this);
            try {
                return entityManager.find(entityClass, primaryKey, lockMode);
            } finally {
                timer.stop();
            }
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
            final Timer timer = Op.find.start(this);
            try {
                return entityManager.find(entityClass, primaryKey, lockMode, properties);
            } finally {
                timer.stop();
            }
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
        assertTransactionActive();
        final Timer timer = Op.getLockMode.start(this);
        try {
            return getEntityManager().getLockMode(entity);
        } finally {
            timer.stop();
        }        
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#getMetamodel()
     */
    public Metamodel getMetamodel() {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getMetamodel.start(this);
            try {
                return entityManager.getMetamodel();
            } finally {
                timer.stop();
            }
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
            final Timer timer = Op.getProperties.start(this);
            try {
                return entityManager.getProperties();
            } finally {
                timer.stop();
            }
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
            final Timer timer = Op.getCriteriaBuilder.start(this);
            try {
                return entityManager.getCriteriaBuilder();
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#lock(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        assertTransactionActive();
        final Timer timer = Op.lock.start(this);
        try {
            getEntityManager().lock(entity, lockMode, properties);
        } finally {
            timer.stop();
        }        
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, java.util.Map)
     */
    public void refresh(Object entity, Map<String, Object> properties) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this);
        try {
            getEntityManager().refresh(entity, properties);
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType)
     */
    public void refresh(Object entity, LockModeType lockMode) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this);
        try {
            getEntityManager().refresh(entity, lockMode);
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#refresh(java.lang.Object, javax.persistence.LockModeType, java.util.Map)
     */
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this);
        try {
            getEntityManager().refresh(entity, lockMode, properties);
        } finally {
            timer.stop();
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String name, Object value) {
        EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.setProperty.start(this);
            try {
                entityManager.setProperty(name, value);
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }
    /* (non-Javadoc)
     * @see javax.persistence.EntityManager#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

    public static class Timer {
        private final long start = System.nanoTime();
        private final Op operation;
        private final JtaEntityManager em;

        public Timer(Op operation, JtaEntityManager em) {
            this.operation = operation;
            this.em = em;
        }

        public void stop() {
            if (!em.logger.isDebugEnabled()) return;

            final long time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);

            em.logger.debug("PersistenceUnit(name=" + em.unitName + ") - entityManager." + operation + " - " + time + "ms");
        }
    }

    private static enum Op {
        clear, close, contains, createNamedQuery, createNativeQuery, createQuery, find, flush, getFlushMode, getReference, getTransaction, lock, merge, refresh, remove, setFlushMode, persist, detach, getLockMode, unwrap, setProperty, getCriteriaBuilder, getProperties, getMetamodel, joinTransaction, getDelegate;

        public Timer start(JtaEntityManager em) {
            return new Timer(this, em);
        }
    }


    protected Object writeReplace() throws ObjectStreamException {
        return new IntraVmArtifact(this, true);
    }
}

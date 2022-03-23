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

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.core.ivm.IntraVmArtifact;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.reflection.Reflections;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.TransactionRequiredException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private static final Method CREATE_NAMED_QUERY_FROM_NAME = Reflections.findMethod("createNamedQuery", EntityManager.class, String.class);
    private static final Method CREATE_QUERY_FROM_NAME = Reflections.findMethod("createQuery", EntityManager.class, String.class);
    private static final Method CREATE_NATIVE_FROM_NAME = Reflections.findMethod("createNativeQuery", EntityManager.class, String.class);
    private static final Method CREATE_NAMED_QUERY_FROM_NAME_CLASS = Reflections.findMethod("createNamedQuery", EntityManager.class, String.class, Class.class);
    private static final Method CREATE_QUERY_FROM_NAME_CLASS = Reflections.findMethod("createQuery", EntityManager.class, String.class, Class.class);
    private static final Method CREATE_QUERY_FROM_CRITERIA = Reflections.findMethod("createQuery", EntityManager.class, CriteriaQuery.class);
    private static final Method CREATE_NATIVE_FROM_NAME_CLASS = Reflections.findMethod("createNativeQuery", EntityManager.class, String.class, Class.class);
    private static final Method CREATE_NATIVE_FROM_NAME_MAPPING = Reflections.findMethod("createNativeQuery", EntityManager.class, String.class, String.class);
    private static final ConcurrentMap<Class<?>, Boolean> IS_JPA21 = new ConcurrentHashMap<>();

    private final JtaEntityManagerRegistry registry;
    private final EntityManagerFactory entityManagerFactory;
    private final Map properties;
    private final boolean extended;
    private final SynchronizationType synchronizationType;
    private final String unitName;
    private final Logger logger;
    private final boolean wrapNoTxQueries;
    private final boolean timer;

    public JtaEntityManager(final JtaEntityManagerRegistry registry, final EntityManagerFactory entityManagerFactory,
                            final Map properties, final String unitName, final String synchronizationType) {
        this(unitName, registry, entityManagerFactory, properties, false, synchronizationType);
    }

    public JtaEntityManager(final String unitName, final JtaEntityManagerRegistry registry, final EntityManagerFactory entityManagerFactory,
                            final Map properties, final boolean extended, final String synchronizationType) {
        if (registry == null) {
            throw new NullPointerException("registry is null");
        }
        if (entityManagerFactory == null) {
            throw new NullPointerException("entityManagerFactory is null");
        }
        this.unitName = unitName;
        this.registry = registry;
        this.entityManagerFactory = entityManagerFactory;
        this.properties = properties;
        this.extended = extended;
        this.synchronizationType = !isJPA21(entityManagerFactory) || synchronizationType == null ?
                null : SynchronizationType.valueOf(synchronizationType.toUpperCase(Locale.ENGLISH));
        final String globalTimerConfig = SystemInstance.get().getProperty("openejb.jpa.timer");
        final Object localTimerConfig = properties == null ? null : properties.get("openejb.jpa.timer");
        this.timer = localTimerConfig == null ? (globalTimerConfig == null || Boolean.parseBoolean(globalTimerConfig)) : Boolean.parseBoolean(localTimerConfig.toString());
        logger = unitName == null ? baseLogger : baseLogger.getChildLogger(unitName);
        final String wrapConfig = ReloadableEntityManagerFactory.class.isInstance(entityManagerFactory) ?
                ReloadableEntityManagerFactory.class.cast(entityManagerFactory).getUnitProperties().getProperty("openejb.jpa.query.wrap-no-tx", "true") : "true";
        this.wrapNoTxQueries = wrapConfig == null || "true".equalsIgnoreCase(wrapConfig);
    }

    public static boolean isJPA21(final EntityManagerFactory entityManagerFactory) {
        return ReloadableEntityManagerFactory.class.isInstance(entityManagerFactory) ?
                hasMethod(
                        ReloadableEntityManagerFactory.class.cast(entityManagerFactory).getEntityManagerFactoryCallable().getProvider(),
                        "generateSchema", String.class, Map.class)
                : hasMethod(entityManagerFactory.getClass(), "createEntityManager", SynchronizationType.class);
    }

    private static boolean hasMethod(final Class<?> objectClass, final String name, final Class<?>... params) {
        try {
            Boolean result = IS_JPA21.get(objectClass);
            if (result == null) {
                result = !Modifier.isAbstract(objectClass.getMethod(name, params).getModifiers());
                if (objectClass.getClassLoader() == JtaEntityManager.class.getClassLoader()) {
                    IS_JPA21.putIfAbsent(objectClass, result);
                } // else don't cache to not leak
            }
            return result;
        } catch (final Throwable e) {
            return false;
        }
    }

    EntityManager getEntityManager() {
        return registry.getEntityManager(entityManagerFactory, properties, extended, unitName, synchronizationType);
    }

    boolean isTransactionActive() {
        return registry.isTransactionActive();
    }

    /**
     * This method assures that a non-extended entity managers has an acive transaction.  This is
     * required for some operations on the entity manager.
     *
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
     *
     * @param entityManager the entity manager to close if non-extended and a transaction is not active
     */
    void closeIfNoTx(final EntityManager entityManager) {
        if (!extended && !isTransactionActive()) {
            entityManager.close();
            logger.debug("Closed EntityManager(unit=" + unitName + ", hashCode=" + entityManager.hashCode() + ")");
        }
    }

    public EntityManager getDelegate() {
        final Timer timer = Op.getDelegate.start(this.timer, this);
        try {
            final EntityManager em = getEntityManager();
            em.getDelegate(); // exception if not open etc... to respect the spec
            return em;
        } finally {
            timer.stop();
        }
    }

    public void persist(final Object entity) {
        assertTransactionActive();
        final Timer timer = Op.persist.start(this.timer, this);
        try {
            getEntityManager().persist(entity);
        } finally {
            timer.stop();
        }
    }

    public <T> T merge(final T entity) {
        assertTransactionActive();
        final Timer timer = Op.merge.start(this.timer, this);
        try {
            return getEntityManager().merge(entity);
        } finally {
            timer.stop();
        }
    }

    public void remove(final Object entity) {
        assertTransactionActive();
        final Timer timer = Op.remove.start(this.timer, this);
        try {
            getEntityManager().remove(entity);
        } finally {
            timer.stop();
        }
    }

    public <T> T find(final Class<T> entityClass, final Object primaryKey) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.find.start(this.timer, this);
            try {
                return entityManager.find(entityClass, primaryKey);
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public <T> T getReference(final Class<T> entityClass, final Object primaryKey) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getReference.start(this.timer, this);
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
        final Timer timer = Op.flush.start(this.timer, this);
        try {
            getEntityManager().flush();
        } finally {
            timer.stop();
        }
    }

    public void setFlushMode(final FlushModeType flushMode) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.setFlushMode.start(this.timer, this);
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
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getFlushMode.start(this.timer, this);
            try {
                return entityManager.getFlushMode();
            } finally {
                timer.stop();
            }
        } finally {
            closeIfNoTx(entityManager);
        }
    }

    public void lock(final Object entity, final LockModeType lockMode) {
        assertTransactionActive();
        final Timer timer = Op.lock.start(this.timer, this);
        try {
            getEntityManager().lock(entity, lockMode);
        } finally {
            timer.stop();
        }
    }

    public void refresh(final Object entity) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this.timer, this);
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
        final Timer timer = Op.clear.start(this.timer, this);
        try {
            getEntityManager().clear();
        } finally {
            timer.stop();
        }
    }

    public boolean contains(final Object entity) {
        final Timer timer = Op.contains.start(this.timer, this);
        try {
            return !(!extended && !isTransactionActive()) && getEntityManager().contains(entity);
        } finally {
            timer.stop();
        }
    }

    public Query createQuery(final String qlString) {
        final Timer timer = Op.createQuery.start(this.timer, this);
        try {
            return proxyIfNoTx(CREATE_QUERY_FROM_NAME, qlString);
        } finally {
            timer.stop();
        }
    }

    public Query createNamedQuery(final String name) {
        final Timer timer = Op.createNamedQuery.start(this.timer, this);
        try {
            return proxyIfNoTx(CREATE_NAMED_QUERY_FROM_NAME, name);
        } finally {
            timer.stop();
        }
    }

    public Query createNativeQuery(final String sqlString) {
        final Timer timer = Op.createNativeQuery.start(this.timer, this);
        try {
            return proxyIfNoTx(CREATE_NATIVE_FROM_NAME, sqlString);
        } finally {
            timer.stop();
        }
    }

    public Query createNativeQuery(final String sqlString, final Class resultClass) {
        final Timer timer = Op.createNativeQuery.start(this.timer, this);
        try {
            return proxyIfNoTx(CREATE_NATIVE_FROM_NAME_CLASS, sqlString, resultClass);
        } finally {
            timer.stop();
        }
    }

    public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
        final Timer timer = Op.createNativeQuery.start(this.timer, this);
        try {
            return proxyIfNoTx(CREATE_NATIVE_FROM_NAME_MAPPING, sqlString, resultSetMapping);
        } finally {
            timer.stop();
        }
    }

    private Query proxyIfNoTx(final Method method, final Object... args) {
        if (wrapNoTxQueries && !extended && !isTransactionActive()) {
            return new JtaQuery(getEntityManager(), this, method, args);
        }
        return createQuery(Query.class, getEntityManager(), method, args);
    }

    private <T> TypedQuery<T> typedProxyIfNoTx(final Method method, final Object... args) {
        if (wrapNoTxQueries && !extended && !isTransactionActive()) {
            return new JtaTypedQuery<>(getEntityManager(), this, method, args);
        }
        return createQuery(TypedQuery.class, getEntityManager(), method, args);
    }

    <T> T createQuery(final Class<T> expected, final EntityManager entityManager, final Method method, final Object... args) {
        try {
            return expected.cast(method.invoke(entityManager, args));
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            final Throwable t = e.getCause();
            if (RuntimeException.class.isInstance(t)) {
                throw RuntimeException.class.cast(t);
            }
            throw new OpenEJBRuntimeException(t.getMessage(), t);
        }
    }

    public void joinTransaction() {
        final Timer timer = Op.joinTransaction.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#createNamedQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) {
        final Timer timer = Op.createNamedQuery.start(this.timer, this);
        try {
            return typedProxyIfNoTx(CREATE_NAMED_QUERY_FROM_NAME_CLASS, name, resultClass);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#createQuery(jakarta.persistence.criteria.CriteriaQuery)
     */
    public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) {
        final Timer timer = Op.createQuery.start(this.timer, this);
        try {
            return typedProxyIfNoTx(CREATE_QUERY_FROM_CRITERIA, criteriaQuery);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#createQuery(java.lang.String, java.lang.Class)
     */
    public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) {
        final Timer timer = Op.createQuery.start(this.timer, this);
        try {
            return typedProxyIfNoTx(CREATE_QUERY_FROM_NAME_CLASS, qlString, resultClass);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#detach(java.lang.Object)
     */
    public void detach(final Object entity) {
        final Timer timer = Op.detach.start(this.timer, this);
        try {
            if (!extended && isTransactionActive()) {
                getEntityManager().detach(entity);
            }
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#find(java.lang.Class, java.lang.Object, java.util.Map)
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final Map<String, Object> properties) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.find.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#find(java.lang.Class, java.lang.Object, jakarta.persistence.LockModeType)
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.find.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#find(java.lang.Class, java.lang.Object, jakarta.persistence.LockModeType, java.util.Map)
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode, final Map<String, Object> properties) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.find.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#getEntityManagerFactory()
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#getLockMode(java.lang.Object)
     */
    public LockModeType getLockMode(final Object entity) {
        assertTransactionActive();
        final Timer timer = Op.getLockMode.start(this.timer, this);
        try {
            return getEntityManager().getLockMode(entity);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#getMetamodel()
     */
    public Metamodel getMetamodel() {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getMetamodel.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#getProperties()
     */
    public Map<String, Object> getProperties() {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getProperties.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#getCriteriaBuilder()
     */
    public CriteriaBuilder getCriteriaBuilder() {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.getCriteriaBuilder.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#lock(java.lang.Object, jakarta.persistence.LockModeType, java.util.Map)
     */
    public void lock(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
        assertTransactionActive();
        final Timer timer = Op.lock.start(this.timer, this);
        try {
            getEntityManager().lock(entity, lockMode, properties);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#refresh(java.lang.Object, java.util.Map)
     */
    public void refresh(final Object entity, final Map<String, Object> properties) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this.timer, this);
        try {
            getEntityManager().refresh(entity, properties);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#refresh(java.lang.Object, jakarta.persistence.LockModeType)
     */
    public void refresh(final Object entity, final LockModeType lockMode) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this.timer, this);
        try {
            getEntityManager().refresh(entity, lockMode);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#refresh(java.lang.Object, jakarta.persistence.LockModeType, java.util.Map)
     */
    public void refresh(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
        assertTransactionActive();
        final Timer timer = Op.refresh.start(this.timer, this);
        try {
            getEntityManager().refresh(entity, lockMode, properties);
        } finally {
            timer.stop();
        }
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.EntityManager#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(final String name, final Object value) {
        final EntityManager entityManager = getEntityManager();
        try {
            final Timer timer = Op.setProperty.start(this.timer, this);
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
     * @see jakarta.persistence.EntityManager#unwrap(java.lang.Class)
     */
    public <T> T unwrap(final Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

    public static class Timer {
        private final long start = System.nanoTime();
        private final Op operation;
        private final JtaEntityManager em;

        public Timer(final Op operation, final JtaEntityManager em) {
            this.operation = operation;
            this.em = em;
        }

        public void stop() {
            if (!em.logger.isDebugEnabled()) {
                return;
            }

            final long time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);

            em.logger.debug("PersistenceUnit(name=" + em.unitName + ") - entityManager." + operation + " - " + time + "ms");
        }
    }

    private enum Op {
        clear, close, contains, createNamedQuery, createNativeQuery, createQuery, find, flush, getFlushMode, getReference, getTransaction, lock, merge, refresh, remove, setFlushMode, persist, detach, getLockMode, unwrap, setProperty, getCriteriaBuilder, getProperties, getMetamodel, joinTransaction, getDelegate,
        // JPA 2.1
        createNamedStoredProcedureQuery, createStoredProcedureQuery, createEntityGraph, getEntityGraph, getEntityGraphs, isJoinedToTransaction;

        private static final Timer NOOP = new Timer(null, null) {
            @Override
            public void stop() {
                // no-op
            }
        };

        public Timer start(final boolean timer, final JtaEntityManager em) {
            return timer ? new Timer(this, em) : NOOP;
        }
    }


    protected Object writeReplace() throws ObjectStreamException {
        return new IntraVmArtifact(this, true);
    }

    // TODO: JPA 2.1 methods doesn't have yet proxying

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(final String name) {
        final Timer timer = Op.createNamedStoredProcedureQuery.start(this.timer, this);
        try {
            return getEntityManager().createNamedStoredProcedureQuery(name);
        } finally {
            timer.stop();
        }
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(final String procedureName) {
        final Timer timer = Op.createNamedStoredProcedureQuery.start(this.timer, this);
        try {
            return getEntityManager().createStoredProcedureQuery(procedureName);
        } finally {
            timer.stop();
        }
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(final String procedureName, final Class... resultClasses) {
        final Timer timer = Op.createStoredProcedureQuery.start(this.timer, this);
        try {
            return getEntityManager().createStoredProcedureQuery(procedureName, resultClasses);
        } finally {
            timer.stop();
        }
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(final String procedureName, final String... resultSetMappings) {
        final Timer timer = Op.createStoredProcedureQuery.start(this.timer, this);
        try {
            return getEntityManager().createStoredProcedureQuery(procedureName, resultSetMappings);
        } finally {
            timer.stop();
        }
    }

    @Override
    public Query createQuery(final CriteriaUpdate updateQuery) {
        final Timer timer = Op.createQuery.start(this.timer, this);
        try {
            return getEntityManager().createQuery(updateQuery);
        } finally {
            timer.stop();
        }
    }

    @Override
    public Query createQuery(final CriteriaDelete deleteQuery) {
        final Timer timer = Op.createQuery.start(this.timer, this);
        try {
            return getEntityManager().createQuery(deleteQuery);
        } finally {
            timer.stop();
        }
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(final Class<T> rootType) {
        final Timer timer = Op.createEntityGraph.start(this.timer, this);
        try {
            return getEntityManager().createEntityGraph(rootType);
        } finally {
            timer.stop();
        }
    }

    @Override
    public EntityGraph<?> createEntityGraph(final String graphName) {
        final Timer timer = Op.createEntityGraph.start(this.timer, this);
        try {
            return getEntityManager().createEntityGraph(graphName);
        } finally {
            timer.stop();
        }
    }

    @Override
    public EntityGraph<?> getEntityGraph(final String graphName) {
        final Timer timer = Op.getEntityGraph.start(this.timer, this);
        try {
            return getEntityManager().getEntityGraph(graphName);
        } finally {
            timer.stop();
        }
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(final Class<T> entityClass) {
        final Timer timer = Op.getEntityGraphs.start(this.timer, this);
        try {
            return getEntityManager().getEntityGraphs(entityClass);
        } finally {
            timer.stop();
        }
    }

    @Override
    public boolean isJoinedToTransaction() {
        final Timer timer = Op.isJoinedToTransaction.start(this.timer, this);
        try {
            return synchronizationType == null /* JPA < 2.1 */ || getEntityManager().isJoinedToTransaction();
        } finally {
            timer.stop();
        }
    }
}

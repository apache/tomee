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

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.Map;

public class QueryLogEntityManager implements EntityManager {
    private final EntityManager delegate;
    private final String level;

    public QueryLogEntityManager(final EntityManager entityManager, final String level) {
        delegate = entityManager;
        this.level = level;
    }

    @Override
    public void persist(final Object entity) {
        delegate.persist(entity);
    }

    @Override
    public <T> T merge(final T entity) {
        return delegate.merge(entity);
    }

    @Override
    public void remove(final Object entity) {
        delegate.remove(entity);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey) {
        return delegate.find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final Map<String, Object> properties) {
        return delegate.find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode) {
        return delegate.find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode, final Map<String, Object> properties) {
        return delegate.find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, FindOption... options) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public <T> T find(EntityGraph<T> entityGraph, Object primaryKey, FindOption... options) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public <T> T getReference(final Class<T> entityClass, final Object primaryKey) {
        return delegate.getReference(entityClass, primaryKey);
    }

    @Override
    public <T> T getReference(T entity) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void setFlushMode(final FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    @Override
    public void lock(final Object entity, final LockModeType lockMode) {
        delegate.lock(entity, lockMode);
    }

    @Override
    public void lock(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
        delegate.lock(entity, lockMode, properties);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, LockOption... options) {

    }

    @Override
    public void refresh(final Object entity) {
        delegate.refresh(entity);
    }

    @Override
    public void refresh(final Object entity, final Map<String, Object> properties) {
        delegate.refresh(entity, properties);
    }

    @Override
    public void refresh(final Object entity, final LockModeType lockMode) {
        delegate.refresh(entity, lockMode);
    }

    @Override
    public void refresh(final Object entity, final LockModeType lockMode, final Map<String, Object> properties) {
        delegate.refresh(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity, RefreshOption... options) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void detach(final Object entity) {
        delegate.detach(entity);
    }

    @Override
    public boolean contains(final Object entity) {
        return delegate.contains(entity);
    }

    @Override
    public LockModeType getLockMode(final Object entity) {
        return delegate.getLockMode(entity);
    }

    @Override
    public void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public void setCacheStoreMode(CacheStoreMode cacheStoreMode) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public CacheRetrieveMode getCacheRetrieveMode() {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public CacheStoreMode getCacheStoreMode() {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public void setProperty(final String propertyName, final Object value) {
        delegate.setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Query createQuery(final String qlString) {
        return delegate.createQuery(qlString);
    }

    @Override
    public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) {
        return new CriteriaLogQuery(delegate.createQuery(criteriaQuery), level);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public Query createQuery(final CriteriaUpdate updateQuery) {
        return delegate.createQuery(updateQuery);
    }

    @Override
    public Query createQuery(final CriteriaDelete deleteQuery) {
        return delegate.createQuery(deleteQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) {
        return delegate.createQuery(qlString, resultClass);
    }

    @Override
    public Query createNamedQuery(final String name) {
        return delegate.createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) {
        return delegate.createNamedQuery(name, resultClass);
    }

    @Override
    public <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public Query createNativeQuery(final String sqlString) {
        return delegate.createNativeQuery(sqlString);
    }

    @Override
    public Query createNativeQuery(final String sqlString, final Class resultClass) {
        return delegate.createNativeQuery(sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
        return delegate.createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(final String name) {
        return delegate.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(final String procedureName) {
        return delegate.createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(final String procedureName, final Class... resultClasses) {
        return delegate.createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(final String procedureName, final String... resultSetMappings) {
        return delegate.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        delegate.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return delegate.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(final Class<T> cls) {
        return delegate.unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return delegate.getDelegate();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return delegate.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(final Class<T> rootType) {
        return delegate.createEntityGraph(rootType);
    }

    @Override
    public EntityGraph<?> createEntityGraph(final String graphName) {
        return delegate.createEntityGraph(graphName);
    }

    @Override
    public EntityGraph<?> getEntityGraph(final String graphName) {
        return delegate.getEntityGraph(graphName);
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(final Class<T> entityClass) {
        return delegate.getEntityGraphs(entityClass);
    }

    @Override
    public <C> void runWithConnection(ConnectionConsumer<C> action) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }

    @Override
    public <C, T> T callWithConnection(ConnectionFunction<C, T> function) {
        //TODO TomEE 11 - JPA 3.2
        throw new UnsupportedOperationException("TomEE does not support JPA 3.2 yet");
    }
}

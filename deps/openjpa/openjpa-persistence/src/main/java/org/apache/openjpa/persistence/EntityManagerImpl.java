/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockScope;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.kernel.AbstractBrokerFactory;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.DataCacheRetrieveMode;
import org.apache.openjpa.kernel.DataCacheStoreMode;
import org.apache.openjpa.kernel.DelegatingBroker;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.FindCallbacks;
import org.apache.openjpa.kernel.OpCallbacks;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.kernel.QueryFlushModes;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.persistence.criteria.CriteriaBuilderImpl;
import org.apache.openjpa.persistence.criteria.OpenJPACriteriaBuilder;
import org.apache.openjpa.persistence.criteria.OpenJPACriteriaQuery;
import org.apache.openjpa.persistence.validation.ValidationUtils;
import org.apache.openjpa.util.ExceptionInfo;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.RuntimeExceptionTranslator;
import org.apache.openjpa.util.UserException;

import serp.util.Strings;

/**
 * Implementation of {@link EntityManager} interface.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @nojavadoc
 */
public class EntityManagerImpl
    implements OpenJPAEntityManagerSPI, Externalizable,
    FindCallbacks, OpCallbacks, Closeable, OpenJPAEntityTransaction {

    private static final Localizer _loc = Localizer.forPackage(EntityManagerImpl.class);
    private static final Object[] EMPTY_OBJECTS = new Object[0];
    
    private static final String GET_LOCK_MODE = "getLockMode";
    private static final String LOCK = "lock";
    private static final String REFRESH = "refresh";

    private DelegatingBroker _broker;
    private EntityManagerFactoryImpl _emf;
    private Map<FetchConfiguration,FetchPlan> _plans = new IdentityHashMap<FetchConfiguration,FetchPlan>(1);
    protected RuntimeExceptionTranslator _ret = PersistenceExceptions.getRollbackTranslator(this);
    private boolean _convertPositionalParams = false;
    
    public EntityManagerImpl() {
        // for Externalizable
    }

    /**
     * Constructor; supply factory and delegate.
     */
    public EntityManagerImpl(EntityManagerFactoryImpl factory, Broker broker) {
        initialize(factory, broker);
    }

    private void initialize(EntityManagerFactoryImpl factory, Broker broker) {
        _emf = factory;
        _broker = new DelegatingBroker(broker, _ret);
        _broker.setImplicitBehavior(this, _ret);
        _broker.putUserObject(JPAFacadeHelper.EM_KEY, this);
        _convertPositionalParams =
            factory.getConfiguration().getCompatibilityInstance().getConvertPositionalParametersToNamed();
    }

    /**
     * Broker delegate.
     */
    public Broker getBroker() {
        return _broker.getDelegate();
    }

    public OpenJPAEntityManagerFactory getEntityManagerFactory() {
        return _emf;
    }

    public OpenJPAConfiguration getConfiguration() {
        return _broker.getConfiguration();
    }

    public FetchPlan getFetchPlan() {
        assertNotCloseInvoked();
        _broker.lock();
        try {
            FetchConfiguration fc = _broker.getFetchConfiguration();
            FetchPlan fp = _plans.get(fc);
            if (fp == null) {
                fp = _emf.toFetchPlan(_broker, fc);
                _plans.put(fc, fp);
            }
            return fp;
        } finally {
            _broker.unlock();
        }
    }

    public FetchPlan pushFetchPlan() {
		return pushFetchPlan(null);
    }

    public FetchPlan pushFetchPlan(FetchConfiguration fc) {
        assertNotCloseInvoked();
        _broker.lock();
        try {
            _broker.pushFetchConfiguration(fc);
            return getFetchPlan();
        } finally {
            _broker.unlock();
        }
    }

    public void popFetchPlan() {
        assertNotCloseInvoked();
        _broker.lock();
        try {
            _plans.remove(_broker.getFetchConfiguration());
            _broker.popFetchConfiguration();
        } finally {
            _broker.unlock();
        }
    }

    public ConnectionRetainMode getConnectionRetainMode() {
        return ConnectionRetainMode.fromKernelConstant(
            _broker.getConnectionRetainMode());
    }

    public boolean isTransactionManaged() {
        return _broker.isManaged();
    }

    public boolean isManaged() {
        return _broker.isManaged();
    }

    public ManagedRuntime getManagedRuntime() {
        return _broker.getManagedRuntime();
    }

    public boolean getSyncWithManagedTransactions() {
        return _broker.getSyncWithManagedTransactions();
    }

    public void setSyncWithManagedTransactions(boolean sync) {
        assertNotCloseInvoked();
        _broker.setSyncWithManagedTransactions(sync);
    }

    public ClassLoader getClassLoader() {
        return _broker.getClassLoader();
    }

    public String getConnectionUserName() {
        return _broker.getConnectionUserName();
    }

    public String getConnectionPassword() {
        return _broker.getConnectionPassword();
    }

    public boolean getMultithreaded() {
        return _broker.getMultithreaded();
    }

    public void setMultithreaded(boolean multithreaded) {
        assertNotCloseInvoked();
        _broker.setMultithreaded(multithreaded);
    }

    public boolean getIgnoreChanges() {
        return _broker.getIgnoreChanges();
    }

    public void setIgnoreChanges(boolean val) {
        assertNotCloseInvoked();
        _broker.setIgnoreChanges(val);
    }

    public boolean getNontransactionalRead() {
        return _broker.getNontransactionalRead();
    }

    public void setNontransactionalRead(boolean val) {
        assertNotCloseInvoked();
        _broker.setNontransactionalRead(val);
    }

    public boolean getNontransactionalWrite() {
        return _broker.getNontransactionalWrite();
    }

    public void setNontransactionalWrite(boolean val) {
        assertNotCloseInvoked();
        _broker.setNontransactionalWrite(val);
    }

    public boolean getOptimistic() {
        return _broker.getOptimistic();
    }

    public void setOptimistic(boolean val) {
        assertNotCloseInvoked();
        _broker.setOptimistic(val);
    }

    public RestoreStateType getRestoreState() {
        return RestoreStateType.fromKernelConstant(_broker.getRestoreState());
    }

    public void setRestoreState(RestoreStateType val) {
        assertNotCloseInvoked();
        _broker.setRestoreState(val.toKernelConstant());
    }

    public void setRestoreState(int restore) {
        assertNotCloseInvoked();
        _broker.setRestoreState(restore);
    }

    public boolean getRetainState() {
        return _broker.getRetainState();
    }

    public void setRetainState(boolean val) {
        assertNotCloseInvoked();
        _broker.setRetainState(val);
    }

    public AutoClearType getAutoClear() {
        return AutoClearType.fromKernelConstant(_broker.getAutoClear());
    }

    public void setAutoClear(AutoClearType val) {
        assertNotCloseInvoked();
        _broker.setAutoClear(val.toKernelConstant());
    }

    public void setAutoClear(int autoClear) {
        assertNotCloseInvoked();
        _broker.setAutoClear(autoClear);
    }

    public DetachStateType getDetachState() {
        return DetachStateType.fromKernelConstant(_broker.getDetachState());
    }

    public void setDetachState(DetachStateType type) {
        assertNotCloseInvoked();
        _broker.setDetachState(type.toKernelConstant());
    }

    public void setDetachState(int detach) {
        assertNotCloseInvoked();
        _broker.setDetachState(detach);
    }

    public EnumSet<AutoDetachType> getAutoDetach() {
        return AutoDetachType.toEnumSet(_broker.getAutoDetach());
    }

    public void setAutoDetach(AutoDetachType flag) {
        assertNotCloseInvoked();
        _broker.setAutoDetach(AutoDetachType.fromEnumSet(EnumSet.of(flag)));
    }

    public void setAutoDetach(EnumSet<AutoDetachType> flags) {
        assertNotCloseInvoked();
        _broker.setAutoDetach(AutoDetachType.fromEnumSet(flags));
    }

    public void setAutoDetach(int autoDetachFlags) {
        assertNotCloseInvoked();
        _broker.setAutoDetach(autoDetachFlags);
    }

    public void setAutoDetach(AutoDetachType value, boolean on) {
        assertNotCloseInvoked();
        _broker.setAutoDetach(AutoDetachType.fromEnumSet(EnumSet.of(value)),on);
    }

    public void setAutoDetach(int flag, boolean on) {
        assertNotCloseInvoked();
        _broker.setAutoDetach(flag, on);
    }

    public boolean getEvictFromStoreCache() {
        return _broker.getEvictFromDataCache();
    }

    public void setEvictFromStoreCache(boolean evict) {
        assertNotCloseInvoked();
        _broker.setEvictFromDataCache(evict);
    }

    public boolean getPopulateStoreCache() {
        return _broker.getPopulateDataCache();
    }

    public void setPopulateStoreCache(boolean cache) {
        assertNotCloseInvoked();
        _broker.setPopulateDataCache(cache);
    }

    public boolean isTrackChangesByType() {
        return _broker.isTrackChangesByType();
    }

    public void setTrackChangesByType(boolean trackByType) {
        assertNotCloseInvoked();
        _broker.setTrackChangesByType(trackByType);
    }

    public boolean isLargeTransaction() {
        return isTrackChangesByType();
    }

    public void setLargeTransaction(boolean value) {
        setTrackChangesByType(value);
    }

    public Object getUserObject(Object key) {
        return _broker.getUserObject(key);
    }

    public Object putUserObject(Object key, Object val) {
        assertNotCloseInvoked();
        return _broker.putUserObject(key, val);
    }

    public void addTransactionListener(Object listener) {
        assertNotCloseInvoked();
        _broker.addTransactionListener(listener);
    }

    public void removeTransactionListener(Object listener) {
        assertNotCloseInvoked();
        _broker.removeTransactionListener(listener);
    }

    public EnumSet<CallbackMode> getTransactionListenerCallbackModes() {
        return CallbackMode.toEnumSet(
            _broker.getTransactionListenerCallbackMode());
    }

    public void setTransactionListenerCallbackMode(CallbackMode mode) {
        assertNotCloseInvoked();
        _broker.setTransactionListenerCallbackMode(
            CallbackMode.fromEnumSet(EnumSet.of(mode)));
    }

    public void setTransactionListenerCallbackMode(EnumSet<CallbackMode> modes){
        assertNotCloseInvoked();
        _broker.setTransactionListenerCallbackMode(
            CallbackMode.fromEnumSet(modes));
    }

    public int getTransactionListenerCallbackMode() {
        return _broker.getTransactionListenerCallbackMode();
    }

    public void setTransactionListenerCallbackMode(int callbackMode) {
        throw new UnsupportedOperationException();
    }

    public void addLifecycleListener(Object listener, Class... classes) {
        assertNotCloseInvoked();
        _broker.addLifecycleListener(listener, classes);
    }

    public void removeLifecycleListener(Object listener) {
        assertNotCloseInvoked();
        _broker.removeLifecycleListener(listener);
    }

    public EnumSet<CallbackMode> getLifecycleListenerCallbackModes() {
        return CallbackMode.toEnumSet(
            _broker.getLifecycleListenerCallbackMode());
    }

    public void setLifecycleListenerCallbackMode(CallbackMode mode) {
        assertNotCloseInvoked();
        _broker.setLifecycleListenerCallbackMode(
            CallbackMode.fromEnumSet(EnumSet.of(mode)));
    }

    public void setLifecycleListenerCallbackMode(EnumSet<CallbackMode> modes) {
        assertNotCloseInvoked();
        _broker.setLifecycleListenerCallbackMode(
            CallbackMode.fromEnumSet(modes));
    }

    public int getLifecycleListenerCallbackMode() {
        return _broker.getLifecycleListenerCallbackMode();
    }

    public void setLifecycleListenerCallbackMode(int callbackMode) {
        assertNotCloseInvoked();
        _broker.setLifecycleListenerCallbackMode(callbackMode);
    }

    @SuppressWarnings("unchecked")
    public <T> T getReference(Class<T> cls, Object oid) {
        assertNotCloseInvoked();
        oid = _broker.newObjectId(cls, oid);
        return (T) _broker.find(oid, false, this);
    }

    @SuppressWarnings("unchecked")
    public <T> T find(Class<T> cls, Object oid) {
        assertNotCloseInvoked();
        if (oid == null)
        	return null;
        oid = _broker.newObjectId(cls, oid);
        return (T) _broker.find(oid, true, this);
    }

    public <T> T find(Class<T> cls, Object oid, LockModeType mode) {
        return find(cls, oid, mode, null);
    }

    public <T> T find(Class<T> cls, Object oid, 
        Map<String, Object> properties){
        return find(cls, oid, null, properties);
    }

    @SuppressWarnings("unchecked")
    public <T> T find(Class<T> cls, Object oid, LockModeType mode, Map<String, Object> properties) {
        assertNotCloseInvoked();
        properties = cloneProperties(properties);
        configureCurrentCacheModes(pushFetchPlan(), properties);
        configureCurrentFetchPlan(getFetchPlan(), properties, mode, true);
        try {
            oid = _broker.newObjectId(cls, oid);
            return (T) _broker.find(oid, true, this);
        } finally {
            popFetchPlan();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] findAll(Class<T> cls, Object... oids) {
        if (oids.length == 0)
            return (T[]) Array.newInstance(cls, 0);
        Collection<T> ret = findAll(cls, Arrays.asList(oids));
        return ret.toArray((T[]) Array.newInstance(cls, ret.size()));
    }

    @SuppressWarnings("unchecked")
    public <T> Collection<T> findAll(final Class<T> cls, Collection oids) {
        assertNotCloseInvoked();
        Object[] objs = _broker.findAll(oids, true, new FindCallbacks() {
            public Object processArgument(Object oid) {
                return _broker.newObjectId(cls, oid);
            }

            public Object processReturn(Object oid, OpenJPAStateManager sm) {
                return EntityManagerImpl.this.processReturn(oid, sm);
            }
        });
        return (Collection<T>) Arrays.asList(objs);
    }

    @SuppressWarnings("unchecked")
    public <T> T findCached(Class<T> cls, Object oid) {
        assertNotCloseInvoked();
        return (T) _broker.findCached(_broker.newObjectId(cls, oid), this);
    }

    public Class getObjectIdClass(Class cls) {
        assertNotCloseInvoked();
        if (cls == null)
            return null;
        return JPAFacadeHelper.fromOpenJPAObjectIdClass
                (_broker.getObjectIdType(cls));
    }

    public OpenJPAEntityTransaction getTransaction() {
        if (_broker.isManaged())
            throw new InvalidStateException(_loc.get("get-managed-trans"),
                null, null, false);
        return this;
    }

    public void joinTransaction() {
        assertNotCloseInvoked();
        if (!_broker.syncWithManagedTransaction())
            throw new TransactionRequiredException(_loc.get
                ("no-managed-trans"), null, null, false);
    }

    public void begin() {
        _broker.begin();
    }

    public void commit() {
        try {
            _broker.commit();
        } catch (RollbackException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
        	// Per JPA 2.0 spec, if the exception was due to a JSR-303 
            // constraint violation, the ConstraintViolationException should be 
            // thrown.  Since JSR-303 is optional, the cast to RuntimeException 
            // prevents the introduction of a runtime dependency on the BV API.
            if (ValidationUtils.isConstraintViolationException(e))
                throw (RuntimeException)e;
            // RollbackExceptions are special and aren't handled by the
            // normal exception translator, since the spec says they
            // should be thrown whenever the commit fails for any reason at
            // all, wheras the exception translator handles exceptions that
            // are caused for specific reasons            

            // pass along the failed object if one is available.
            Object failedObject = null;
            if (e instanceof ExceptionInfo){
            	failedObject = ((ExceptionInfo)e).getFailedObject();            	
            }
            
            throw new RollbackException(e).setFailedObject(failedObject);
        }
    }

    public void rollback() {
        _broker.rollback();
    }

    public void commitAndResume() {
        _broker.commitAndResume();
    }

    public void rollbackAndResume() {
        _broker.rollbackAndResume();
    }

    public Throwable getRollbackCause() {
        if (!isActive())
            throw new IllegalStateException(_loc.get("no-transaction")
                .getMessage());

        return _broker.getRollbackCause();
    }

    public boolean getRollbackOnly() {
        if (!isActive())
            throw new IllegalStateException(_loc.get("no-transaction")
                .getMessage());

        return _broker.getRollbackOnly();
    }

    public void setRollbackOnly() {
        _broker.setRollbackOnly();
    }

    public void setRollbackOnly(Throwable cause) {
        _broker.setRollbackOnly(cause);
    }

    public void setSavepoint(String name) {
        assertNotCloseInvoked();
        _broker.setSavepoint(name);
    }

    public void rollbackToSavepoint() {
        assertNotCloseInvoked();
        _broker.rollbackToSavepoint();
    }

    public void rollbackToSavepoint(String name) {
        assertNotCloseInvoked();
        _broker.rollbackToSavepoint(name);
    }

    public void releaseSavepoint() {
        assertNotCloseInvoked();
        _broker.releaseSavepoint();
    }

    public void releaseSavepoint(String name) {
        assertNotCloseInvoked();
        _broker.releaseSavepoint(name);
    }

    public void flush() {
        assertNotCloseInvoked();
        _broker.assertOpen();
        _broker.assertActiveTransaction();
        _broker.flush();
    }

    public void preFlush() {
        assertNotCloseInvoked();
        _broker.preFlush();
    }

    public void validateChanges() {
        assertNotCloseInvoked();
        _broker.validateChanges();
    }

    public boolean isActive() {
        return isOpen() && _broker.isActive();
    }

    public boolean isStoreActive() {
        return _broker.isStoreActive();
    }

    public void beginStore() {
        _broker.beginStore();
    }

    public boolean contains(Object entity) {
        assertNotCloseInvoked();
        if (entity == null)
            return false;
        OpenJPAStateManager sm = _broker.getStateManager(entity);
        if (sm == null
            && !ImplHelper.isManagedType(getConfiguration(), entity.getClass()))
            throw new ArgumentException(_loc.get("not-entity",
                entity.getClass()), null, null, true);
        return sm != null && !sm.isDeleted();
    }

    public boolean containsAll(Object... entities) {
        for (Object entity : entities)
            if (!contains(entity))
                return false;
        return true;
    }

    public boolean containsAll(Collection entities) {
        for (Object entity : entities)
            if (!contains(entity))
                return false;
        return true;
    }

    public void persist(Object entity) {
        assertNotCloseInvoked();
        _broker.persist(entity, this);
    }

    public void persistAll(Object... entities) {
        persistAll(Arrays.asList(entities));
    }

    public void persistAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.persistAll(entities, this);
    }

    public void remove(Object entity) {
        assertNotCloseInvoked();
        _broker.delete(entity, this);
    }

    public void removeAll(Object... entities) {
        removeAll(Arrays.asList(entities));
    }

    public void removeAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.deleteAll(entities, this);
    }

    public void release(Object entity) {
        assertNotCloseInvoked();
        _broker.release(entity, this);
    }

    public void releaseAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.releaseAll(entities, this);
    }

    public void releaseAll(Object... entities) {
        releaseAll(Arrays.asList(entities));
    }

    public void refresh(Object entity) {
        refresh(entity, null, null);
    }

    public void refresh(Object entity, LockModeType mode) {
        refresh(entity, mode, null);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        refresh(entity, null, properties);
    }

    public void refresh(Object entity, LockModeType mode, Map<String, Object> properties) {
        assertNotCloseInvoked();
        assertValidAttchedEntity(REFRESH, entity);

        _broker.assertWriteOperation();
        configureCurrentCacheModes(pushFetchPlan(), properties);
        configureCurrentFetchPlan(getFetchPlan(), properties, mode, true);
        DataCacheRetrieveMode rmode = getFetchPlan().getCacheRetrieveMode();
        if (DataCacheRetrieveMode.USE.equals(rmode) || rmode == null) {
            getFetchPlan().setCacheRetrieveMode(DataCacheRetrieveMode.BYPASS);
        }
        try {
            _broker.refresh(entity, this);
        } finally {
            popFetchPlan();
        }
    }

    public void refreshAll() {
        assertNotCloseInvoked();
        _broker.assertWriteOperation();
        _broker.refreshAll(_broker.getTransactionalObjects(), this);
    }

    public void refreshAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.assertWriteOperation();
        _broker.refreshAll(entities, this);
    }

    public void refreshAll(Object... entities) {
        refreshAll(Arrays.asList(entities));
    }

    public void retrieve(Object entity) {
        assertNotCloseInvoked();
        _broker.retrieve(entity, true, this);
    }

    public void retrieveAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.retrieveAll(entities, true, this);
    }

    public void retrieveAll(Object... entities) {
        retrieveAll(Arrays.asList(entities));
    }

    public void evict(Object entity) {
        assertNotCloseInvoked();
        _broker.evict(entity, this);
    }

    public void evictAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.evictAll(entities, this);
    }

    public void evictAll(Object... entities) {
        evictAll(Arrays.asList(entities));
    }

    public void evictAll() {
        assertNotCloseInvoked();
        _broker.evictAll(this);
    }

    public void evictAll(Class cls) {
        assertNotCloseInvoked();
        _broker.evictAll(_broker.newExtent(cls, true), this);
    }

    public void evictAll(Extent extent) {
        assertNotCloseInvoked();
        _broker.evictAll(((ExtentImpl) extent).getDelegate(), this);
    }

    @SuppressWarnings("unchecked")
    public <T> T detachCopy(T entity) {
        assertNotCloseInvoked();
        Compatibility compat = this.getConfiguration().
            getCompatibilityInstance();
        boolean copyOnDetach = compat.getCopyOnDetach();
        boolean cascadeWithDetach = compat.getCascadeWithDetach();
        // Set compatibility options to get 1.x detach behavior
        compat.setCopyOnDetach(true);
        compat.setCascadeWithDetach(true);
        try {
            T t = (T)_broker.detach(entity, this);
            return t;
        } finally {
            // Reset compatibility options
            compat.setCopyOnDetach(copyOnDetach);
            compat.setCascadeWithDetach(cascadeWithDetach);
        }        
    }

    public Object[] detachAll(Object... entities) {
        assertNotCloseInvoked();
        return _broker.detachAll(Arrays.asList(entities), this);
    }

    public Collection detachAll(Collection entities) {
        assertNotCloseInvoked();
        return Arrays.asList(_broker.detachAll(entities, this));
    }

    @SuppressWarnings("unchecked")
    public <T> T merge(T entity) {
        assertNotCloseInvoked();
        return (T) _broker.attach(entity, true, this);
    }

    public Object[] mergeAll(Object... entities) {
        if (entities.length == 0)
            return EMPTY_OBJECTS;
        return mergeAll(Arrays.asList(entities)).toArray();
    }

    public Collection mergeAll(Collection entities) {
        assertNotCloseInvoked();
        return Arrays.asList(_broker.attachAll(entities, true, this));
    }

    public void transactional(Object entity, boolean updateVersion) {
        assertNotCloseInvoked();
        _broker.transactional(entity, updateVersion, this);
    }

    public void transactionalAll(Collection objs, boolean updateVersion) {
        assertNotCloseInvoked();
        _broker.transactionalAll(objs, updateVersion, this);
    }

    public void transactionalAll(Object[] objs, boolean updateVersion) {
        assertNotCloseInvoked();
        _broker.transactionalAll(Arrays.asList(objs), updateVersion, this);
    }

    public void nontransactional(Object entity) {
        assertNotCloseInvoked();
        _broker.nontransactional(entity, this);
    }

    public void nontransactionalAll(Collection objs) {
        assertNotCloseInvoked();
        _broker.nontransactionalAll(objs, this);
    }

    public void nontransactionalAll(Object[] objs) {
        assertNotCloseInvoked();
        _broker.nontransactionalAll(Arrays.asList(objs), this);
    }

    public Generator getNamedGenerator(String name) {
        assertNotCloseInvoked();
        try {
            SequenceMetaData meta = _broker.getConfiguration().
                getMetaDataRepositoryInstance().getSequenceMetaData(name,
                _broker.getClassLoader(), true);
            Seq seq = meta.getInstance(_broker.getClassLoader());
            return new GeneratorImpl(seq, name, _broker, null);
        } catch (RuntimeException re) {
            throw PersistenceExceptions.toPersistenceException(re);
        }
    }

    public Generator getIdGenerator(Class forClass) {
        assertNotCloseInvoked();
        try {
            ClassMetaData meta = _broker.getConfiguration().
                getMetaDataRepositoryInstance().getMetaData(forClass,
                _broker.getClassLoader(), true);
            Seq seq = _broker.getIdentitySequence(meta);
            return (seq == null) ? null : new GeneratorImpl(seq, null, _broker,
                meta);
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        }
    }

    public Generator getFieldGenerator(Class forClass, String fieldName) {
        assertNotCloseInvoked();
        try {
            ClassMetaData meta = _broker.getConfiguration().
                getMetaDataRepositoryInstance().getMetaData(forClass,
                _broker.getClassLoader(), true);
            FieldMetaData fmd = meta.getField(fieldName);
            if (fmd == null)
                throw new ArgumentException(_loc.get("no-named-field",
                    forClass, fieldName), null, null, false);

            Seq seq = _broker.getValueSequence(fmd);
            return (seq == null) ? null : new GeneratorImpl(seq, null, _broker,
                meta);
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        }
    }

    public <T> Extent<T> createExtent(Class<T> cls, boolean subclasses) {
        assertNotCloseInvoked();
        return new ExtentImpl<T>(this, _broker.newExtent(cls, subclasses));
    }

    @SuppressWarnings("unchecked")
    public <T> TypedQuery<T> createQuery(String query, Class<T> resultClass) {
        return createQuery(query).setResultClass(resultClass);
    }
    
    public OpenJPAQuery createQuery(String query) {
        return createQuery(JPQLParser.LANG_JPQL, query);
    }

    public OpenJPAQuery createQuery(String language, String query) {
        assertNotCloseInvoked();
        try {
            // We need
            if (query != null && _convertPositionalParams && JPQLParser.LANG_JPQL.equals(language)) {
                query = query.replaceAll("[\\?]", "\\:_");
            }
            String qid = query;
            PreparedQuery pq = JPQLParser.LANG_JPQL.equals(language)
                ? getPreparedQuery(qid) : null;
            org.apache.openjpa.kernel.Query q = (pq == null || !pq.isInitialized())
                ? _broker.newQuery(language, query)
                : _broker.newQuery(pq.getLanguage(), pq);
            // have to validate JPQL according to spec
            if (pq == null && JPQLParser.LANG_JPQL.equals(language))
                q.compile(); 
            if (pq != null) {
                pq.setInto(q);
            }
            return newQueryImpl(q, null).setId(qid);
        } catch (RuntimeException re) {
            throw PersistenceExceptions.toPersistenceException(re);
        }
    }
    
    public OpenJPAQuery createQuery(Query query) {
        if (query == null)
            return createQuery((String) null);
        assertNotCloseInvoked();
        org.apache.openjpa.kernel.Query q = ((QueryImpl) query).getDelegate();
        return newQueryImpl(_broker.newQuery(q.getLanguage(), q), null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return createNamedQuery(name).setResultClass(resultClass);
    }

    public OpenJPAQuery createNamedQuery(String name) {
        assertNotCloseInvoked();
        _broker.assertOpen();
        try {
            QueryMetaData meta = _broker.getConfiguration().
                getMetaDataRepositoryInstance().getQueryMetaData(null, name,
                _broker.getClassLoader(), true);
            String qid = meta.getQueryString();
            
            PreparedQuery pq = JPQLParser.LANG_JPQL.equals(meta.getLanguage()) ? getPreparedQuery(qid) : null;
            org.apache.openjpa.kernel.Query del =
                (pq == null || !pq.isInitialized()) ? _broker.newQuery(meta.getLanguage(), meta.getQueryString())
                    : _broker.newQuery(pq.getLanguage(), pq);
            
            if (pq != null) {
                pq.setInto(del);
            } else {
                meta.setInto(del);
                del.compile();
            }
            
            OpenJPAQuery q = newQueryImpl(del, meta).setId(qid);
            String[] hints = meta.getHintKeys();
            Object[] values = meta.getHintValues();
            for (int i = 0; i < hints.length; i++)
                q.setHint(hints[i], values[i]);
            return q;
        } catch (RuntimeException re) {
            throw PersistenceExceptions.toPersistenceException(re);
        }
    }    

    public OpenJPAQuery createNativeQuery(String query) {
        validateSQL(query);
        return createQuery(QueryLanguages.LANG_SQL, query);
    }

    public OpenJPAQuery createNativeQuery(String query, Class cls) {
        return createNativeQuery(query).setResultClass(cls);
    }

    public OpenJPAQuery createNativeQuery(String query, String mappingName) {
        assertNotCloseInvoked();
        validateSQL(query);
        org.apache.openjpa.kernel.Query kernelQuery = _broker.newQuery(
            QueryLanguages.LANG_SQL, query);
        kernelQuery.setResultMapping(null, mappingName);
        return newQueryImpl(kernelQuery, null);
    }

    protected <T> QueryImpl<T> newQueryImpl(org.apache.openjpa.kernel.Query kernelQuery, QueryMetaData qmd) {
        return new QueryImpl<T>(this, _ret, kernelQuery, qmd);
    }
    
    /**
     * @Deprecated -- Use org.apache.openjpa.persistence.EntityManagerImpl.newQueryImpl(Query kernelQuery, QueryMetaData
     *             qmd)
     * <br>
     *             Leave this method here as extenders of OpenJPA might depend on this hook to allow interception of
     *             query creation
     */
    protected <T> QueryImpl<T> newQueryImpl(org.apache.openjpa.kernel.Query kernelQuery) {
        return new QueryImpl<T>(this, _ret, kernelQuery, null);
    }

    /**
     * Validate that the user provided SQL.
     */
    protected void validateSQL(String query) {
        if (StringUtils.trimToNull(query) == null)
            throw new ArgumentException(_loc.get("no-sql"), null, null, false);
    }
    
    PreparedQueryCache getPreparedQueryCache() {
        return _broker.getCachePreparedQuery() ?
            getConfiguration().getQuerySQLCacheInstance() : null;
    }
    
    /**
     * Gets the prepared query cached by the given key. 
     * 
     * @return the cached PreparedQuery or null if none exists.
     */
    PreparedQuery getPreparedQuery(String id) {
        PreparedQueryCache cache = getPreparedQueryCache();
        return (cache == null) ? null : cache.get(id);
    }

    public void setFlushMode(FlushModeType flushMode) {
        assertNotCloseInvoked();
        _broker.assertOpen();
        _broker.getFetchConfiguration().setFlushBeforeQueries
            (toFlushBeforeQueries(flushMode));
    }

    public FlushModeType getFlushMode() {
        assertNotCloseInvoked();
        _broker.assertOpen();
        return fromFlushBeforeQueries(_broker.getFetchConfiguration().
            getFlushBeforeQueries());
    }

    /**
     * Translate our internal flush constant to a flush mode enum value.
     */
    static FlushModeType fromFlushBeforeQueries(int flush) {
        switch (flush) {
            case QueryFlushModes.FLUSH_TRUE:
                return FlushModeType.AUTO;
            case QueryFlushModes.FLUSH_FALSE:
                return FlushModeType.COMMIT;
            default:
                return null;
        }
    }

    /**
     * Translate a flush mode enum value to our internal flush constant.
     */
    static int toFlushBeforeQueries(FlushModeType flushMode) {
        // choose default for null
        if (flushMode == null)
            return QueryFlushModes.FLUSH_WITH_CONNECTION;
        if (flushMode == FlushModeType.AUTO)
            return QueryFlushModes.FLUSH_TRUE;
        if (flushMode == FlushModeType.COMMIT)
            return QueryFlushModes.FLUSH_FALSE;
        throw new ArgumentException(flushMode.toString(), null, null, false);
    }

    /*
     * Used by Java EE Containers that wish to pool OpenJPA EntityManagers.  The specification
     * doesn't allow the closing of connections with the clear() method.  By introducing this
     * new method, we can do additional processing (and maybe more efficient processing) to 
     * properly prepare an EM for pooling.
     */
    public void prepareForPooling() {
        assertNotCloseInvoked();
        clear();
        // Do not close connection if ConnectionRetainMode is set to Always...
        if (getConnectionRetainMode() != ConnectionRetainMode.ALWAYS) {
            _broker.lock();  // since this direct close path is not protected...
            try {
                _broker.getStoreManager().close();
            } finally {
                _broker.unlock();
            }
        }
    }
    
    public void clear() {
        assertNotCloseInvoked();
        _broker.detachAll(this, false);
        _plans.clear();
    }

    public Object getDelegate() {
        _broker.assertOpen();
        assertNotCloseInvoked();
        return this;
    }

    public LockModeType getLockMode(Object entity) {
        assertNotCloseInvoked();
        _broker.assertActiveTransaction();
        assertValidAttchedEntity(GET_LOCK_MODE, entity);
        return MixedLockLevelsHelper.fromLockLevel(
            _broker.getLockLevel(entity));
    }

    public void lock(Object entity, LockModeType mode) {
        lock(entity, mode, -1);
    }

    public void lock(Object entity) {
        assertNotCloseInvoked();
        assertValidAttchedEntity(LOCK, entity);
        _broker.lock(entity, this);
    }

    public void lock(Object entity, LockModeType mode, int timeout) {
        assertNotCloseInvoked();
        assertValidAttchedEntity(LOCK, entity);

        configureCurrentFetchPlan(pushFetchPlan(), null, mode, false);
        try {
            _broker.lock(entity, MixedLockLevelsHelper.toLockLevel(mode),  timeout, this);
        } finally {
            popFetchPlan();
        }
    }

    public void lock(Object entity, LockModeType mode, Map<String, Object> properties) {
        assertNotCloseInvoked();
        assertValidAttchedEntity(LOCK, entity);
        _broker.assertActiveTransaction();
        properties = cloneProperties(properties);
        configureCurrentCacheModes(pushFetchPlan(), properties);
        configureCurrentFetchPlan(getFetchPlan(), properties, mode, false);
        try {
            _broker.lock(entity, MixedLockLevelsHelper.toLockLevel(mode),
                _broker.getFetchConfiguration().getLockTimeout(), this);
        } finally {
            popFetchPlan();
        }
    }

    public void lockAll(Collection entities) {
        assertNotCloseInvoked();
        _broker.lockAll(entities, this);
    }

    public void lockAll(Collection entities, LockModeType mode, int timeout) {
        assertNotCloseInvoked();
        _broker.lockAll(entities, MixedLockLevelsHelper.toLockLevel(mode),
            timeout, this);
    }

    public void lockAll(Object... entities) {
        lockAll(Arrays.asList(entities));
    }

    public void lockAll(Object[] entities, LockModeType mode, int timeout) {
        lockAll(Arrays.asList(entities), mode, timeout);
    }

    public boolean cancelAll() {
        return _broker.cancelAll();
    }

    public Object getConnection() {
        return _broker.getConnection();
    }

    public Collection getManagedObjects() {
        return _broker.getManagedObjects();
    }

    public Collection getTransactionalObjects() {
        return _broker.getTransactionalObjects();
    }

    public Collection getPendingTransactionalObjects() {
        return _broker.getPendingTransactionalObjects();
    }

    public Collection getDirtyObjects() {
        return _broker.getDirtyObjects();
    }

    public boolean getOrderDirtyObjects() {
        return _broker.getOrderDirtyObjects();
    }

    public void setOrderDirtyObjects(boolean order) {
        assertNotCloseInvoked();
        _broker.setOrderDirtyObjects(order);
    }

    public void dirtyClass(Class cls) {
        assertNotCloseInvoked();
        _broker.dirtyType(cls);
    }

    @SuppressWarnings("unchecked")
    public Collection<Class> getPersistedClasses() {
        return (Collection<Class>) _broker.getPersistedTypes();
    }

    @SuppressWarnings("unchecked")
    public Collection<Class> getUpdatedClasses() {
        return (Collection<Class>) _broker.getUpdatedTypes();
    }

    @SuppressWarnings("unchecked")
    public Collection<Class> getRemovedClasses() {
        return (Collection<Class>) _broker.getDeletedTypes();
    }

    public <T> T createInstance(Class<T> cls) {
        assertNotCloseInvoked();
        return (T) _broker.newInstance(cls);
    }

    public void close() {
        assertNotCloseInvoked();
        Log log = _emf.getConfiguration().getLog(OpenJPAConfiguration.LOG_RUNTIME);
        if (log.isTraceEnabled()) {
            log.trace(this + ".close() invoked.");
        }
        _broker.close();
        _plans.clear();
    }

    public boolean isOpen() {
        return !_broker.isCloseInvoked();
    }

    public void dirty(Object o, String field) {
        assertNotCloseInvoked();
        OpenJPAStateManager sm = _broker.getStateManager(o);
        try {
            if (sm != null)
                sm.dirty(field);
        } catch (Exception e) {
            throw PersistenceExceptions.toPersistenceException(e);
        }
    }

    public Object getObjectId(Object o) {
        assertNotCloseInvoked();
        return JPAFacadeHelper.fromOpenJPAObjectId(_broker.getObjectId(o));
    }

    public boolean isDirty(Object o) {
        assertNotCloseInvoked();
        return _broker.isDirty(o);
    }

    public boolean isTransactional(Object o) {
        assertNotCloseInvoked();
        return _broker.isTransactional(o);
    }

    public boolean isPersistent(Object o) {
        assertNotCloseInvoked();
        return _broker.isPersistent(o);
    }

    public boolean isNewlyPersistent(Object o) {
        assertNotCloseInvoked();
        return _broker.isNew(o);
    }

    public boolean isRemoved(Object o) {
        assertNotCloseInvoked();
        return _broker.isDeleted(o);
    }

    public boolean isDetached(Object entity) {
        assertNotCloseInvoked();
        return _broker.isDetached(entity);
    }

    public Object getVersion(Object o) {
        assertNotCloseInvoked();
        return _broker.getVersion(o);
    }

    /**
     * Throw appropriate exception if close has been invoked but the broker
     * is still open.  We test only for this because if the broker is already
     * closed, it will throw its own more informative exception when we 
     * delegate the pending operation to it.
     */
    protected void assertNotCloseInvoked() {
        if (!_broker.isClosed() && _broker.isCloseInvoked())
            throw new InvalidStateException(_loc.get("close-invoked"), null,
                null, true);
    }

    /**
     * Throw IllegalArgumentExceptionif if entity is not a valid entity or
     * if it is detached.
     */
    void assertValidAttchedEntity(String call, Object entity) {
        OpenJPAStateManager sm = _broker.getStateManager(entity);
        if (sm == null || !sm.isPersistent() || sm.isDetached() || (call.equals(REFRESH) && sm.isDeleted())) {
            throw new IllegalArgumentException(_loc.get("invalid_entity_argument", 
                call, entity == null ? "null" : Exceptions.toString(entity)).getMessage());
        }
    }

    ////////////////////////////////
    // FindCallbacks implementation
    ////////////////////////////////

    public Object processArgument(Object arg) {
        return arg;
    }

    public Object processReturn(Object oid, OpenJPAStateManager sm) {
        return (sm == null || sm.isDeleted()) ? null : sm.getManagedInstance();
    }

    //////////////////////////////
    // OpCallbacks implementation
    //////////////////////////////

    public int processArgument(int op, Object obj, OpenJPAStateManager sm) {
        switch (op) {
            case OP_DELETE:
                // cascade through non-persistent non-detached instances
                if (sm == null && !_broker.isDetached(obj))
                    return ACT_CASCADE;
                if (sm != null && !sm.isDetached() && !sm.isPersistent())
                    return ACT_CASCADE;
                // ignore deleted instances
                if (sm != null && sm.isDeleted())
                    return ACT_NONE;
                break;
            case OP_ATTACH:
                // die on removed
                if (sm != null && sm.isDeleted())
                    throw new UserException(_loc.get("removed",
                        Exceptions.toString(obj))).setFailedObject(obj);
                // cascade through managed instances
                if (sm != null && !sm.isDetached())
                    return ACT_CASCADE;
                break;
            case OP_REFRESH:
                // die on unmanaged instances
                if (sm == null)
                    throw new UserException(_loc.get("not-managed",
                        Exceptions.toString(obj))).setFailedObject(obj);
                break;
            case OP_DETACH:
                if (sm == null || !sm.isPersistent() || sm.isDetached())
                    return ACT_NONE;
                break;
        }
        return ACT_RUN | ACT_CASCADE;
    }

    public int hashCode() {
        return (_broker == null) ? 0 : _broker.hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if ((other == null) || (other.getClass() != this.getClass()))
            return false;
        if (_broker == null)
            return false;
        return _broker.equals(((EntityManagerImpl) other)._broker);
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        try {
            _ret = PersistenceExceptions.getRollbackTranslator(this);

            // this assumes that serialized Brokers are from something
            // that extends AbstractBrokerFactory.
            Object factoryKey = in.readObject();
            AbstractBrokerFactory factory =
                AbstractBrokerFactory.getPooledFactoryForKey(factoryKey);
            byte[] brokerBytes = (byte[]) in.readObject();
            ObjectInputStream innerIn = new BrokerBytesInputStream(brokerBytes,
                factory.getConfiguration());

            Broker broker = (Broker) innerIn.readObject();
            EntityManagerFactoryImpl emf = (EntityManagerFactoryImpl)
                JPAFacadeHelper.toEntityManagerFactory(
                    broker.getBrokerFactory());
            broker.putUserObject(JPAFacadeHelper.EM_KEY, this);
            initialize(emf, broker);
        } catch (RuntimeException re) {
            try {
                re = _ret.translate(re);
            } catch (Exception e) {
                // ignore
            }
            throw re;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            // this requires that only AbstractBrokerFactory-sourced
            // brokers can be serialized
            Object factoryKey = ((AbstractBrokerFactory) _broker
                .getBrokerFactory()).getPoolKey();
            out.writeObject(factoryKey);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream innerOut = new ObjectOutputStream(baos);
            _broker.getDelegate().putUserObject(JPAFacadeHelper.EM_KEY, null);
            innerOut.writeObject(_broker.getDelegate());
            innerOut.flush();
            out.writeObject(baos.toByteArray());
        } catch (RuntimeException re) {
            try {
                re = _ret.translate(re);
            } catch (Exception e) {
                // ignore
            }
            throw re;
        }
    }

    private static class BrokerBytesInputStream extends ObjectInputStream {

        private OpenJPAConfiguration conf;

        BrokerBytesInputStream(byte[] bytes, OpenJPAConfiguration conf)
            throws IOException {
            super(new ByteArrayInputStream(bytes));
            if (conf == null)
                throw new IllegalArgumentException(
                    "Illegal null argument to ObjectInputStreamWithLoader");
            this.conf = conf;
        }

        /**
         * Make a primitive array class
         */
        private Class primitiveType(char type) {
            switch (type) {
                case 'B': return byte.class;
                case 'C': return char.class;
                case 'D': return double.class;
                case 'F': return float.class;
                case 'I': return int.class;
                case 'J': return long.class;
                case 'S': return short.class;
                case 'Z': return boolean.class;
                default: return null;
            }
        }

        protected Class<?> resolveClass(ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException {

            String cname = classDesc.getName();
            if (cname.startsWith("[")) {
                // An array
                Class<?> component;		// component class
                int dcount;			    // dimension
                for (dcount=1; cname.charAt(dcount)=='['; dcount++) ;
                if (cname.charAt(dcount) == 'L') {
                    component = lookupClass(cname.substring(dcount+1,
                        cname.length()-1));
                } else {
                    if (cname.length() != dcount+1) {
                        throw new ClassNotFoundException(cname);// malformed
                    }
                    component = primitiveType(cname.charAt(dcount));
                }
                int dim[] = new int[dcount];
                for (int i=0; i<dcount; i++) {
                    dim[i]=0;
                }
                return Array.newInstance(component, dim).getClass();
            } else {
                return lookupClass(cname);
            }
        }

        /**
         * If this is a generated subclass, look up the corresponding Class
         * object via metadata.
         */
        private Class<?> lookupClass(String className)
            throws ClassNotFoundException {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                if (PCEnhancer.isPCSubclassName(className)) {
                    String superName = PCEnhancer.toManagedTypeName(className);
                    ClassMetaData[] metas = conf.getMetaDataRepositoryInstance()
                        .getMetaDatas();
                    for (int i = 0; i < metas.length; i++) {
                        if (superName.equals(
                            metas[i].getDescribedType().getName())) {
                            return PCRegistry.getPCType(
                                metas[i].getDescribedType());
                        }
                    }

                    // if it's not found, try to look for it anyways
                    return Class.forName(className);
                } else {
                    throw e;
                }
            }
        }
    }

    public void detach(Object entity) {
        if (entity == null)
            throw new IllegalArgumentException(_loc.get("null-detach").getMessage());
        assertNotCloseInvoked();
        _broker.detach(entity, this);
    }

    /**
     * Create a query from the given CritriaQuery.
     * Compile to register the parameters in this query.
     */
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        ((OpenJPACriteriaQuery<T>)criteriaQuery).compile(); 
        
        org.apache.openjpa.kernel.Query kernelQuery =_broker.newQuery(CriteriaBuilderImpl.LANG_CRITERIA, criteriaQuery);
        
        QueryImpl<T> facadeQuery = newQueryImpl(kernelQuery, null).setId(criteriaQuery.toString());
        Set<ParameterExpression<?>> params = criteriaQuery.getParameters();
        
        for (ParameterExpression<?> param : params) {
            facadeQuery.declareParameter(param, param);
        }
        return facadeQuery;
    }
    
    public OpenJPAQuery createDynamicQuery(
        org.apache.openjpa.persistence.query.QueryDefinition qdef) {
        String jpql = _emf.getDynamicQueryBuilder().toJPQL(qdef);
        return createQuery(jpql);
    }

    /**
     * Get the properties used currently by this entity manager.
     * The property keys and their values are harvested from kernel artifacts namely
     * the Broker and FetchPlan by reflection.
     * These property keys and values that denote the bean properties/values of the kernel artifacts
     * are converted to the original keys/values that user used to set the properties.
     *    
     */
    public Map<String, Object> getProperties() {
        Map<String,Object> props = _broker.getProperties();
        for (String s : _broker.getSupportedProperties()) {
            String kernelKey = getBeanPropertyName(s);
            Method getter = Reflection.findGetter(this.getClass(), kernelKey, false);
            if (getter != null) {
                String userKey = JPAProperties.getUserName(kernelKey);
                Object kvalue  = Reflection.get(this, getter);
                props.put(userKey.equals(kernelKey) ? s : userKey, JPAProperties.convertToUserValue(userKey, kvalue));
            }
        }
        FetchPlan fetch = getFetchPlan();
        Class<?> fetchType = fetch.getClass();
        Set<String> fProperties = Reflection.getBeanStylePropertyNames(fetchType);
        for (String s : fProperties) {
            String kernelKey = getBeanPropertyName(s);
            Method getter = Reflection.findGetter(fetchType, kernelKey, false);
            if (getter != null) {
                String userKey = JPAProperties.getUserName(kernelKey);
                Object kvalue  = Reflection.get(fetch, getter);
                props.put(userKey.equals(kernelKey) ? s : userKey, JPAProperties.convertToUserValue(userKey, kvalue));
            }
        }
        return props;
    }

    public OpenJPACriteriaBuilder getCriteriaBuilder() {
        return _emf.getCriteriaBuilder();
    }

    public Set<String> getSupportedProperties() {
        return _broker.getSupportedProperties();
    }

    /**
     * Unwraps this receiver to an instance of the given class, if possible.
     * 
     * @exception if the given class is null, generic <code>Object.class</code> or a class
     * that is not wrapped by this receiver.  
     */
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> cls) {
        if (cls != null && cls != Object.class) {
            Object[] delegates = new Object[] { _broker.getInnermostDelegate(), _broker.getDelegate(), _broker, this };
            for (Object o : delegates) {
                if (cls.isInstance(o))
                    return (T) o;
            }
            // Only call getConnection() once we are certain that is the type that we need to unwrap.
            if (cls.isAssignableFrom(Connection.class)) {
                Object o = getConnection();
                if(Connection.class.isInstance(o)){
                    return (T) o;   
                }else{
                    // Try and cleanup if  aren't going to return the connection back to the caller.
                    ImplHelper.close(o);
                }
            }
        }
        // Set this transaction to rollback only (as per spec) here because the raised exception 
        // does not go through normal exception translation pathways
        RuntimeException ex = new PersistenceException(_loc.get("unwrap-em-invalid", cls).toString(), null, 
                this, false);
        if (isActive())
            setRollbackOnly(ex);
        throw ex;
    }

    public void setQuerySQLCache(boolean flag) {
        _broker.setCachePreparedQuery(flag);
    }
    
    public boolean getQuerySQLCache() {
        return _broker.getCachePreparedQuery();
    }
    
    RuntimeExceptionTranslator getExceptionTranslator() {
        return _ret;
    }

    /**
     * Populate the given FetchPlan with the given properties. 
     * Optionally overrides the given lock mode.
     */
    private void configureCurrentFetchPlan(FetchPlan fetch, Map<String, Object> properties, 
            LockModeType lock, boolean requiresTxn) {
        // handle properties in map first
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals("javax.persistence.lock.scope")) {
                    fetch.setLockScope((PessimisticLockScope)value);
                } else
                    fetch.setHint(key, value);
            }
        }
        // override with the specific lockMode, if needed.
        if (lock != null && lock != LockModeType.NONE) {
            if (requiresTxn) {
                _broker.assertActiveTransaction();
            }
            // Override read lock level
            LockModeType curReadLockMode = fetch.getReadLockMode();
            if (lock != curReadLockMode)
                fetch.setReadLockMode(lock);
        }
    }
    
    /**
     * Populate the fetch configuration with specified cache mode properties.
     * The cache mode properties modify the fetch configuration and remove those
     * properties. This method should be called <em>before</em> the fetch configuration of the current 
     * context has been pushed.
     * @param fetch the fetch configuration of the current context. Not the 
     * new configuration pushed (and later popped) during a single operation.
     * 
     * @param properties
     */
    private void configureCurrentCacheModes(FetchPlan fetch, Map<String, Object> properties) {
        if (properties == null)
            return;
        CacheRetrieveMode rMode = JPAProperties.getEnumValue(CacheRetrieveMode.class, 
                JPAProperties.CACHE_RETRIEVE_MODE, properties);
        if (rMode != null) {
            fetch.setCacheRetrieveMode(JPAProperties.convertToKernelValue(DataCacheRetrieveMode.class, 
                    JPAProperties.CACHE_RETRIEVE_MODE, rMode));
            properties.remove(JPAProperties.CACHE_RETRIEVE_MODE);
        }
        CacheStoreMode sMode = JPAProperties.getEnumValue(CacheStoreMode.class, 
                JPAProperties.CACHE_STORE_MODE, properties);
        if (sMode != null) {
            fetch.setCacheStoreMode(JPAProperties.convertToKernelValue(DataCacheStoreMode.class, 
                    JPAProperties.CACHE_STORE_MODE, sMode));
            properties.remove(JPAProperties.CACHE_STORE_MODE);
        }
    }

    public Metamodel getMetamodel() {
        return _emf.getMetamodel();
    }

    /**
     * Sets the given property to the given value, reflectively.
     * 
     * The property key is transposed to a bean-style property.
     * The value is converted to a type consumable by the kernel.
     * After requisite transformation, if the value can not be set
     * on either this instance or its fetch plan by reflection,
     * then an warning message (not an exception as per JPA specification) is issued.
     */
    public void setProperty(String prop, Object value) {
        if (!setKernelProperty(this, prop, value)) {
            if (!setKernelProperty(this.getFetchPlan(), prop, value)) {
                Log log = getConfiguration().getLog(OpenJPAConfiguration.LOG_RUNTIME);
                if (log.isWarnEnabled()) {
                    log.warn(_loc.get("ignored-em-prop", prop, value == null ? "" : value.getClass()+":" + value));
                 }
            }
        }
    }
    
    /**
     * Attempt to set the given property and value to the given target instance.
     * The original property is transposed to a bean-style property name.
     * The original value is transformed to a type consumable by the target.
     *  
     * @return if the property can be set to the given target.
     */
    private boolean setKernelProperty(Object target, String original, Object value) {
        String beanProp = getBeanPropertyName(original);
        JPAProperties.record(beanProp, original);
        Class<?> kType  = null;
        Object   kValue = null;
        Method setter = Reflection.findSetter(target.getClass(), beanProp, false);
        if (setter != null) {
            kType  = setter.getParameterTypes()[0];
            kValue = convertUserValue(original, value, kType);
            Reflection.set(target, setter, kValue);
            return true;
        } else {
            Field field = Reflection.findField(target.getClass(), beanProp, false);
            if (field != null) {
                kType  = field.getType();
                kValue = convertUserValue(original, value, kType);
                Reflection.set(target, field, kValue);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract a bean-style property name from the given string.
     * If the given string is <code>"a.b.xyz"</code> then returns <code>"xyz"</code> 
     */
    String getBeanPropertyName(String user) {
        String result = user;
        if (JPAProperties.isValidKey(user)) {
            result = JPAProperties.getBeanProperty(user);
        } else {
            int dot = user.lastIndexOf('.');
            if (dot != -1)
                result = user.substring(dot+1);
        }
        return result; 
    }
    
    
    /**
     * Convert the given value to a value consumable by OpenJPA kernel constructs.
     */
    Object convertUserValue(String key, Object value, Class<?> targetType) {
        if (JPAProperties.isValidKey(key)) 
            return JPAProperties.convertToKernelValue(targetType, key, value);
        if (value instanceof String) {
            if ("null".equals(value)) {
                return null;
            } else {
                String val = (String) value;
                int parenIndex = val.indexOf('(');
                if (!String.class.equals(targetType) && (parenIndex > 0)) {
                    val = val.substring(0, parenIndex);
                }
                return Strings.parse(val, targetType);
            } 
        } else if (value instanceof AutoDetachType) {
        	EnumSet<AutoDetachType> autoDetachFlags = EnumSet.noneOf(AutoDetachType.class);
        	autoDetachFlags.add((AutoDetachType)value);
        	return autoDetachFlags;
        } else if (value instanceof AutoDetachType[]) {
        	EnumSet<AutoDetachType> autoDetachFlags = EnumSet.noneOf(AutoDetachType.class);
        	autoDetachFlags.addAll(Arrays.asList((AutoDetachType[])value));
        	return autoDetachFlags;
        }
        return value;
    }

    private Map<String, Object> cloneProperties(Map<String, Object> properties) {
        if (properties != null) {
            properties = new HashMap<String, Object>(properties);
        }
        return properties;
    }
}

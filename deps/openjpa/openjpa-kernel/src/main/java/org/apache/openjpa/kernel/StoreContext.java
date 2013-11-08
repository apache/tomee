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
package org.apache.openjpa.kernel;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.UserException;

/**
 * Represents a set of managed objects and their environment.
 *
 * @since 0.4.0
 * @author Abe White
 */
public interface StoreContext {

    /**
     * Marker bitset to indicate that all field loads should be excluded in
     * the <code>find</code> methods of this interface.
     */
    public static final BitSet EXCLUDE_ALL = new BitSet(0);

    public static final int OID_NOVALIDATE = 2 << 0;
    public static final int OID_NODELETED = 2 << 1;
    public static final int OID_COPY = 2 << 2;
    public static final int OID_ALLOW_NEW = 2 << 3;

    /**
     * Return the broker for this context, if possible. Note that a broker
     * will be unavailable in remote contexts, and this method may return null.
     */
    public Broker getBroker();

    /**
     * Return the configuration associated with this context.
     */
    public OpenJPAConfiguration getConfiguration();

    /**
     * Return the (mutable) fetch configuration for loading objects from this
     * context.
     */
    public FetchConfiguration getFetchConfiguration();

    /**
     * Pushes a new fetch configuration that inherits from the current
     * fetch configuration onto a stack, and makes the new configuration
     * the active one.
     *
     * @since 1.1.0
     * @return the new fetch configuration
     */
    public FetchConfiguration pushFetchConfiguration();

    /**
     * Pushes the fetch configuration argument onto a stack, and makes the new configuration
     * the active one.
     *
     * @since 2.1.1
     * @return the new fetch configuration
     */
    public FetchConfiguration pushFetchConfiguration(FetchConfiguration fc);

    /**
     * Pops the fetch configuration from the top of the stack, making the
     * next one down the active one. This returns void to avoid confusion,
     * since fetch configurations tend to be used in method-chaining
     * patterns often.
     *
     * @since 1.1.0
     * @throws UserException if the fetch configuration stack is empty
     */
    public void popFetchConfiguration();

    /**
     * Return the current thread's class loader at the time this context
     * was obtained.
     */
    public ClassLoader getClassLoader();

    /**
     * Return the lock manager in use.
     */
    public LockManager getLockManager();

    /**
     * Return the store manager in use. This will be a wrapper around the
     * native store manager, which you can retrieve via
     * {@link DelegatingStoreManager#getInnermostDelegate}.
     */
    public DelegatingStoreManager getStoreManager();

    /**
     * Return the connection user name.
     */
    public String getConnectionUserName();

    /**
     * Return the connection password.
     */
    public String getConnectionPassword();

    /**
     * Return the instance for the given oid/object , or null if not
     * found in the L1 cache. 
     *
     * @param oid the object's id
     * @return the cached object, or null if not cached
     */
    public Object findCached(Object oid, FindCallbacks call);

    /**
     * Find the persistence object with the given oid. If
     * <code>validate</code> is true, the broker will check the store
     * for the object, and return null if it does not exist. If
     * <code>validate</code> is false, this method never returns null. The
     * broker will either return its cached instance, attempt to create a
     * hollow instance, or throw an <code>ObjectNotFoundException</code> if
     * unable to return a hollow instance.
     *
     * @param validate if true, validate that the instance exists in the
     * store and load fetch group fields, otherwise return
     * any cached or hollow instance
     */
    public Object find(Object oid, boolean validate, FindCallbacks call);

    /**
     * Return the objects with the given oids.
     *
     * @param oids the oids of the objects to return
     * @return the objects that were looked up, in the same order as the oids
     * parameter
     * @see #find(Object,boolean,FindCallbacks)
     */
    public Object[] findAll(Collection<Object> oids, boolean validate,
        FindCallbacks call);

    /**
     * Return the object with the given oid. If present, the
     * cached instance will be returned. Otherwise, the instance will be
     * initialized through the store as usual; however, in this case
     * the store will be passed the given execution data, and the
     * system will load the object according to the given fetch configuration
     * (or the context's configuration, if the given one is null).
     * Fields can optionally be excluded from required loading using the
     * <code>exclude</code> mask. By default this method does not find new
     * unflushed instances, validates, and does not throw an exception
     * if a cached instance has been deleted concurrently. These options
     * are controllable through the given <code>OID_XXX</code> flags.
     */
    public Object find(Object oid, FetchConfiguration fetch, BitSet exclude,
        Object edata, int flags);

    /**
     * Return the objects with the given oids.
     *
     * @see #find(Object,FetchConfiguration,BitSet,Object,int)
     */
    public Object[] findAll(Collection<Object> oids, FetchConfiguration fetch,
        BitSet exclude, Object edata, int flags);

    /**
     * Return an iterator over all instances of the given type. The iterator
     * should be closed with {@link org.apache.openjpa.util.ImplHelper#close} 
     * when no longer needed. This method delegates to 
     * {@link StoreManager#executeExtent}.
     */
    public Iterator<Object> extentIterator(Class<?> cls, boolean subs,
        FetchConfiguration fetch, boolean ignoreChanges);

    /**
     * Immediately load the given object's persistent fields. One might
     * use this action to make sure that an instance's fields are loaded
     * before transitioning it to transient. Note that this action is not
     * recursive. Any related objects that are loaded will not necessarily
     * have their fields loaded. Unmanaged target is ignored.
     *
     * @param fgOnly indicator as to whether to retrieve only fields
     * in the current fetch groups, or all fields
     * @see #retrieve
     */
    public void retrieve(Object pc, boolean fgOnly, OpCallbacks call);

    /**
     * Retrieve the given objects' persistent state. Unmanaged targets are
     * ignored.
     *
     * @param fgOnly indicator as to whether to retrieve only fields
     * @see #retrieve
     */
    public void retrieveAll(Collection<Object> objs, boolean fgOnly, OpCallbacks call);

    /**
     * Make the given instance embedded.
     *
     * @param obj the instance to embed; may be null to create a new instance
     * @param id the id to give the embedded state manager; may be
     * null for default
     * @param owner the owning state manager
     * @param ownerMeta the value in which the object is embedded
     * @return the state manager for the embedded instance
     */
    public OpenJPAStateManager embed(Object obj, Object id,
        OpenJPAStateManager owner, ValueMetaData ownerMeta);

    /**
     * Return the application or datastore identity class the given persistent
     * class uses for object ids.
     */
    public Class<?> getObjectIdType(Class<?> cls);

    /**
     * Create a new object id instance from the given value.
     *
     * @param cls the persistent class that uses this identity value
     * @param val an object id instance, stringified object id, or primary
     * key value
     */
    public Object newObjectId(Class<?> cls, Object val);

    /**
     * Return the set of classes that have been made persistent in the current
     * transaction.
     *
     * @since 0.3.4
     */
    public Collection<Class<?>> getPersistedTypes();

    /**
     * Return the set of classes that have been deleted in the current
     * transaction.
     *
     * @since 0.3.4
     */
    public Collection<Class<?>> getDeletedTypes();

    /**
     * Return the set of classes for objects that have been modified
     * in the current transaction.
     *
     * @since 0.3.4
     */
    public Collection<Class<?>> getUpdatedTypes();

    /**
     * Return a list of all managed instances.
     */
    public Collection<Object> getManagedObjects();

    /**
     * Return a list of current transaction instances.
     */
    public Collection<Object> getTransactionalObjects();

    /**
     * Return a list of instances which will become transactional upon
     * the next transaction.
     */
    public Collection<Object> getPendingTransactionalObjects();

    /**
     * Return a list of current dirty instances.
     */
    public Collection<Object> getDirtyObjects();

    /**
     * Whether to maintain the order in which objects are dirtied for
     * {@link #getDirtyObjects}. Default is the store manager's decision.
     */
    public boolean getOrderDirtyObjects();

    /**
     * Whether to maintain the order in which objects are dirtied for
     * {@link #getDirtyObjects}. Default is the store manager's decision.
     */
    public void setOrderDirtyObjects(boolean order);

    /**
     * Return the state manager for the given instance. Includes objects
     * made persistent in the current transaction. If <code>obj</code> is not
     * a managed type or is managed by another context, throw an exception.
     */
    public OpenJPAStateManager getStateManager(Object obj);

    /**
     * Return the lock level of the specified object.
     */
    public int getLockLevel(Object obj);

    /**
     * Returns the current version indicator for <code>o</code>.
     */
    public Object getVersion(Object obj);

    /**
     * Return whether the given object is dirty.
     */
    public boolean isDirty(Object obj);

    /**
     * Return whether the given object is transactional.
     */
    public boolean isTransactional(Object obj);

    /**
     * Make the given object transactional.
     *
     * @param pc instance to make transactional
     * @param updateVersion if true, the instance's version will be
     * incremented at the next flush
     */
    public void transactional(Object pc, boolean updateVersion, OpCallbacks call);

    /**
     * Make the given objects transactional.
     *
     * @param objs instances to make transactional
     * @param updateVersion if true, the instance's version will be
     * incremented at the next flush
     */
    public void transactionalAll(Collection<Object> objs, boolean updateVersion, OpCallbacks call);

    /**
     * Make the given object non-transactional.
     */
    public void nontransactional(Object pc, OpCallbacks call);

    /**
     * Make the given objects nontransactional.
     */
    public void nontransactionalAll(Collection<Object> objs, OpCallbacks call);

    /**
     * Return whether the given object is persistent.
     */
    public boolean isPersistent(Object obj);

    /**
     * Return whether the given object is a newly-created instance registered
     * with <code>broker</code>.
     */
    public boolean isNew(Object obj);

    /**
     * Return whether the given object is deleted.
     */
    public boolean isDeleted(Object obj);

    /**
     * Return the oid of the given instance.
     */
    public Object getObjectId(Object obj);

    /**
     * Detach mode constant to determine which fields are part of the
     * detached graph. Defaults to {@link DetachState#DETACH_LOADED}.
     */
    public int getDetachState();

    /**
     * Detach mode constant to determine which fields are part of the
     * detached graph. Defaults to {@link DetachState#DETACH_LOADED}.
     */
    public void setDetachState(int mode);

    /**
     * Whether objects accessed during this transaction will be added to the
     * store cache. Defaults to true.
     *
     * @since 0.3.4
     */
    public boolean getPopulateDataCache();

    /**
     * Whether to populate the store cache with objects used by this
     * transaction. Defaults to true.
     *
     * @since 0.3.4
     */
    public void setPopulateDataCache(boolean cache);

    /**
     * Whether memory usage is reduced during this transaction at the expense
     * of tracking changes at the type level instead of the instance level,
     * resulting in more aggressive cache invalidation.
     *
     * @since 1.0.0
     */
    public boolean isTrackChangesByType();

    /**
     * If a large number of objects will be created, modified, or deleted
     * during this transaction setting this option to true will reduce memory
     * usage if you perform periodic flushes by tracking changes at the type
     * level instead of the instance level, resulting in more aggressive cache
     * invalidation. Upon transaction commit the data cache will have to
     * more aggressively flush objects. The store cache will have to flush
     * instances of objects for each class of object modified during the
     * transaction. A side benefit of large transaction mode is that smaller
     * update messages can be used for
     * {@link org.apache.openjpa.event.RemoteCommitEvent}s. Defaults to false.
     *
     * @since 1.0.0
     */
    public void setTrackChangesByType(boolean largeTransaction);

    /**
     * Whether this context is using managed transactions.
     */
    public boolean isManaged();

    /**
     * Whether a logical transaction is active.
     */
    public boolean isActive();

    /**
     * Whether a data store transaction is active.
     */
    public boolean isStoreActive();

    /**
     * Begin a data store transaction.
     */
    public void beginStore();

    /**
     * Whether the broker has a dedicated connection based on the configured
     * connection retain mode and transaction status.
     */
    public boolean hasConnection();

    /**
     * Return the connection in use by the context, or a new connection if none.
     */
    public Object getConnection();

    /**
     * Synchronizes on an internal lock if the
     * <code>Multithreaded</code> flag is set to true. Make sure to call
	 * {@link #unlock} in a finally clause of the same method.
	 */
	public void lock ();

	/**
	 * Releases the internal lock.
	 */
	public void unlock ();

    /**
     * Return the 'JTA' connectionFactoryName
     */
    public String getConnectionFactoryName();

    /**
     * Set the 'JTA' ConnectionFactoryName.
     */
    public void setConnectionFactoryName(String connectionFactoryName);

    /**
     * Return the 'NonJTA' ConnectionFactoryName.
     */
    public String getConnectionFactory2Name();

    /**
     * Set the 'NonJTA' ConnectionFactoryName. 
     */
    public void setConnectionFactory2Name(String connectionFactory2Name);

    /**
     * Return the 'JTA' ConnectionFactory, looking it up from JNDI if needed.
     * 
     * @return the JTA connection factory or null if connectionFactoryName is blank.
     */
    public Object getConnectionFactory();

    /**
     * Return the 'NonJTA' ConnectionFactory, looking it up from JNDI if needed.
     * 
     * @return the NonJTA connection factory or null if connectionFactoryName is blank.
     */
    public Object getConnectionFactory2();
    
    /**
     * Indicate whether the oid can be found in the StoreContext's L1 cache or in the StoreManager cache.
     * @param oid List of ObjectIds for PersistenceCapables which may be found in memory.
     * @return true if the oid is available in memory (cached) otherwise false.
     * @since 2.0.0. 
     */
    public boolean isCached(List<Object> oid);
    
    /**
     * Affirms if this context will allow its managed instances to refer instances 
     * that are managed by other contexts. 
     * <B>Note</B>: Some specification (such as JPA) does not warranty predictable
     * behavior when strict group-like property of a persistent context (where managed 
     * instances can only refer to instances managed by the <em>same</em> context).
     * Please be aware of consequences when the flag is set to true.
     * 
     * @since 2.1
     */
    public void setAllowReferenceToSiblingContext(boolean flag);
    
    /**
     * Affirms if this context will allow its managed instances to refer instances 
     * that are managed by other contexts. 
     * 
     * @return false by default.
     * 
     * @since 2.1
     */
    public boolean getAllowReferenceToSiblingContext();


    /**
     * Set to <code>true</code> if the merge operation should trigger
     * a &#064;PostLoad lifecycle event.
     * @param allow PostLoad lifecycle events to be triggered on a merge operation
     */
    public void setPostLoadOnMerge(boolean allow);

    /**
     * Force sending a &#064;PostLoad lifecycle event while merging.
     *
     * @return <code>false</code> by default
     *
     * @since 2.2
     */
    public boolean getPostLoadOnMerge();

}

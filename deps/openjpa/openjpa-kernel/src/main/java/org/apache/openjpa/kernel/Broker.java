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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.transaction.Synchronization;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.event.CallbackModes;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

/**
 * The broker is the primary interface into the OpenJPA runtime. Each broker
 * maintains an independent object cache and an independent transactional
 * context.
 *
 * @since 0.4.0
 * @author Abe White
 */
public interface Broker
    extends Synchronization, Closeable, StoreContext,
    ConnectionRetainModes, DetachState, LockLevels,
    RestoreState, AutoClear, AutoDetach, CallbackModes {

    /**
     * Set the broker's behavior for implicit actions such as flushing,
     * automatic detachment, and exceptions thrown by managed instances outside
     * a broker operation. A broker's implicit behavior can only be set once;
     * after the first invocation with non-null arguments,
     * subsequent invocations of this method are ignored.
     */
    public void setImplicitBehavior(OpCallbacks call,
        RuntimeExceptionTranslator ex);

    /**
     * Return the factory that produced this broker.
     */
    public BrokerFactory getBrokerFactory();

    /**
     * Return the connection retain mode for this broker.
     */
    public int getConnectionRetainMode();

    /**
     * Return the managed runtime in use.
     */
    public ManagedRuntime getManagedRuntime();

    /**
     * Return the inverse manager in use.
     *
     * @since 0.3.2
     */
    public InverseManager getInverseManager();

    /**
     * Whether the broker or its managed instances are used in a multithreaded
     * environment.
     */
    public boolean getMultithreaded();

    /**
     * Whether the broker or its managed instances are used in a multithreaded
     * environment.
     */
    public void setMultithreaded(boolean multi);

    /**
     * Whether to take into account changes in the transaction when executing
     * a query or iterating an extent.
     */
    public boolean getIgnoreChanges();

    /**
     * Whether to take into account changes in the transaction when executing
     * a query or iterating an extent.
     */
    public void setIgnoreChanges(boolean ignore);

    /**
     * Whether to allow nontransactional access to persistent state.
     */
    public boolean getNontransactionalRead();

    /**
     * Whether to allow nontransactional access to persistent state.
     */
    public void setNontransactionalRead(boolean read);

    /**
     * Whether to allow nontransactional changes to persistent state.
     */
    public boolean getNontransactionalWrite();

    /**
     * Whether to allow nontransactional changes to persistent state.
     */
    public void setNontransactionalWrite(boolean write);

    /**
     * Whether to restore an object's original state on rollback.
     */
    public int getRestoreState();

    /**
     * Whether to restore an object's original state on rollback.
     */
    public void setRestoreState(int restore);

    /**
     * Whether to use optimistic transactional semantics.
     */
    public boolean getOptimistic();

    /**
     * Whether to use optimistic transactional semantics.
     */
    public void setOptimistic(boolean opt);

    /**
     * Whether objects retain their persistent state on transaction commit.
     */
    public boolean getRetainState();

    /**
     * Whether objects retain their persistent state on transaction commit.
     */
    public void setRetainState(boolean retain);

    /**
     * Whether objects clear when entering transactions.
     */
    public int getAutoClear();

    /**
     * Whether objects clear when entering transactions.
     */
    public void setAutoClear(int clear);

    /**
     * Whether to check for a global transaction upon every managed,
     * non-transactional operation. Defaults to false.
     */
    public boolean getSyncWithManagedTransactions();

    /**
     * Whether to check for a global transaction upon every managed,
     * non-transactional operation. Defaults to false.
     */
    public void setSyncWithManagedTransactions(boolean resync);

    /**
     * Bit flags marked in {@link AutoDetach} which indicate when persistent
     * managed objects should be automatically detached in-place.
     */
    public int getAutoDetach();

    /**
     * Bit flags marked in {@link AutoDetach} which indicate when persistent
     * managed objects should be automatically detached in-place.
     */
    public void setAutoDetach(int flags);

    /**
     * Bit flags marked in {@link AutoDetach} which indicate when persistent
     * managed objects should be automatically detached in-place.
     */
    public void setAutoDetach(int flag, boolean on);
    
    /**
     * Retrieve the current properties for this broker Some of these properties
     * may have been changed from the original configuration.
     * 
     * @return the changed properties
     * 
     * @since 2.0.0
     */
    public Map<String, Object> getProperties();
    
    /**
     * Return the supported properties for this broker as property keys. If a
     * property has multiple keys, all keys will be returned.
     * 
     * @since 2.0.0
     */
    public Set<String> getSupportedProperties();

    /**
     * Whether to treat relations to detached instances during persist
     * operations as new or as pseudo-hollow instances.
     */
    public boolean isDetachedNew();

    /**
     * Whether to treat relations to detached instances as new.
     */
    public void setDetachedNew(boolean isNew);

    /**
     * Whether to also evict an object from the store cache when it is
     * evicted through this broker.
     */
    public boolean getEvictFromDataCache();

    /**
     * Whether to also evict an object from the store cache when it is
     * evicted through this broker.
     */
    public void setEvictFromDataCache(boolean evict);

    /**
     * Put the specified key-value pair into the map of user objects. Use
     * a value of null to remove the key.
     *
     * @since 0.3.2
     */
    public Object putUserObject(Object key, Object val);

    /**
     * Get the value for the specified key from the map of user objects.
     *
     * @since 0.3.2
     */
    public Object getUserObject(Object key);

    /**
     * Register a listener for transaction-related events.
     *
     * @since 0.2.5
     */
    public void addTransactionListener(Object listener);

    /**
     * Remove a listener for transaction-related events.
     *
     * @since 0.2.5
     */
    public void removeTransactionListener(Object listener);
    
    /**
     * Gets an umodifiable collection of currently registered lsteners.
     * 
     * @since 2.0.0
     */
    public Collection<Object> getTransactionListeners();

    /**
     * The callback mode for handling exceptions from transaction event
     * listeners.
     */
    public int getTransactionListenerCallbackMode();

    /**
     * The callback mode for handling exceptions from transaction event
     * listeners.
     */
    public void setTransactionListenerCallbackMode(int mode);

    /**
     * Register a listener for lifecycle-related events on the specified
     * classes. If the classes are null, all events will be propagated to
     * the listener.
     *
     * @since 0.3.3
     */
    public void addLifecycleListener(Object listener, Class[] classes);

    /**
     * Remove a listener for lifecycle-related events.
     *
     * @since 0.3.3
     */
    public void removeLifecycleListener(Object listener);

    /**
     * Return the lifecycle event manager associated with the broker.
     */
    public LifecycleEventManager getLifecycleEventManager();

    /**
     * The callback mode for handling exceptions from lifecycle event listeners.
     */
    public int getLifecycleListenerCallbackMode();

    /**
     * The callback mode for handling exceptions from lifecycle event listeners.
     */
    public void setLifecycleListenerCallbackMode(int mode);

    
    /**
     * Affirms if this receiver is caching prepared queries.
     *  
     * @since 2.0.0
     */
    public boolean getCachePreparedQuery();
    
    /**
     * Sets whether this receiver will cache prepared queries during its 
     * lifetime. The cache configured at BrokerFactory level is not affected by 
     * setting it inactive for this receiver. 
     * 
     * @since 2.0.0
     */
    public void setCachePreparedQuery(boolean flag);

    /**
     * Begin a transaction.
     */
    public void begin();

    /**
     * Commit the current transaction.
     */
    public void commit();

    /**
     * Rollback the current transaction.
     */
    public void rollback();

    /**
     * Attempt to synchronize with a current managed transaction, returning
     * true if successful, false if no managed transaction is active.
     */
    public boolean syncWithManagedTransaction();

    /**
     * Issue a commit and then start a new transaction. This is identical to:
     * <pre> broker.commit (); broker.begin ();
     * </pre> except that the broker's internal atomic lock is utilized,
     * so this method can be safely executed from multiple threads.
     *
     * @see #commit()
     * @see #begin()
     * @since 0.2.4
     */
    public void commitAndResume();

    /**
     * Issue a rollback and then start a new transaction. This is identical to:
     * <pre> broker.rollback (); broker.begin ();
     * </pre> except that the broker's internal atomic lock is utilized,
     * so this method can be safely executed from multiple threads.
     *
     * @see #rollback()
     * @see #begin()
     * @since 0.2.4
     */
    public void rollbackAndResume();

    /**
     * Return whether the current transaction has been marked for rollback.
     */
    public boolean getRollbackOnly();

    /**
     * Mark the current transaction for rollback.
     */
    public void setRollbackOnly();

    /**
     * Mark the current transaction for rollback with the specified cause
     * of the rollback.
     *
     * @since 0.9.7
     */
    public void setRollbackOnly(Throwable cause);

    /** 
     * Returns the Throwable that caused the transaction to be
     * marked for rollback. 
     *  
     * @return the Throwable, or null if none was given
     *
     * @since 0.9.7
     */
    public Throwable getRollbackCause();

    /**
     * Set a transactional savepoint where operations after this savepoint
     * will be rolled back.
     */
    public void setSavepoint(String name);

    /**
     * Rollback the current transaction to the last savepoint.
     * Savepoints set after this one will become invalid.
     */
    public void rollbackToSavepoint();

    /**
     * Rollback the current transaction to the given savepoint name.
     * Savepoints set after this one will become invalid.
     */
    public void rollbackToSavepoint(String name);

    /**
     * Release the last set savepoint and any resources associated with it.
     * The given savepoint and any set after it will become invalid.
     */
    public void releaseSavepoint();

    /**
     * Release the savepoint and any resources associated with it.
     * The given savepoint and any set after it will become invalid.
     */
    public void releaseSavepoint(String name);

    /**
     * Flush all transactional instances to the data store. This method may
     * set the rollback only flag on the current transaction if it encounters
     * an error.
     *
     * @since 0.2.5
     */
    public void flush();

    /**
     * Run pre-flush actions on transactional objects, including
     * persistence-by-reachability, inverse relationship management,
     * deletion of dependent instances, and instance callbacks.
     * Transaction listeners are not invoked.
     *
     * @since 0.3.3
     */
    public void preFlush();

    /**
     * Validate the changes made in this transaction, reporting any optimistic
     * violations, constraint violations, etc. In a datastore transaction or
     * a flushed optimistic transaction, this method will act just like
     * {@link #flush}. In an optimistic transaction that has not yet begun a
     * datastore-level transaction, however, it will only report exceptions
     * that would occur on flush, without retaining any datastore resources.
     */
    public void validateChanges();

    /**
     * Persist the given object.
     */
    public void persist(Object obj, OpCallbacks call);

    /**
     * Persist the given objects.
     */
    public void persistAll(Collection objs, OpCallbacks call);

    /**
     * Make the given instance persistent. Unlike other persist operations,
     * this method does <b>not</b> immediately cascade to fields marked
     * {@link ValueMetaData#CASCADE_IMMEDIATE}.
     *
     * @param pc the instance to persist
     * @param id the id to give the state manager; may be null for default
     * @return the state manager for the newly persistent instance
     */
    public OpenJPAStateManager persist(Object pc, Object id, OpCallbacks call);

    /**
     * Delete the given object.
     */
    public void delete(Object pc, OpCallbacks call);

    /**
     * Delete the given objects.
     */
    public void deleteAll(Collection objs, OpCallbacks call);

    /**
     * Release the given object from management. This operation is not
     * recursive.
     */
    public void release(Object pc, OpCallbacks call);

    /**
     * Release the given objects from management. This operation is not
     * recursive.
     */
    public void releaseAll(Collection objs, OpCallbacks call);

    /**
     * Refresh the state of the given object.
     */
    public void refresh(Object pc, OpCallbacks call);

    /**
     * Refresh the state of the given objects.
     */
    public void refreshAll(Collection objs, OpCallbacks call);

    /**
     * Evict the given object.
     */
    public void evict(Object pc, OpCallbacks call);

    /**
     * Evict the given objects.
     */
    public void evictAll(Collection objs, OpCallbacks call);

    /**
     * Evict all clean objects.
     */
    public void evictAll(OpCallbacks call);

    /**
     * Evict all persistent-clean and persistent-nontransactional
     * instances in the given {@link Extent}.
     */
    public void evictAll(Extent extent, OpCallbacks call);

    /**
     * Detach all objects in place.  A flush will be performed before
     * detaching the entities.
     */
    public void detachAll(OpCallbacks call);

    /**
     * Detach all objects in place, with the option of performing a
     * flush before doing the detachment.
     * @param call Persistence operation callbacks
     * @param flush boolean value to indicate whether to perform a
     * flush before detaching the entities (true, do the flush;
     * false, don't do the flush)
     */
    public void detachAll(OpCallbacks call, boolean flush);

    /**
     * Detach the specified object from the broker.
     *
     * @param pc the instance to detach
     * @return the detached instance
     */
    public Object detach(Object pc, OpCallbacks call);

    /**
     * Detach the specified objects from the broker. The objects returned can
     * be manipulated and re-attached with {@link #attachAll}. The
     * detached instances will be unmanaged copies of the specified parameters,
     * and are suitable for serialization and manipulation outside
     * of a OpenJPA environment. When detaching instances, only fields
     * in the current {@link FetchConfiguration} will be traversed. Thus,
     * to detach a graph of objects, relations to other persistent
     * instances must either be in the <code>default-fetch-group</code>,
     * or in the current custom {@link FetchConfiguration}.
     *
     * @param objs the instances to detach
     * @return the detached instances
     */
    public Object[] detachAll(Collection objs, OpCallbacks call);

    /**
     * Import the specified detached object into the broker.
     *
     * @param pc instance to import
     * @return the re-attached instance
     * @param copyNew whether to copy new instances
     */
    public Object attach(Object pc, boolean copyNew, OpCallbacks call);

    /**
     * Import the specified objects into the broker. Instances that were
     * previously detached from this or another broker will have their
     * changed merged into the persistent instances. Instances that
     * are new will be persisted as new instances.
     *
     * @param objs array of instances to import
     * @return the re-attached instances
     * @param copyNew whether to copy new instances
     */
    public Object[] attachAll(Collection objs, boolean copyNew,
        OpCallbacks call);

    /**
     * Create a new instance of type <code>cls</code>. If <code>cls</code> is
     * an interface or an abstract class whose abstract methods follow the
     * JavaBeans convention, this method will create a concrete implementation
     * according to the metadata that defines the class.
     * Otherwise, if <code>cls</code> is a managed type, this will return an
     * instance of the specified class.
     *
     * @throws IllegalArgumentException if <code>cls</code> is not a managed
     * type or interface.
     */
    public Object newInstance(Class cls);

    /**
     * Returns <code>true</code> if <code>obj</code> is a detached object
     * (one that can be reattached to a {@link Broker} via a call to
     * {@link Broker#attach}); otherwise returns <code>false</code>.
     */
    public boolean isDetached(Object obj);

    /**
     * Return an extent of the given class, optionally including subclasses.
     */
    public Extent newExtent(Class cls, boolean subs);

    /**
     * Create a new query from the given data, with the given candidate class
     * and language.
     */
    public Query newQuery(String language, Class cls, Object query);

    /**
     * Create a new query in the given language.
     */
    public Query newQuery(String language, Object query);

    /**
     * Returns a {@link Seq} for the datastore identity values of the
     * specified persistent class, or null if the class' identity cannot be
     * represented as a sequence.
     */
    public Seq getIdentitySequence(ClassMetaData meta);

    /**
     * Returns a {@link Seq} for the generated values of the specified
     * field, or null if the field is not generated.
     */
    public Seq getValueSequence(FieldMetaData fmd);

    /**
     * Ensure that the given instance is locked at the given lock level.
     *
     * @param pc the object to lock
     * @param level the lock level to use
     * @param timeout the number of milliseconds to wait for the lock before
     * giving up, or -1 for no limit
     * @since 0.3.1
     */
    public void lock(Object pc, int level, int timeout, OpCallbacks call);

    /**
     * Ensure that the given instance is locked at the current lock level, as
     * set in the {@link FetchConfiguration} for the broker.
     *
     * @since 0.3.1
     */
    public void lock(Object pc, OpCallbacks call);

    /**
     * Ensure that the given instances are locked at the given lock level.
     *
     * @param objs the objects to lock
     * @param level the lock level to use
     * @param timeout the number of milliseconds to wait for the lock before
     * giving up, or -1 for no limit
     * @since 0.3.1
     */
    public void lockAll(Collection objs, int level, int timeout,
        OpCallbacks call);

    /**
     * Ensure that the given instances are locked at the current lock level, as
     * set in the {@link FetchConfiguration} for the broker.
     *
     * @since 0.3.1
     */
    public void lockAll(Collection objs, OpCallbacks call);

    /**
     * Cancel all pending data store statements. If statements are cancelled
     * while a flush is in progress, the transaction rollback only flag will
     * be set.
     *
     * @return true if any statements were cancelled, false otherwise
     * @since 0.3.1
     */
    public boolean cancelAll();

    /**
     * Mark the given class as dirty within the current transaction.
     *
     * @since 0.3.0
     */
    public void dirtyType(Class cls);

    /**
     * Begin a logical operation. This indicates to the broker the
     * granularity of an operation which may require pre/post operation
     * side-effects, such as non-tx detach.
     * Will lock the broker until the {@link #endOperation} is called.
     *
     * @param syncTrans whether instances may be loaded/modified during
     * this operation requiring a re-check of global tx
     * @return whether this is the outermost operation on the stack
     */
    public boolean beginOperation(boolean syncTrans);

    /**
     * End a logical operation. This indicates to the broker the
     * granularity of an operation which may require pre/post operation
     * side-effects, such as non-tx detach. Unlocks the given broker.
     *
     * @return whether this is the outermost operation on the stack
     */
    public boolean endOperation();

    /**
     * Whether the broker is closed.
     */
    public boolean isClosed();

    /**
     * Whether {@link #close} has been invoked, though the broker might 
     * remain open until the current managed transaction completes.
     */
    public boolean isCloseInvoked();

    /**
     * Close the broker.
     */
    public void close();

    /**
     * Throw an exception if this broker has been closed.
     */
    public void assertOpen();

    /**
     * Throw an exception if there is no active transaction.
     */
    public void assertActiveTransaction();

    /**
     * Throw an exception if there is no transaction active and
     * nontransactional reading is not enabled.
	 */
	public void assertNontransactionalRead ();

	/**
	 * Throw an exception if a write operation is not permitted (there is
	 * no active transaction and nontransactional writing is not enabled).
	 */
	public void assertWriteOperation ();
}

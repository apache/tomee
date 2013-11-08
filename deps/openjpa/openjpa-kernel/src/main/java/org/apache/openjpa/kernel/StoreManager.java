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
import java.util.List;

import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.ValueStrategies;

/**
 * Interface to be implemented by data store mechanisms to interact with
 * this runtime.
 *
 * @author Abe White
 */
public interface StoreManager
    extends Closeable {

    public static final int VERSION_LATER = 1;
    public static final int VERSION_EARLIER = 2;
    public static final int VERSION_SAME = 3;
    public static final int VERSION_DIFFERENT = 4;

    public static final int FORCE_LOAD_NONE = 0;
    public static final int FORCE_LOAD_DFG = 1;
    public static final int FORCE_LOAD_REFRESH = 3;
    public static final int FORCE_LOAD_ALL = 2;

    /**
     * Set a reference to the corresponding context. This method
     * will be called before the store manager is used. The store manager
     * is responsible for pulling any necessary configuration data from the
     * context, including the transaction mode and connection retain mode.
     */
    public void setContext(StoreContext ctx);

    /**
     * Notification that an optimistic transaction has started. This method
     * does not replace the {@link #begin} method, which will still be called
     * when a true data store transaction should begin.
     */
    public void beginOptimistic();

    /**
     * Notification that an optimistic transaction was rolled back before
     * a data store transaction ever began.
     */
    public void rollbackOptimistic();

    /**
     * Begin a data store transaction. After this method is called,
     * it is assumed that all further operations are operating in a single
     * transaction that can be committed or rolled back. If optimistic
     * transactions are in use, this method will only be called when the
     * system requires a transactionally consistent connection due to a
     * user request to flush or commit the transaction. In this case, it
     * is possible that the optimistic transaction does not have the latest
     * versions of all instances (i.e. another transaction has modified the
     * same instances and committed since the optimistic transaction started).
     * On commit, an exception must be thrown on any attempt to overwrite
     * data for an instance with an older version.
     *
     * @since 0.2.5
     */
    public void begin();

    /**
     * Commit the current data store transaction.
     */
    public void commit();

    /**
     * Rollback the current data store transaction.
     */
    public void rollback();

    /**
     * Verify that the given instance exists in the data store; return false
     * if it does not.
     */
    public boolean exists(OpenJPAStateManager sm, Object edata);
    
    /**
     * Verify that the given instance exists in the data store in memory; return false
     * if it does not. When an object is found in memory the corresponding element of 
     * the BitSet is set to 1. 
     */
    public boolean isCached(List<Object> oids, BitSet edata);

    /**
     * Update the version information in the given state manager to the
     * version stored in the data store.
     *
     * @param sm the instance to check
     * @param edata the current execution data, or null if not
     * given to the calling method of the context
     * @return true if the instance still exists in the
     * datastore and is up-to-date, false otherwise
     */
    public boolean syncVersion(OpenJPAStateManager sm, Object edata);

    /**
     * Initialize the given state manager. The object id of the
     * state manager will be set, and the state manager's metadata be set to
     * the class of the instance to load, or possibly one of its superclasses.
     * Initialization involves first calling the
     * {@link OpenJPAStateManager#initialize} method with
     * a new instance of the correct type constructed with the
     * {@link org.apache.openjpa.enhance.PCRegistry#newInstance(Class,
     * org.apache.openjpa.enhance.StateManager, boolean)} method
     * (this will reset the state manager's metadata if the actual type was a
     * subclass). After instance initialization, load any the fields for the
     * given fetch configuration that can be efficiently retrieved. If any of
     * the configured fields are not loaded in this method, they will be
     * loaded with a subsequent call to {@link #load}. If this method is
     * called during a data store transaction, the instance's database record
     * should be locked. Version information can be loaded if desired through
     * the {@link OpenJPAStateManager#setVersion} method.
     *
     * @param sm the instance to initialize
     * @param state the lifecycle state to initialize the state manager with
     * @param fetch configuration for how to load the instance
     * @param edata the current execution data, or null if not
     * given to the calling method of the broker
     * @return true if the matching instance exists in the data
     * store, false otherwise
     */
    public boolean initialize(OpenJPAStateManager sm, PCState state,
        FetchConfiguration fetch, Object edata);

    /**
     * Load the given state manager.
     * Note that any collection or map types loaded into the state manager
     * will be proxied with the correct type; therefore the store manager
     * does not have to load the same concrete collection/map types as the
     * instance declares. However, array types must be consistent with the
     * array type stored by the persistence capable instance. If this method
     * is called during a data store transaction, the instance should be
     * locked. If the given state manager does not have its version set
     * already, version information can be loaded if desired through the
     * {@link OpenJPAStateManager#setVersion} method.
     *
     * @param sm the instance to load
     * @param fields set of fields to load; all field indexes in this
     * set must be loaded; this set is mutable
     * @param fetch the fetch configuration to use when loading
     * related objects
     * @param lockLevel attempt to load simple fields at this lock level;
     * relations should be loaded at the read lock level
     * of the fetch configuration
     * @param edata the current execution data, or null if not
     * given to the calling method of the broker
     * @return false if the object no longer exists in the
     * database, true otherwise
     */
    public boolean load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, int lockLevel, Object edata);

    /**
     * Initialize, load, or validate the existance of all of the given
     * objects. This method is called from various broker methods that act
     * on multiple objects, such as {@link StoreContext#retrieveAll}. It gives
     * the store manager an opportunity to efficiently batch-load data for
     * several objects. Each of the given state managers will be in one of
     * three states, each requiring a different action:
     * <ul>
     * <li><code>stateManager.getO () == null</code>: An
     * uninitialized state manager. Perform the same actions as in
     * {@link #initialize}.
     * <li><code>load != FORCE_LOAD_NONE || stateManager.getPCState ()
     * == PCState.HOLLOW</code>: A hollow state manager, or one whose
     * fields must be loaded because this is a refresh or retrieve action.
     * Peform the same actions as in {@link #load}, choosing the fields
     * to load based on the fetch configuration, or loading all fields
     * if <code>load == FORCE_LOAD_ALL</code>. Any required fields left
     * unloaded will cause a subsequent invocation of {@link #load} on
     * the individual object in question.</li>
     * <li><code>load == FORCE_LOAD_NONE &amp;&amp;
     * stateManager.getPCState () != PCState.HOLLOW</code>: A non-hollow
     * state manager. Perform the same actions as in {@link #exists},
     * and load additional state if desired. Non-hollow objects will only
     * be included outside of refresh invocations if a user calls
     * <code>findAll</code> with the <code>validate</code>
     * parameter set to <code>true</code>.</li>
     * </ul> 
     * Store managers that cannot efficiently batch load can simply test
     * for these conditions and delegate to the proper methods.
     *
     * @param sms the state manager instances to load
     * @param state the lifecycle state to initialize uninitialized
     * state managers with; may be null if no uninitialized
     * instances are included in <code>sms</code>
     * @param load one of the FORCE_LOAD_* constants describing the
     * fields to force-load if this is a refresh or retrieve action
     * @param fetch the current fetch configuration to use when loading
     * related objects
     * @param edata the current execution data, or null if not
     * given to the calling method of the broker
     * @return a collection of the state manager identities for
     * which no data store record exists
     * @see org.apache.openjpa.util.ImplHelper#loadAll
     */
    public Collection<Object> loadAll(Collection<OpenJPAStateManager> sms, PCState state, int load,
        FetchConfiguration fetch, Object edata);

    /**
     * Notification that the given state manager is about to change its
     * lifecycle state. The store manager is not required to do anything in
     * this method, but some back ends may need to.
     *
     * @since 0.3.0
     */
    public void beforeStateChange(OpenJPAStateManager sm, PCState fromState,
        PCState toState);

    /**
     * Flush the given state manager collection to the datastore, returning
     * a collection of exceptions encountered during flushing.
     * The given collection may include states that do not require data
     * store action, such as persistent-clean instances or persistent-dirty
     * instances that have not been modified since they were last flushed.
     * For datastore updates and inserts, the dirty, non-flushed fields of
     * each state should be flushed. New instances without an assigned object
     * id should be given one via {@link OpenJPAStateManager#setObjectId}. New
     * instances with value-strategy fields that have not been assigned yet
     * should have their fields set. Datastore version information should be
     * updated during flush, and the state manager's version indicator
     * updated through the {@link OpenJPAStateManager#setNextVersion} method.
     * The current version will roll over to this next version upon successful
     * commit.
     *
     * @see org.apache.openjpa.util.ApplicationIds#assign()
     */
    public Collection<Exception> flush(Collection<OpenJPAStateManager> sms);

    /**
     * Assign an object id to the given new instance. Return false if the
     * instance cannot be assigned an identity because a flush is required
     * (for example, the identity is determined by the datastore on insert).
     * For application identity instances, the assigned object id should be
     * based on field state. The implementation is responsible for using the
     * proper value strategy according to the instance metadata. This method
     * is called the first time a user requests the oid of a new instance
     * before flush.
     *
     * @param preFlush whether this assignment is being requested by the
     * system as part of pre-flush activities, and can
     * be ignored if it is more efficient to assign within {@link #flush}
     * @see org.apache.openjpa.util.ImplHelper#generateFieldValue
     * @see org.apache.openjpa.util.ImplHelper#generateIdentityValue
     * @see org.apache.openjpa.util.ApplicationIds#assign()
     * @since 0.3.3
     */
    public boolean assignObjectId(OpenJPAStateManager sm, boolean preFlush);

    /**
     * Assign a value to the given field. Return false if the value cannot
     * be assigned because a flush is required (for example, the field value
     * is determined by the datastore on insert). This method is called the
     * first time a user requests the value of a field with a value-strategy
     * on a new instance before flush.
     *
     * @param preFlush whether this assignment is being requested by the
     * system as part of pre-flush activities, and can
     * be ignored if it is more efficient to assign within {@link #flush}
     * @see org.apache.openjpa.util.ImplHelper#generateFieldValue
     * @since 0.4.0
     */
    public boolean assignField(OpenJPAStateManager sm, int field,
        boolean preFlush);

    /**
     * Return the persistent class for the given data store identity value.
     * If the given value is not a datastore identity object, return null.
     *
     * @since 0.3.0
     */
    public Class<?> getManagedType(Object oid);

    /**
     * Return the class used by this StoreManager for datastore identity
     * values. The given metadata may be null, in which case the return
     * value should the common datastore identity class for all classes, or
     * null if this store manager does not use a common identity class.
     */
    public Class<?> getDataStoreIdType(ClassMetaData meta);

    /**
     * Copy the given object id value. Use the described type of the given
     * metadata, which may be a subclass of the given oid's described type.
     */
    public Object copyDataStoreId(Object oid, ClassMetaData meta);

    /**
     * Create a new unique datastore identity for the given type from
     * the given oid value (presumably pk, stringified oid, or oid instance).
     */
    public Object newDataStoreId(Object oidVal, ClassMetaData meta);

    /**
     * Return a connection to the data store suitable for client use. If
     * this method is called during a data store transaction, thie connection
     * must be transactional. If no connection is in use, this method should
     * create one to return.
     */
    public Object getClientConnection();

    /**
     * Instruct the store to retain a connection for continued use. This
     * will be invoked automatically based on the user's configured connection
     * retain mode.
     */
    public void retainConnection();

    /**
     * Instruct the store to release a retained connection. This
     * will be invoked automatically based on the user's configured connection
     * retain mode.
     */
    public void releaseConnection();

    /**
     * Cancel all pending data store statements.
     *
     * @return true if any statements cancelled, false otherwise
     * @since 0.3.1
     */
    public boolean cancelAll();

    /**
     * Return a provider for all instances of the given candidate class,
     * optionally including subclasses. The given candidate may be an
     * unmapped type with mapped subclasses. If the provider is iterated
     * within a data store transaction, returned instances should be locked.
     */
    public ResultObjectProvider executeExtent(ClassMetaData meta,
        boolean subclasses, FetchConfiguration fetch);

    /**
     * Return a query implementation suitable for this store. If the query
     * is iterated within a data store transaction, returned instances should
     * be locked. Return null if this store does not support native execution
     * of the given language. OpenJPA can execute JPQL in memory even without
     * back end support.
     *
     * @param language the query language
     */
    public StoreQuery newQuery(String language);

    /**
     * Return a fetch configuration suitable for this runtime. Typically
     * will be or extend <code>FetchConfigurationImpl</code>.
     */
    public FetchConfiguration newFetchConfiguration();

    /**
     * Compare the two version objects.
     *
     * @param state the state manager for the object
     * @param v1 the first version object to compare
     * @param v2 the second version object to compare
     * @return <ul>
     * <li>{@link #VERSION_LATER} if <code>v1</code>
     * is later than <code>v2</code></li>
     * <li>{@link #VERSION_EARLIER} if <code>v1</code>
     * is earlier than <code>v2</code></li>
     * <li>{@link #VERSION_SAME} if <code>v1</code>
     * is the same as <code>v2</code></li>
     * <li>{@link #VERSION_DIFFERENT} if <code>v1</code>
     * is different from <code>v2</code>, but the time
     * difference of the versions cannot be determined</li>
     * </ul>
     */
    public int compareVersion(OpenJPAStateManager state, Object v1, Object v2);

    /**
     * Return a sequence that generates datastore identity values for the
     * given class. This method will only be called when the identity strategy
     * for the class is one of:
     * <ul>
     * <li>{@link ValueStrategies#NATIVE}</li>
     * <li>{@link ValueStrategies#AUTOASSIGN}</li>
     * <li>{@link ValueStrategies#INCREMENT}</li>
     * </ul>
     * If the identity strategy cannot be represented as a sequence, return
     * null.
     *
     * @since 0.4.0
     */
    public Seq getDataStoreIdSequence(ClassMetaData forClass);

    /**
     * Return a sequence that generates values for the given field. This
     * method will only be called when the value strategy for the field
     * is one of:
     * <ul>
     * <li>{@link ValueStrategies#NATIVE}</li>
     * <li>{@link ValueStrategies#AUTOASSIGN}</li>
     * <li>{@link ValueStrategies#INCREMENT}</li>
     * </ul> If the value strategy cannot be represented as a sequence, return
     * null.
     *
     * @since 0.4.0
     */
    public Seq getValueSequence(FieldMetaData forField);

    /**
     * Free any resources this store manager is using.
     *
     * @since 0.2.5
     */
    public void close ();
}

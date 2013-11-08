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
package org.apache.openjpa.abstractstore;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.FetchConfigurationImpl;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.Id;
import org.apache.openjpa.util.ImplHelper;

/**
 * Abstract store manager implementation to ease development of custom
 * OpenJPA back-ends. A concrete subclass must define implementations for the
 * following methods:
 * <ul>
 * <li>{@link StoreManager#exists}</li>
 * <li>{@link #initialize}</li>
 * <li>{@link #load}</li>
 * <li>{@link
 * #flush(Collection,Collection,Collection,Collection,Collection)}</li>
 * <li>{@link #executeExtent}</li>
 * </ul> Additionally, subclasses should not attempt to acquire resources
 * until {@link #open} has been called. Store manager instances might be
 * created to call metadata methods such as {@link #newConfiguration} or
 * {@link #getUnsupportedOptions} and never opened. These instances should
 * not consume any data store resources.
 *  Notes:
 * <ul>
 * <li>The {@link StoreManager#initialize} method is responsible
 * for creating new instances of objects freshly loaded from the
 * database. The method will be invoked with a {@link OpenJPAStateManager}
 * that the newly-loaded object should be associated with. To create the
 * new object and set up this association correctly, the implementation
 * should use the {@link OpenJPAStateManager#initialize} method.</li>
 * <li>If your data store supports some sort of transaction or
 * unit of work, you should override the {@link #begin}, {@link #commit},
 * and {@link #rollback} methods.</li>
 * <li>This class provides no infrastructure support for optimistic
 * transactions. To provide optimistic transaction support:
 * <ul>
 * <li>Override {@link #beginOptimistic}, {@link #rollbackOptimistic},
 * and {@link #syncVersion}.</li>
 * <li>Override {@link #getUnsupportedOptions} to not include {@link
 * OpenJPAConfiguration#OPTION_OPTIMISTIC} in the list of unsupported
 * options.</li>
 * <li>Ensure that your flush implementation sets the next
 * version for each modified object via the {@link
 * OpenJPAStateManager#setNextVersion} method.</li>
 * <li>If your version object does not implement {@link Comparable},
 * override {@link #compareVersion}, which relies on the
 * {@link Comparable#compareTo} method.</li>
 * </ul></li>
 * <li>If your data store supports a mechanism for automatically
 * generating and managing identity values (or if you want to
 * provide that facility on top of your data store), implement
 * the {@link #getDataStoreIdSequence} method if you want to use a
 * <code>long</code> as your datastore identity type and are
 * happy with OpenJPA's {@link Id} class. To use another datastore identity
 * type, override {@link #getManagedType},
 * {@link #getDataStoreIdType}, {@link #copyDataStoreId}, and
 * {@link #newDataStoreId} instead. In either case, override
 * {@link #getUnsupportedOptions} to not include
 * {@link OpenJPAConfiguration#OPTION_ID_DATASTORE} in the list of
 * unsupported options.</li>
 * <li>If your data store does not support queries (or if you do
 * not want to convert OpenJPA's query parse tree into a
 * datastore-specific query), you still have two options in terms
 * of query execution:
 * <ul>
 * <li><em>In-memory execution</em>: If you
 * execute a query against an extent or a class, OpenJPA will
 * automatically load the full extent of objects into memory and
 * execute the query in memory.</li>
 * <li><em>openjpa.MethodQL</em>: MethodQL allows
 * you to use the query APIs to execute a method that finds
 * data in your back-end and returns that data as a
 * {@link org.apache.openjpa.lib.rop.ResultList}. For more details on
 * MethodQL, see the OpenJPA Reference Guide.</li>
 * </ul></li>
 * </ul>
 *
 * @since 0.3.1
 */
public abstract class AbstractStoreManager
    implements StoreManager {

    protected StoreContext ctx;

    public final void setContext(StoreContext ctx) {
        this.ctx = ctx;
        open();
    }

    /**
     * Returns the {@link StoreContext} that this store manager is
     * associated with.
     */
    public StoreContext getContext() {
        return ctx;
    }

    /**
     * No-op implementation. Ready this store manager for persistent operations.
     */
    protected void open() {
    }

    /**
     * No-op implementation. Override this method to provide optimistic
     * locking semantics for your data store if you need notification of
     * the beginning of an optimistic transaction.
     */
    public void beginOptimistic() {
    }

    /**
     * No-op implementation. Override this method to provide optimistic
     * locking semantics for your data store if you need notification of
     * a rollback of an optimistic transaction before {@link #begin} is invoked.
     */
    public void rollbackOptimistic() {
    }

    /**
     * OpenJPA assumes that after this method is invoked, all data
     * accesses through this store manager will be part of a single
     * unit of work that can be rolled back.
     *  This is a no-op implementation. If your data store does not
     * support any concept of locking or transactions, you need not
     * override this method.
     */
    public void begin() {
    }

    /**
     * This is a no-op implementation. If your data store does not
     * have a concept of transactions or a unit of work, you need not
     * override this method. If it does, then override this method to
     * notify the data store that the current transaction should be committed.
     */
    public void commit() {
    }

    /**
     * This is a no-op implementation. If your data store does not
     * have a concept of transactions or a unit of work, you need not
     * override this method. If it does, then override this method to
     * notify the data store that the current transaction should be rolled back.
     */
    public void rollback() {
    }

    /**
     * Since this store manager does not provide optimistic locking
     * support, this method always returns <code>true</code>.
     */
    public boolean syncVersion(OpenJPAStateManager sm, Object edata) {
        return true;
    }

    /**
     * This method is invoked when OpenJPA needs to load an object whose
     * identity is known but which has not yet been loaded from the data
     * store. <code>sm</code> is a partially-set-up state manager for this
     * object. The ID and least-derived type information for the instance
     * to load can be obtained by invoking
     * <code>sm.getObjectId()</code> and <code>sm.getMetaData()</code>.
     * 
     *  When implementing this method, load the data for this object from
     * the data store, determine the most-derived subclass of the newly-loaded
     * data, and then use the {@link OpenJPAStateManager#initialize} method to
     * populate <code>sm</code> with a new instance of the appropriate type.
     * Once {@link OpenJPAStateManager#initialize} has been invoked, proceed to
     * load field data into <code>sm</code> as in the {@link #load} method, by
     * using {@link OpenJPAStateManager#store} (or the appropriate
     * <code>OpenJPAStateManager.store<em>type</em></code> method) to put the
     * data into the object.
     */
    public abstract boolean initialize(OpenJPAStateManager sm, PCState state,
        FetchConfiguration fetch, Object edata);

    /**
     * This method is invoked when OpenJPA needs to load additional data
     * into an object that has already been at least partially loaded by
     * a previous {@link #initialize} invocation.
     *  Load data into <code>sm</code> by using {@link
     * OpenJPAStateManager#store} (or the appropriate
     * <code>OpenJPAStateManager.store<em>type</em></code> method) to put the
     * data into the object.
     */
    public abstract boolean load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, int lockLevel, Object edata);

    /**
     * This implementation just delegates to the proper singular
     * method ({@link StoreManager#initialize} or {@link StoreManager#load})
     * depending on each state manager's state. If your data store provides
     * bulk loading APIs, overriding this method to be more clever may be
     * advantageous.
     */
    public Collection<Object> loadAll(Collection<OpenJPAStateManager> sms, PCState state, int load,
        FetchConfiguration fetch, Object edata) {
        return ImplHelper.loadAll(sms, this, state, load, fetch, edata);
    }

    /**
     * Breaks down <code>states</code> based on the objects' current
     * states, and delegates to
     * {@link #flush(Collection,Collection,Collection,Collection,Collection)}.
     */
    public Collection<Exception> flush(Collection<OpenJPAStateManager> sms) {
        // break down state managers by state; initialize as empty lists;
        // use constants for efficiency
        Collection<OpenJPAStateManager> pNew = new LinkedList<OpenJPAStateManager>();
        Collection<OpenJPAStateManager> pNewUpdated = new LinkedList<OpenJPAStateManager>();
        Collection<OpenJPAStateManager> pNewFlushedDeleted = new LinkedList<OpenJPAStateManager>();
        Collection<OpenJPAStateManager> pDirty = new LinkedList<OpenJPAStateManager>();
        Collection<OpenJPAStateManager> pDeleted = new LinkedList<OpenJPAStateManager>();

        for (OpenJPAStateManager sm : sms) {
            if (sm.getPCState() == PCState.PNEW && !sm.isFlushed())
                pNew.add(sm);
            else if (sm.getPCState() == PCState.PNEW && sm.isFlushed())
                pNewUpdated.add(sm);
            else if (sm.getPCState() == PCState.PNEWFLUSHEDDELETED)
                pNewFlushedDeleted.add(sm);
            else if (sm.getPCState() == PCState.PDIRTY)
                pDirty.add(sm);
            else if (sm.getPCState() == PCState.PDELETED)
                pDeleted.add(sm);
        }

        // no dirty instances to flush?
        if (pNew.isEmpty() && pNewUpdated.isEmpty()
            && pNewFlushedDeleted.isEmpty() && pDirty.isEmpty()
            && pDeleted.isEmpty())
            return Collections.EMPTY_LIST;

        return flush(pNew, pNewUpdated, pNewFlushedDeleted, pDirty, pDeleted);
    }

    public void beforeStateChange(OpenJPAStateManager sm, PCState fromState,
        PCState toState) {
    }

    public boolean assignObjectId(OpenJPAStateManager sm, boolean preFlush) {
        ClassMetaData meta = sm.getMetaData();
        if (meta.getIdentityType() == ClassMetaData.ID_APPLICATION)
            return ApplicationIds.assign(sm, this, preFlush);

        // datastore identity
        Object val = ImplHelper.generateIdentityValue(ctx, meta,
            JavaTypes.LONG);
        return assignDataStoreId(sm, val);
    }

    /**
     * Assign a new datastore identity to the given instance. This given
     * value may be null.
     */
    protected boolean assignDataStoreId(OpenJPAStateManager sm, Object val) {
        ClassMetaData meta = sm.getMetaData();
        if (val == null && meta.getIdentityStrategy() != ValueStrategies.NATIVE)
            return false;
        if (val == null)
            val = getDataStoreIdSequence(meta).next(ctx, meta);
        sm.setObjectId(newDataStoreId(val, meta));
        return true;
    }

    public boolean assignField(OpenJPAStateManager sm, int field,
        boolean preFlush) {
        FieldMetaData fmd = sm.getMetaData().getField(field);
        Object val = ImplHelper.generateFieldValue(ctx, fmd);
        if (val == null)
            return false;
        sm.store(field, val);
        return true;
    }

    public Class<?> getManagedType(Object oid) {
        if (oid instanceof Id)
            return ((Id) oid).getType();
        return null;
    }

    public Class<?> getDataStoreIdType(ClassMetaData meta) {
        return Id.class;
    }

    public Object copyDataStoreId(Object oid, ClassMetaData meta) {
        Id id = (Id) oid;
        return new Id(meta.getDescribedType(), id.getId(),
            id.hasSubclasses());
    }

    public Object newDataStoreId(Object val, ClassMetaData meta) {
        // we use base types for all oids
        while (meta.getPCSuperclass() != null)
            meta = meta.getPCSuperclassMetaData();
        return Id.newInstance(meta.getDescribedType(), val);
    }

    /**
     * Override to retain a dedicated connection.
     */
    public void retainConnection() {
    }

    /**
     * Override to release previously-retained connection.
     */
    public void releaseConnection() {
    }

    /**
     * Returns <code>null</code>. If your data store can provide a
     * distinct connection object, return it here.
     */
    public Object getClientConnection() {
        return null;
    }

    /**
     * Create a {@link ResultObjectProvider} that can return all instances
     * of <code>type</code>, optionally including subclasses as defined
     * by <code>subclasses</code>.
     *  The implementation of the result provider will typically execute
     * some sort of data store query to find all the applicable objects, loop
     * through the results, extracting object IDs from the data, and invoke
     * {@link StoreContext#find(Object,FetchConfiguration,BitSet,Object,int)}
     * on each OID. When invoking this method, the first argument is the OID.
     * The second is the given fetch configuration. The
     * third argument is a mask of fields to exclude from loading; it will
     * typically be null. The fourth argument is an object that will be passed
     * through to {@link #initialize} or {@link #load}, and typically will
     * contain the actual data to load. For example, for a JDBC-based store
     * manager, this might be the result set that is being iterated over. If
     * this argument is <code>null</code>, then the {@link #initialize} or
     * {@link #load} method will have to issue another command to the data
     * store in order to fetch the data to be loaded. 
     */
    public abstract ResultObjectProvider executeExtent(ClassMetaData meta,
        boolean subs, FetchConfiguration fetch);

    public StoreQuery newQuery(String language) {
        return null;
    }

    public FetchConfiguration newFetchConfiguration() {
        return new FetchConfigurationImpl();
    }

    /**
     * Casts <code>v1</code> and <code>v2</code> to {@link Comparable}, and
     * invokes <code>v1.compareTo (v2)</code>. If <code>v1</code> is less
     * than <code>v2</code>, returns {@link #VERSION_EARLIER}. If the same,
     * returns {@link #VERSION_SAME}. Otherwise, returns {@link
     * #VERSION_LATER}. If either <code>v1</code> or <code>v2</code> are
     * <code>null</code>, returns {@link #VERSION_DIFFERENT}.
     */
    public int compareVersion(OpenJPAStateManager state, Object v1, Object v2) {
        if (v1 == null || v2 == null)
            return VERSION_DIFFERENT;

        int compare = ((Comparable) v1).compareTo((Comparable) v2);
        if (compare < 0)
            return VERSION_EARLIER;
        if (compare == 0)
            return VERSION_SAME;
        return VERSION_LATER;
    }

    /**
     * Returns the system-configured sequence. To use some other sort
     * of datastore identifier (a GUID, string, or someting of that nature),
     * override {@link #getManagedType},
     * {@link #getDataStoreIdType}, {@link #copyDataStoreId},
     * {@link #newDataStoreId}.
     */
    public Seq getDataStoreIdSequence(ClassMetaData forClass) {
        return ctx.getConfiguration().getSequenceInstance();
    }

    /**
     * Returns null.
     */
    public Seq getValueSequence(FieldMetaData forField) {
        return null;
    }

    /**
     * Returns <code>false</code>. If your data store supports
     * cancelling queries, this method should cancel any
     * currently-running queries and return <code>true</code> if any
     * were cancelled.
     */
    public boolean cancelAll() {
        return false;
    }

    public void close() {
    }

    /**
     * Responsible for writing modifications happened back to the data
     * store. If you do not remove the
     * {@link OpenJPAConfiguration#OPTION_INC_FLUSH} option in
     * {@link #getUnsupportedOptions}, this will be called only once at the
     * end	of a transaction. Otherwise, it may be called periodically
     * throughout the course of a transaction.
     *  If this store manager supports optimistic transactions, datastore
     * version information should be updated during flush, and the state
     * manager's version indicator should be updated through the
     * {@link OpenJPAStateManager#setNextVersion} method.
     *  This method will only be invoked if there are meaningful changes
     * to store. This differs from the behavior of {@link StoreManager#flush},
     * which may be invoked with a collection of objects in states that
     * do not require any datastore action (for example, objects in the
     * transient-transactional state).
     *
     * @param pNew Objects that should be added to the store,
     * and that have not previously been flushed.
     * @param pNewUpdated New objects that have been modified since
     * they were initially flushed. These were
     * in <code>persistentNew</code> in an earlier flush invocation.
     * @param pNewFlushedDeleted New objects that have been deleted since
     * they were initially flushed. These were
     * in <code>persistentNew</code> in an earlier flush invocation.
     * @param pDirty Objects that were loaded from the data
     * store and have since been modified.
     * @param pDeleted Objects that were loaded from the data
     * store and have since been deleted. These
     * may have been in a previous flush invocation's persistentDirty list.
     * @return a collection of exceptions encountered during flushing.
     */
    protected abstract Collection<Exception> flush(Collection<OpenJPAStateManager> pNew,
        Collection<OpenJPAStateManager> pNewUpdated, Collection<OpenJPAStateManager> pNewFlushedDeleted,
        Collection<OpenJPAStateManager> pDirty, Collection<OpenJPAStateManager> pDeleted);

    /**
     * Return a new configuration instance for this runtime. Configuration
     * data is maintained at the factory level and is available to all OpenJPA
     * components; therefore it is a good place to maintain shared resources
     * such as connection pools, etc.
     */
    protected OpenJPAConfiguration newConfiguration() {
        return new OpenJPAConfigurationImpl();
    }

    /**
     * Returns a set of option names that this store manager does
     * not support. By default, returns the following:
     * <ul>
     * <li>{@link OpenJPAConfiguration#OPTION_OPTIMISTIC}</li>
     * <li>{@link OpenJPAConfiguration#OPTION_ID_DATASTORE}</li>
     * <li>{@link OpenJPAConfiguration#OPTION_INC_FLUSH}</li>
     * <li>{@link OpenJPAConfiguration#OPTION_VALUE_AUTOASSIGN}</li>
     * <li>{@link OpenJPAConfiguration#OPTION_VALUE_INCREMENT}</li>
     * <li>{@link OpenJPAConfiguration#OPTION_DATASTORE_CONNECTION}</li>
     * </ul>
     */
    protected Collection<String> getUnsupportedOptions() {
        Collection<String> c = new HashSet<String>();
        c.add(OpenJPAConfiguration.OPTION_OPTIMISTIC);
        c.add(OpenJPAConfiguration.OPTION_ID_DATASTORE);
        c.add(OpenJPAConfiguration.OPTION_INC_FLUSH);
        c.add(OpenJPAConfiguration.OPTION_VALUE_AUTOASSIGN);
        c.add(OpenJPAConfiguration.OPTION_VALUE_INCREMENT);
        c.add(OpenJPAConfiguration.OPTION_DATASTORE_CONNECTION);
        return c;
    }

    /**
     * Returns a string name to identify the platform of this
     * store manager. Returns the class name of this store manager by default.
     */
    protected String getPlatform ()
	{
		return getClass ().getName ();
	}
}

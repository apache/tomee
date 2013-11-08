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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * Allows configuration and optimization of how objects are loaded from
 * the data store.
 *
 * @since 0.3.0
 * @author Abe White
 * @author Pinaki Poddar
 */
public interface FetchConfiguration
    extends Serializable, Cloneable, LockLevels, LockScopes, QueryFlushModes {

    /**
     * Constant to revert any setting back to its default value.
     */
    public static final int DEFAULT = -99;

    /**
     * Constant indicating that a field does not require fetching.
     *
     * @see #requiresFetch
     */
    public static final int FETCH_NONE = 0;

    /**
     * Constant indicating that a field requires a fetch and load of fetched
     * data.
     *
     * @see #requiresFetch
     */
    public static final int FETCH_LOAD = 1;

    /**
     * Constant indicating that a reference to the field's value must be
     * fetched, but loading data is not necessary.  Used when we know the
     * data will be loaded anyway, such as when traversing the back-ptr of
     * a bidirectional relation where the other side is also being fetched.
     *
     * @see #requiresFetch
     */
    public static final int FETCH_REF = 2;


    /**
     * Return the context associated with this configuration;
     * may be null if it has not been set or this object has been serialized.
     */
    public StoreContext getContext();

    /**
     * Called automatically by the system to associate the fetch configuration
     * with a context before use. The fetch configuration properties should
     * be synchronized with the context's configuration object. Subclasses
     * for specific back ends cannot rely on the context's configuration
     * implementing their back end's configuration sub-interface.
     */
    public void setContext(StoreContext ctx);

    /**
     * Clone this instance.
     */
    public Object clone();

    /**
     * Copy the state from the given fetch configuration to this one.
     */
    public void copy(FetchConfiguration fetch);

    /**
     * Return the fetch batch size for large result set support.
     * Defaults to the	<code>openjpa.FetchBatchSize</code> setting. Note
     * that this property will be ignored under some data stores.
     */
    public int getFetchBatchSize();

    /**
     * Set the fetch batch size for large result set support.
     * Defaults to the	<code>openjpa.FetchBatchSize</code> setting. Note
     * that this property will be ignored under some data stores.
     */
    public FetchConfiguration setFetchBatchSize(int fetchBatchSize);

    /**
     * Return the maximum depth of fetched instance graph.
     * Defaults to <code>1</code>
     */
    public int getMaxFetchDepth();

    /**
     * Set the maximum depth of the fetched instance graph.
     *
     * @param max denotes limiting length of traversal path from a root
     * instance. <code>-1</code> implies no limit. <code>0</code> is not
     * permissible.
     */
    public FetchConfiguration setMaxFetchDepth(int max);

    /**
     * Return whether or not query caching is enabled. If this returns
     * <code>true</code> but the datacache plugin is not installed, caching
     * will not be enabled. If this
     * returns <code>false</code>, query caching will not be used
     * even if the datacache plugin is installed.
     */
    public boolean getQueryCacheEnabled();

    /**
     * Control whether or not query caching is enabled. This has no effect
     * if the datacache plugin is not installed, or if the query cache size
     * is set to zero.
     */
    public FetchConfiguration setQueryCacheEnabled(boolean cache);

    /**
     * The query automatic flush configuration.
     */
    public int getFlushBeforeQueries();

    /**
     * The query automatic flush configuration.
     */
    public FetchConfiguration setFlushBeforeQueries(int flush);
    
    /**
     * Affirms if extended path lookup feature is active.
     * 
     * @since 2.0.0
     */
    public boolean getExtendedPathLookup();
    
    /**
     * Sets extended path lookup feature.
     *
     * @since 2.0.0
     */
    public FetchConfiguration setExtendedPathLookup(boolean flag);

    /**
     * Returns immutable set of names of the fetch groups that this component
     * will use when loading objects. Defaults to the
     * <code>openjpa.FetchGroups</code> setting.  This set is not thread safe.
     */
    public Set<String> getFetchGroups();

    /**
     * Return true if the given fetch group has been added.
     */
    public boolean hasFetchGroup(String group);

    /**
     * Adds <code>group</code> to the set of fetch group names to
     * use when loading objects.
     */
    public FetchConfiguration addFetchGroup(String group);

    /**
     * Adds <code>groups</code> to the set of fetch group names to
     * use when loading objects.
     */
    public FetchConfiguration addFetchGroups(Collection<String> groups);

    /**
     * Remove the given fetch group.
     */
    public FetchConfiguration removeFetchGroup(String group);

    /**
     * Removes <code>groups</code> from the set of fetch group names
     * to use when loading objects.
     */
    public FetchConfiguration removeFetchGroups(Collection<String> groups);

    /**
     * Clears the set of fetch group names to use when loading
     * data. After this operation is invoked, only those fields in
     * the default fetch group (and any requested field) will be
     * loaded when loading an object.
     */
    public FetchConfiguration clearFetchGroups();

    /**
     * Resets the set of fetch groups to the list in the global configuration.
     */
    public FetchConfiguration resetFetchGroups();

    /**
     * Returns the set of fully-qualified field names that this component
     * will use when loading objects. Defaults to the empty set.  This set is
     * not thread safe.
     */
    public Set<String> getFields();

    /**
     * Return true if the given fully-qualified field has been added.
     */
    public boolean hasField(String field);

    /**
     * Adds <code>field</code> to the set of fully-qualified field names to
     * use when loading objects.
     */
    public FetchConfiguration addField(String field);

    /**
     * Adds <code>fields</code> to the set of fully-qualified field names to
     * use when loading objects.
     */
    public FetchConfiguration addFields(Collection<String> fields);

    /**
     * Remove the given fully-qualified field.
     */
    public FetchConfiguration removeField(String field);

    /**
     * Removes <code>fields</code> from the set of fully-qualified field names
     * to use when loading objects.
     */
    public FetchConfiguration removeFields(Collection<String> fields);

    /**
     * Clears the set of field names to use when loading
     * data. After this operation is invoked, only those fields in
     * the configured fetch groups will be loaded when loading an object.
     */
    public FetchConfiguration clearFields();

    /**
     * The number of milliseconds to wait for an object lock, or -1 for no
     * limit.
     *
     * @since 0.3.1
     */
    public int getLockTimeout();

    /**
     * The number of milliseconds to wait for an object lock, or -1 for no
     * limit.
     *
     * @since 0.3.1
     */
    public FetchConfiguration setLockTimeout(int timeout);

    /**
     * The lock scope for next fetch.
     *
     * @since 2.0.0
     */
    public int getLockScope();

    /**
     * The lock scope for next fetch.
     *
     * @since 2.0.0
     */
    public FetchConfiguration setLockScope(int scope);

    /**
     * The number of milliseconds to wait for a query, or -1 for no
     * limit.
     *
     * @since 2.0.0
     */
    public int getQueryTimeout();

    /**
     * The number of milliseconds to wait for a query, or -1 for no
     * limit.
     *
     * @since 2.0.0
     */
    public FetchConfiguration setQueryTimeout(int timeout);

    /**
     * The lock level to use for locking loaded objects.
     *
     * @since 0.3.1
     */
    public int getReadLockLevel();

    /**
     * The lock level to use for locking loaded objects.
     *
     * @since 0.3.1
     */
    public FetchConfiguration setReadLockLevel(int level);

    /**
     * The lock level to use for locking dirtied objects.
     *
     * @since 0.3.1
     */
    public int getWriteLockLevel();
    
    /**
     * Gets the current storage mode for data cache.
     * 
     * @since 2.0.0
     */
    public DataCacheStoreMode getCacheStoreMode();
    
    /**
     * Sets the current storage mode for data cache.
     * 
     * @since 2.0.0
     */
    public void setCacheStoreMode(DataCacheStoreMode mode);
    
    /**
     * Gets the current retrieve mode for data cache.
     * 
     * @since 2.0.0
     */
    public DataCacheRetrieveMode getCacheRetrieveMode();
    
    /**
     * Sets the current retrieve mode for data cache.
     * 
     * @since 2.0.0
     */
    public void setCacheRetrieveMode(DataCacheRetrieveMode mode);

    /**
     * The lock level to use for locking dirtied objects.
     *
     * @since 0.3.1
     */
    public FetchConfiguration setWriteLockLevel(int level);

    /**
     * Return a new result list for the current fetch configuration.
     */
    public ResultList<?> newResultList(ResultObjectProvider rop);

    /**
     * Sets an arbitrary query hint that may be utilized during execution. 
     * The hint may be specific to a particular database. A hint, if known 
     * to this receiver, may have a corresponding setter method, then the hint sets the value.
     * Otherwise the hint is stored opaquely by the receiver.
     *
     * @param name the name of the hint
     * @param value the value of the hint. If the hint has a corresponding setter, then
     * the type of value must be same as the setter argument. 
     * @param original the value of the hint as specified by the user. 
     * 
     * @since 2.0.0
     */
    public void setHint(String name, Object value, Object original);
    
    /**
     * Sets an arbitrary query hint that may be utilized during execution. 
     * The hint may be specific to a particular database. A hint, if known 
     * to this receiver, may have a corresponding setter method, then the hint sets the value.
     * Otherwise the hint is stored opaquely by the receiver.
     * <br>
     * This is same as calling {@linkplain #setHint(String, Object, Object)} with the third
     * argument being the same as the second.
     *
     * @param name the name of the hint
     * @param value the value of the hint. If the hint has a corresponding setter, then
     * the type of value must be same as the setter argument. 
     * 
     * @since 2.0.0
     */
    public void setHint(String key, Object value);

    /**
     * Get the hint value for the specific key as originally set by the caller, or null if the hint is not specified.
     *
	 * @param name the hint name
	 * @since 0.4.0
	 */
	public Object getHint (String key);
	
	/**
     * Get an immutable view of the currently active hints and their values.
	 * The values are as specified by the user.
	 * 
	 * @since 2.0.0
	 */
	public Map<String, Object> getHints();
	
	/**
	 * Affirm if the given hint has been set in this receiver.
	 * 
	 */
	public boolean isHintSet(String key);

	/**
	 * Affirm if the Fetch Plan currently matches the Persistence Unit's configured default.
	 * 
	 */
	public boolean isDefaultPUFetchGroupConfigurationOnly();
	
    /**
     * Root classes for recursive operations. This set is not thread safe.
     */
    public Set<Class<?>> getRootClasses();

    /**
     * Root classes for recursive operations.
     */
    public FetchConfiguration setRootClasses(Collection<Class<?>> classes);

    /**
     * Root instances for recursive operations. This set is not thread safe.
     */
    public Set<Object> getRootInstances();

    /**
     * Root instances for recursive operations.
     */
    public FetchConfiguration setRootInstances(Collection<?> roots);

    /**
     * Synchronize on internal lock if multithreaded is true.
     */
    public void lock();

    /**
     * Release internal lock if multithreaded is true.
     */
    public void unlock();

    /**
     * Affirms if the given field requires to be fetched in the context
     * of current fetch operation.  Returns one of {@link #FETCH_NONE},
     * {@link #FETCH_LOAD}, {@link FETCH_REF}.
     *
     * @since 0.4.1
     */
    public int requiresFetch(FieldMetaData fm);

    /**
     * Return false if we know that the object being fetched with this
     * configuration does not require a load, because this configuration came
     * from a traversal of a {@link #FETCH_REF} field.
     */
    public boolean requiresLoad();
    
    /**
     * Traverse the given field to generate (possibly) a new configuration 
     * state.
     * 
     * @return a new configuration state resulting out of traversal
     * @since 0.4.1
     */
    public FetchConfiguration traverse(FieldMetaData fm);
    
    /**
     * Whether SQL generated by the FetchConfiguration's current configuration should be cached.
     */
    public boolean isFetchConfigurationSQLCacheAdmissible();
}

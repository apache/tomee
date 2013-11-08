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

import java.util.Collection;
import java.util.Map;

import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockScope;

import org.apache.openjpa.kernel.DataCacheRetrieveMode;
import org.apache.openjpa.kernel.DataCacheStoreMode;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.lib.util.Reflectable;
import org.apache.openjpa.meta.FetchGroup;

/**
 * The fetch plan allows you to dynamically alter eager fetching
 * configuration and other aspects of data loading.
 *
 * @author Abe White
 * @author Pinaki Poddar
 * @since 0.4.1
 * @published
 */
public interface FetchPlan {

    /**
     * Fetch group representing all fields.
     */
    public static final String GROUP_ALL = FetchGroup.NAME_ALL;

    /**
     * The default fetch group.
     */
    public static final String GROUP_DEFAULT = FetchGroup.NAME_DEFAULT;

    /**
     * Infinite fetch depth.
     */
    public static final int DEPTH_INFINITE = FetchGroup.DEPTH_INFINITE;

    /**
     * Constant to revert any setting to its default value.
     */
    public static final int DEFAULT = FetchConfiguration.DEFAULT;

    /**
     * The maximum fetch depth when loading an object.
     */
    public int getMaxFetchDepth();

    /**
     * The maximum fetch depth when loading an object.
     */
    public FetchPlan setMaxFetchDepth(int depth);

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
    public FetchPlan setFetchBatchSize(int fetchBatchSize);

    /**
     * Return whether or not query caching is enabled. If this returns
     * <code>true</code> but the datacache plugin is not installed, caching
     * will not be enabled. If this
     * returns <code>false</code>, query caching will not be used
     * even if the datacache plugin is installed.
     *
     * @since 1.0.0
     */
    public boolean getQueryResultCacheEnabled();

    /**
     * Control whether or not query caching is enabled. This has no effect
     * if the datacache plugin is not installed, or if the query cache size
     * is set to zero.
     *
     * @since 1.0.0
     */
    public FetchPlan setQueryResultCacheEnabled(boolean cache);

    /**
     * @deprecated use {@link #getQueryResultCacheEnabled()} instead.
     */
    public boolean getQueryResultCache();

    /**
     * @deprecated use {@link #setQueryResultCacheEnabled} instead.
     */
    public FetchPlan setQueryResultCache(boolean cache);
    
    
    /**
     * Returns the names of the fetch groups that this component will use
     * when loading objects. Defaults to the
     * <code>openjpa.FetchGroups</code> setting.
     */
    public Collection<String> getFetchGroups();

    /**
     * Adds <code>group</code> to the set of fetch group to
     * use when loading objects.
     */
    public FetchPlan addFetchGroup(String group);

    /**
     * Adds <code>groups</code> to the set of fetch group names to
     * use when loading objects.
     */
    public FetchPlan addFetchGroups(String... groups);

    /**
     * Adds <code>groups</code> to the set of fetch group names to
     * use when loading objects.
     */
    public FetchPlan addFetchGroups(Collection groups);

    /**
     * Remove the given fetch group.
     */
    public FetchPlan removeFetchGroup(String group);

    /**
     * Removes <code>groups</code> from the set of fetch group names
     * to use when loading objects.
     */
    public FetchPlan removeFetchGroups(String... groups);

    /**
     * Removes <code>groups</code> from the set of fetch group names
     * to use when loading objects.
     */
    public FetchPlan removeFetchGroups(Collection groups);

    /**
     * Clears the set of fetch group names to use wen loading
     * data. After this operation is invoked, only those fields in
     * the default fetch group (and any requested field) will be
     * loaded when loading an object.
     */
    public FetchPlan clearFetchGroups();

    /**
     * Resets the set of fetch groups to the list in the global configuration.
     */
    public FetchPlan resetFetchGroups();

    /**
     * Returns the fully qualified names of the fields that this component
     * will use when loading objects. Defaults to the empty set.
     */
    public Collection<String> getFields();

    /**
     * Return true if the given field has been added.
     */
    public boolean hasField(String field);

    /**
     * Return true if the given field has been added.
     */
    public boolean hasField(Class cls, String field);

    /**
     * Adds <code>field</code> to the set of fully-qualified field names to
     * use when loading objects.
     */
    public FetchPlan addField(String field);

    /**
     * Adds <code>field</code> to the set of field names to
     * use when loading objects.
     */
    public FetchPlan addField(Class cls, String field);

    /**
     * Adds <code>fields</code> to the set of fully-qualified field names to
     * use when loading objects.
     */
    public FetchPlan addFields(String... fields);

    /**
     * Adds <code>fields</code> to the set of field names to
     * use when loading objects.
     */
    public FetchPlan addFields(Class cls, String... fields);

    /**
     * Adds <code>fields</code> to the set of fully-qualified field names to
     * use when loading objects.
     */
    public FetchPlan addFields(Collection fields);

    /**
     * Adds <code>fields</code> to the set of field names to
     * use when loading objects.
     */
    public FetchPlan addFields(Class cls, Collection fields);

    /**
     * Remove the given fully-qualified field.
     */
    public FetchPlan removeField(String field);

    /**
     * Remove the given field.
     */
    public FetchPlan removeField(Class cls, String field);

    /**
     * Removes <code>fields</code> from the set of fully-qualified field names
     * to use when loading objects.
     */
    public FetchPlan removeFields(String... fields);

    /**
     * Removes <code>fields</code> from the set of field names
     * to use when loading objects.
     */
    public FetchPlan removeFields(Class cls, String... fields);

    /**
     * Removes <code>fields</code> from the set of fully-qualified field names
     * to use when loading objects.
     */
    public FetchPlan removeFields(Collection fields);

    /**
     * Removes <code>fields</code> from the set of field names
     * to use when loading objects.
     */
    public FetchPlan removeFields(Class cls, Collection fields);

    /**
     * Clears the set of field names to use wen loading
     * data. After this operation is invoked, only those fields in
     * the configured fetch groups will be loaded when loading an object.
     */
    public FetchPlan clearFields();

    /**
     * The number of milliseconds to wait for an object lock, or -1 for no
     * limit.
     */
    public int getLockTimeout();

    /**
     * The number of milliseconds to wait for an object lock, or -1 for no
     * limit.
     */
    public FetchPlan setLockTimeout(int timeout);

    /**
     * The lock scope to use for locking loaded objects.
     */
    public PessimisticLockScope getLockScope();

    /**
     * The lock scope to use for locking loaded objects.
     */
    public FetchPlan setLockScope(PessimisticLockScope scope);

    /**
     * The number of milliseconds to wait for a query, or -1 for no
     * limit.
     */
    public int getQueryTimeout();

    /**
     * The number of milliseconds to wait for a query, or -1 for no
     * limit.
     */
    public FetchPlan setQueryTimeout(int timeout);

    /**
     * The lock level to use for locking loaded objects.
     */
    public LockModeType getReadLockMode();

    /**
     * The lock level to use for locking loaded objects.
     */
    public FetchPlan setReadLockMode(LockModeType mode);

    /**
     * The lock level to use for locking dirtied objects.
     */
    public LockModeType getWriteLockMode();

    /**
     * The lock level to use for locking dirtied objects.
     */
    public FetchPlan setWriteLockMode(LockModeType mode);

    /**
     * @deprecated cast to {@link FetchPlanImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    @Reflectable(false)
    public org.apache.openjpa.kernel.FetchConfiguration getDelegate();
    
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
    public FetchPlan setExtendedPathLookup(boolean flag);
    
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
    public FetchPlan setCacheStoreMode(DataCacheStoreMode mode);
    
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
    public FetchPlan setCacheRetrieveMode(DataCacheRetrieveMode mode);
    
    /**
     * Set the hint for the given key to the given value.
     * 
     * @param value the value of the hint.
     * @param name the name of the hint.
     * 
     * @since 2.0.0
     */
    public void setHint(String key, Object value);

    /**
     * Get the hints and their values currently set on this receiver.
     * 
     * @return empty map if no hint has been set.
     */
    Map<String, Object> getHints();
    
    /**
     * Get the hint value for the given key.
     * 
     * @return null if the key has not been set.
     */
    Object getHint(String key);
}

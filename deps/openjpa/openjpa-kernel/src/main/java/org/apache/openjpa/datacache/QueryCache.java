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
package org.apache.openjpa.datacache;

import java.util.Map;

import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.lib.util.Closeable;

/**
 * Interface that must be implemented by any level 2 query cache
 * used by OpenJPA. These methods should be threadsafe.
 * Most query cache implementations will probably implement
 * {@link org.apache.openjpa.lib.conf.Configurable} to receive a handle to the
 * system configuration on construction.
 *
 * @since 0.2.5
 * @author Patrick Linskey
 */
public interface QueryCache
    extends TypesChangedListener, Closeable {

    /**
     * Initialize any resources associated with the given
     * {@link DataCacheManager}.
     *
     * @since 0.4.1
     */
    public void initialize(DataCacheManager manager);

    /**
     * Return a list of oids for the given query key. This is an
     * unmodifiable list.
     *
     * @return The query results matching the given key, or null if none
     */
    public QueryResult get(QueryKey qk);

    /**
     * Set the list of OIDs for the given query key. A reference
     * to the given list will be stored in the query cache, so the
     * list should not be modified after invoking this method.
     *
     * @return The previously cached value, or <code>null</code> if
     * the key was not previously cached. See {@link Map#put}
     * for more information.
     */
    public QueryResult put(QueryKey qk, QueryResult oids);

    /**
     * Remove the value stored under the given query key.
     *  This method is typically not invoked directly from outside
     * the <code>QueryCache</code> class. Instead, the cache should
     * be updated by implementing {@link
     * org.apache.openjpa.event.RemoteCommitListener},
     * which will result in all queries that may be invalid being dropped.
     *
     * @return The previously cached value, or <code>null</code> if
     * the key was not previously cached. See {@link Map#remove}
     * for more information.
     */
    public QueryResult remove(QueryKey qk);

    /**
     * Remove all data from this cache.
     */
    public void clear();

    /**
     * Pin the value stored under <code>qk</code> into the
     * cache. This method guarantees that <code>qk</code>'s value
     * will not be expired if the cache exceeds its capacity. It
     * causes this data to be ignored when determining whether or not
     * the cache is full, effectively increasing the total amount of
     * data stored in the cache. This method does not affect the
     * behavior of {@link #remove} or {@link #onTypesChanged}.
     *
     * @return <code>true</code> if <code>key</code>'s value was
     * pinned into the cache; <code>false</code> if the key is not in the cache.
     */
    public boolean pin(QueryKey qk);

    /**
     * Unpin the value stored under <code>key</code> into the cache.
     * This method reverses a previous invocation of {@link #pin}.
     * This method does not remove anything from the cache; it merely
     * makes <code>key</code>'s value a candidate for flushing from the cache.
     *
     * @return <code>true</code> if <code>key</code>'s value was
     * unpinned from the cache; <code>false</code> if the
     * key is not in the cache.
     */
    public boolean unpin(QueryKey qk);

    /**
     * Obtain a write lock on the cache.
     */
    public void writeLock();

    /**
     * Release the write lock on the cache.
     */
    public void writeUnlock();

    /**
     * Add a new types event listener to this cache.
     *
     * @since 0.3.3
     */
    public void addTypesChangedListener(TypesChangedListener listen);

    /**
     * Remove an types event listener from this cache.
     *
     * @since 0.3.3
     */
    public boolean removeTypesChangedListener(TypesChangedListener listen);

    /**
     * Free the resources used by this cache.
	 */
	public void close ();
	
	   /**
     * Gets the simple statistics for query results.
     * If the statistics gathering is disabled, an empty statistics is returned.
     * @since 2.1.0 
     */
    public QueryStatistics<QueryKey> getStatistics();
}

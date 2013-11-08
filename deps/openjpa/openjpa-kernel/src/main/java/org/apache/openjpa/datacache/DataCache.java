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

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.lib.util.Clearable;
import org.apache.openjpa.lib.util.Closeable;

/**
 * Interface that must be implemented by any level 2 cache used by
 * OpenJPA. Most data caches will choose to implement the
 * {@link org.apache.openjpa.lib.conf.Configurable} interface as well so that
 * they will be given the system configuration just after construction.
 *  Implementations should take care not to return timed out data.
 *
 * @see AbstractDataCache
 * @see DataCachePCData#isTimedOut
 * @author Patrick Linskey
 * @author Abe White
 * @author Pinaki Poddar
 */
public interface DataCache
    extends Closeable, Clearable {

    /**
     * The name of the default data cache: <code>default</code>
     */
    public static final String NAME_DEFAULT = "default";

    /**
     * Returns a string name that can be used by end-user-visible
     * code to identify this cache.
     *
     * @since 0.2.5.0
     */
    public String getName();

    /**
     * Sets a string name to be used to identify this cache to end-user needs.
     *
     * @since 0.2.5.0
     */
    public void setName(String name);

    /**
     * Initialize any resources associated with the given
     * {@link DataCacheManager}.
     */
    public void initialize(DataCacheManager manager);

    /**
     * Perform a batch update of the cache. Add all {@link DataCachePCData}
     * objects in <code>additions</code> and in
     * <code>newUpdates</code>, make the appropriate modifications to
     * all DataCachePCDatas in <code>existingUpdates</code>, and delete all
     * OIDs in <code>deletes</code>.
     *  All changes made to cached data must be made via this
     * method. It is this method that is responsible for performing
     * any side-effects that should happen on meaningful cache changes.
     *  Implementations should bear in mind that the
     * <code>deletes</code> collection may contain oids that are also
     * in the <code>additions</code> map. This is possible because it
     * is valid for a user to delete an object with a particular oid
     * and then add that object in the same batch.
     *
     * @param additions A collection of {@link DataCachePCData} objects.
     * These represent data that have been newly created,
     * and thus must be added to the cache.
     * @param newUpdates A collection of {@link DataCachePCData} objects.
     * These represent data that have been modified but
     * were not originally in the cache, and thus must be added to the cache.
     * @param existingUpdates A collection of {@link DataCachePCData} objects.
     * These represent data that have been modified and
     * were originally loaded from the cache. It is
     * up to the cache implementation to decide if
     * these values must be re-enlisted in the cache.
     * Some caches may return live data from {@link #get}
     * invocations, in which case these values need not be re-enlisted.
     * @param deletes A collection of object IDs that have been deleted
     * and must therefore be dropped from the cache.
     */
    public void commit(Collection<DataCachePCData> additions, Collection<DataCachePCData> newUpdates,
        Collection<DataCachePCData> existingUpdates, Collection<Object> deletes);

    /**
     * Returns <code>true</code> if this cache contains data
     * corresponding to <code>oid</code>; otherwise returns
     * <code>false</code>.
     */
    public boolean contains(Object oid);

    /**
     * Returns the indexes of the oids in this cache.
     */
    public BitSet containsAll(Collection<Object> oids);

    /**
     * Return the cached object for the given oid. Modifying the returned
     * object may or may not change the cached value; the {@link #update}
     * method should be used to re-cache any changed objects.
     *
     * @return the object matching the given oid, or null if none
     */
    public DataCachePCData get(Object oid);

    /**
     * Set the cached value for the given instance. This does <em>not</em>
     * result in an update of other caches. Rather, it should only be
     * used for loading clean data into the cache. Meaningful changes
     * to the state of the cache should be made via the {@link #commit} method.
     *
     * @return The previously cached value, or <code>null</code> if
     * the value was not previously cached. See {@link Map#put}
     * for more information.
     */
    public DataCachePCData put(DataCachePCData value);

    /**
     * Update the cached value for the given instance. This does
     * <em>not</em> result in an update of other caches. Rather, it should
     * only be used for loading clean data into the cache. Meaningful changes
     * to the state of the cache should be made via the {@link #commit} method.
     *  A cache implementation may or may not return a live object
     * from {@link #get} invocations. If an object retrieved from a
     * {@link #get} operation needs to be updated, this method can be
     * invoked instead of invoking {@link #put}. The DataCache implementation
     * can then make optimizations based on how its {@link #get} method works.
     */
    public void update(DataCachePCData value);

    /**
     * Remove the value stored under the given oid. This does
     * <em>not</em> result in an update of other caches. Rather, it
     * should only be used for removing data in the cache.
     * Meaningful changes to the state of the cache should be made
     * via the {@link #commit} method.
     *
     * @return The previously cached value, or <code>null</code> if
     * the oid was not previously cached. See {@link Map#remove}
     * for more information.
     */
    public DataCachePCData remove(Object oid);

    /**
     * Remove the values stored under the given oids.
     *
     * @return the indexes of the removed oids
     * @see #remove
     */
    public BitSet removeAll(Collection<Object> oids);

    /**
     * Evict all values of a specified type.
     */
    public void removeAll(Class<?> cls, boolean subclasses);

    /**
     * Remove all data from this cache. This does <em>not</em> result
     * in an update of other caches. Rather, it should only be used
     * for clearing the cache. Meaningful changes to the state of the
     * cache should be made via the {@link #commit} method.
     */
    public void clear();

    /**
     * Pin the value stored under <code>oid</code> into the cache.
     * This method guarantees that <code>oid</code>'s value will not
     * be dropped by the caching algorithm. This method does not
     * affect the behavior of {@link #remove}.
     *
     * @return <code>true</code> if <code>oid</code>'s value was
     * pinned into the cache; <code>false</code> if the oid is not in the cache.
     */
    public boolean pin(Object oid);

    /**
     * Pin all oids to the cache.
     *
     * @return the indexes of the pinned oids
     * @see #pin
     */
    public BitSet pinAll(Collection<Object> oids);

    /**
     * Pin all oids for the given type.
     * @param subs Whether to include subclasses.
     */
    public void pinAll(Class<?> cls, boolean subs);

    /**
     * Unpin the value stored under <code>oid</code> from the cache.
     * This method reverses a previous invocation of {@link #pin}.
     * This method does not remove anything from the cache; it merely
     * makes <code>oid</code>'s value a candidate for flushing from the cache.
     *
     * @return <code>true</code> if <code>oid</code>'s value was
     * unpinned from the cache; <code>false</code> if the
     * oid is not in the cache.
     */
    public boolean unpin(Object oid);

    /**
     * Unpin all oids from the cache.
     *
     * @return the indexes of the unpinned oids
     * @see #unpin
     */
    public BitSet unpinAll(Collection<Object> oids);

    /**
     * Unpin all oids associaed with the given type from the cache.
     * @param subs Whether to include subclasses.
     */
    public void unpinAll(Class<?> cls, boolean subs);

    /**
     * Obtain a write lock on the cache.
     */
    public void writeLock();

    /**
     * Release the write lock on the cache.
     */
    public void writeUnlock();

    /**
     * Add a new expiration event listener to this cache.
     *
     * @since 0.2.5.0
     */
    public void addExpirationListener(ExpirationListener listen);

    /**
     * Remove an expiration event listener from this cache.
     *
     * @since 0.2.5.0
     */
    public boolean removeExpirationListener(ExpirationListener listen);

    /**
     * Free the resources used by this cache.
	 */
	public void close ();
    
    /**
	 * Gets objects from the caches for a given list of keys.
	 * The returned map has the same keys as the given keys.
	 * If the cache does not contain data for a specific key,
	 * the returned map still contains the key with a null value.  
	 * 
     */
    public Map<Object,DataCachePCData> getAll(List<Object> keys);
    
    /**
     * Gets the named partition. Note that a partition itself is another cache.
     *  
     * @param name name of the given partition. 
     * 
     * @param create if true optionally create a new partition. 
     * 
     * @return a partition of the given name. Or null, if either no such partition exists or can not be created.
     * @since 2.0.0
     */
    public DataCache getPartition(String name, boolean create);
    
    /**
     * Gets the name of the known partitions. 
     * 
     * @return empty set if no partition exists.
     * 
     * @since 2.0.0
     */
    public Set<String> getPartitionNames();
        
    /**
     * Affirms if this cache maintains partitions.
     * 
     * @since 2.0.0
     */
    public boolean isPartitioned();
    
    /**
     * Returns number of read/write request and cache hit ratio data.
     */
    public CacheStatistics getStatistics();
    
    /**
     * Returns whether the the cache needs to be updated when bulk updates as executed. Defaults to true.
     */
    public boolean getEvictOnBulkUpdate();
}

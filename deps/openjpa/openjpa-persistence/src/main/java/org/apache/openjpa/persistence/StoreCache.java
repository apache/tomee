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

import javax.persistence.Cache;

import org.apache.openjpa.datacache.CacheStatistics;
import org.apache.openjpa.datacache.DataCache;

/**
 * Represents the L2 cache over the data store.
 *
 * @author Abe White
 * @since 0.4.1
 * @published
 */
public interface StoreCache extends Cache {

    public static final String NAME_DEFAULT = DataCache.NAME_DEFAULT;

    /**
     * Whether the cache contains data for the given oid.
     */
    public boolean contains(Class cls, Object oid);

    /**
     * Whether the cache contains data for the given oids.
     */
    public boolean containsAll(Class cls, Object... oids);

    /**
     * Whether the cache contains data for the given oids.
     */
    public boolean containsAll(Class cls, Collection oids);

    /**
     * Pin the data for the given oid to the cache.
     */
    public void pin(Class cls, Object oid);

    /**
     * Pin the data for the given oids to the cache.
     */
    public void pinAll(Class cls, Object... oids);

    /**
     * Pin the data for the given oids to the cache.
     */
    public void pinAll(Class cls, Collection oids);

    /**
     * Unpin the data for the given oid from the cache.
     */
    public void unpin(Class cls, Object oid);

    /**
     * Unpin the data for the given oids from the cache.
     */
    public void unpinAll(Class cls, Object... oids);

    /**
     * Unpin the data for the given oids from the cache.
     */
    public void unpinAll(Class cls, Collection oids);

    /**
     * Remove data for the given oid from the cache.
     */
    public void evict(Class cls, Object oid);
    /**
     * Remove data for the given oids from the cache.
     */
    public void evictAll(Class cls, Object... oids);

    /**
     * Remove data for the given oids from the cache.
     */
    public void evictAll(Class cls, Collection oids);

    /**
     * Clear the cache.
     */
    public void evictAll();
    
    /**
     * Gets the number of read/write/hit on this receiver in total and per
     * class basis.
     * 
     * @since 1.3.0
     */
    public CacheStatistics getStatistics();

    /**
     * @deprecated cast to {@link StoreCacheImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public DataCache getDelegate();
}

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

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.conf.ObjectValue;

/**
 * Manages the system's data and query caches. You can retrieve the data cache manager from the 
 * {@link OpenJPAConfiguration#getDataCacheManagerInstance()}.
 * <br>
 * Manages zero or more individual {@link DataCache caches} or partitions. Each individual partition
 * is identified by a string-based identifier.
 *  
 * Decides eligibility to cache for managed types.
 * 
 *
 * @author Abe White
 * @author Patrick Linskey
 * @author Pinaki Poddar
 */
public interface DataCacheManager {

    /**
     * Initialize the manager, supplying the cache configuration.
     */
    public void initialize(OpenJPAConfiguration conf, ObjectValue dataCache,
        ObjectValue queryCache);

    /**
     * Return the system-wide data cache, or null if caching is not enabled.
     */
    public DataCache getSystemDataCache();

    /**
     * Return the named data cache, or null if it does not exist.
     */
    public DataCache getDataCache(String name);

    /**
     * Return the named data cache. If the given name is null, the default
     * data cache is returned.
     *
     * @param create if true, the cache will be created if it does
     * not already exist
     */
    public DataCache getDataCache(String name, boolean create);

    /**
     * Return the system query cache, or null if not configured.
     */
    public QueryCache getSystemQueryCache();

    /**
     * Return the PCData generator if configured.
     */
    public DataCachePCDataGenerator getPCDataGenerator();

    /**
     * Return the runnable which schedules evictions.
     */
    public ClearableScheduler getClearableScheduler();
    
    /**
     * Select the cache where the given managed proxy instance should be cached.
     * This decision <em>may</em> override the cache returned by 
     * {@link CacheDistributionPolicy#selectCache(OpenJPAStateManager, Object) policy}
     * as specified by the user.  
     *  
     * @param sm the managed proxy instance
     * @return the cache that will store the state of the given managed instance.
     * 
     * @since 2.0.0
     */
    public DataCache selectCache(final OpenJPAStateManager sm);
    
    /**
     * Return the user-specific policy that <em>suggests</em> the cache where a managed entity state is stored.  
     * 
     * @since 2.0.0
     */
    public CacheDistributionPolicy getDistributionPolicy();

    /**
     * Close all caches.
     */
    public void close();
    
    /**
     * Stop caching the type matching the provided class name.
     */
    public void stopCaching(String cls);

    /**
     * Start caching the type matching the provided class name.
     */
    public void startCaching(String cls);

    /**
     * Returns the names of classes that are known to the cache and whether or not they are currently being cached.
     */
    public Map<String, Boolean> listKnownTypes();
}

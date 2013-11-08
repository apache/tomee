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
package org.apache.openjpa.instrumentation;

import java.util.Date;
import java.util.Map;

/**
 * Interface for providing instrumented data cache metrics and operations.
 */
public interface DataCacheInstrument {

    /**
     * Gets number of total read requests for the given class since last reset.
     */
    public long getReadCount(String className);

    /**
     * Gets number of total read requests that has been found in cache for the given class since last reset.
     */
    public long getHitCount(String className);

    /**
     * Gets number of total write requests for the given class since last reset.
     */
    public long getWriteCount(String className);

    /**
     * Gets number of total read requests for the given class since start.
     */
    public long getTotalReadCount(String className);

    /**
     * Gets number of total read requests that has been found in cache for the given class since start.
     */
    public long getTotalHitCount(String className);

    /**
     * Gets number of total write requests for the given class since start.
     */
    public long getTotalWriteCount(String className);

    /**
     * Returns the name of the cache
     */
    public String getCacheName();

    /**
     * Returns the hit count since cache statistics were last reset
     */
    public long getHitCount();

    /**
     * Returns the read count since cache statistics were last reset
     */
    public long getReadCount();

    /**
     * Returns the total hits since start.
     */
    public long getTotalHitCount();

    /**
     * Returns the total reads since start.
     */
    public long getTotalReadCount();

    /**
     * Returns the total writes since start.
     */
    public long getTotalWriteCount();

    /**
     * Returns the write count since cache statistics were last reset
     */
    public long getWriteCount();

    /**
     * Resets cache statistics
     */
    public void reset();

    /**
     * Returns date since cache statistics collection were last reset.
     */
    public Date sinceDate();

    /**
     * Returns date cache statistics collection started.
     */
    public Date startDate();

    /**
     * Returns the names of classes that are known to the cache and whether or not they are currently being cached.
     */
    public Map<String, Boolean> listKnownTypes();

    /**
     * Returns true if cache statistics are currently being calculated. False otherwise.
     */
    public Boolean getStatisticsEnabled();

    /**
     * 
     * @param enable - If true, the cache will start collecting statistics. Else cache statistics will not be collected.
     */
    public void collectStatistics(boolean enable);

    /**
     * This method is used to enable/disable caching for the specified className.
     */
    public void cache(String className, boolean enable);
    
    /**
     * Returns the CacheStatistics for the cache.
     * The format for this map is:
     *  Type(String) => Enabled(Boolean),Read(Long),Hit(Long),Write(Long)
     */
    public Map<String, long[]> getCacheStatistics();
    
    
    /**
     * Clears all data from the DataCache.
     */
    public void clear();
}

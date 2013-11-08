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

import org.apache.openjpa.datacache.CacheStatistics;
import org.apache.openjpa.datacache.CacheStatisticsSPI;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.lib.instrumentation.AbstractInstrument;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;

/**
 * Provides a basic instrument implementation wrapper for the data cache. This class can be extended to create a
 * provider specific instrument for the data cache.
 */
public abstract class AbstractDataCacheInstrument extends AbstractInstrument implements DataCacheInstrument {

    /**
     * Value indicating that cache statistics are not available.
     */
    public static final long NO_STATS = -1;

    private DataCacheManager _dcm = null;
    private DataCache _dc = null;
    private String _configID = null;
    private String _configRef = null;

    public void setDataCache(DataCache dc) {
        _dc = dc;
    }

    public void setDataCacheManager(DataCacheManager dcm) {
        _dcm = dcm;
    }

    public void setConfigId(String cid) {
        _configID = cid;
    }

    public void setContextRef(String cref) {
        _configRef = cref;
    }

    public long getHitCount() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getHitCount();
        return NO_STATS;
    }

    public long getReadCount() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getReadCount();
        return NO_STATS;
    }

    public long getTotalHitCount() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getTotalHitCount();
        return NO_STATS;
    }

    public long getTotalReadCount() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getTotalReadCount();
        return NO_STATS;
    }

    public long getTotalWriteCount() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getTotalWriteCount();
        return NO_STATS;
    }

    public long getWriteCount() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getWriteCount();
        return NO_STATS;
    }

    public void reset() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            stats.reset();
    }

    public Date sinceDate() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.since();
        return null;
    }

    public Date startDate() {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.start();
        return null;
    }

    public String getConfigId() {
        return _configID;
    }

    public String getContextRef() {
        return _configRef;
    }

    public String getCacheName() {
        if (_dc != null)
            return _dc.getName();
        return null;
    }

    private CacheStatistics getStatistics() {
        if (_dc != null) {
            return _dc.getStatistics();
        }
        return null;
    }

    public long getWriteCount(String c) {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getWriteCount(c);
        return NO_STATS;
    }

    public long getTotalWriteCount(String c) {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getTotalWriteCount(c);
        return NO_STATS;
    }

    public long getTotalReadCount(String c) {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getTotalReadCount(c);
        return NO_STATS;
    }

    public long getTotalHitCount(String c) {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getTotalHitCount(c);
        return NO_STATS;
    }

    public long getReadCount(String c) {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getReadCount(c);
        return NO_STATS;
    }

    public long getHitCount(String c) {
        CacheStatistics stats = getStatistics();
        if (stats != null)
            return stats.getHitCount(c);
        return NO_STATS;
    }

    public InstrumentationLevel getLevel() {
        return InstrumentationLevel.FACTORY;
    }

    public void cache(String className, boolean enable) {
        if (enable) {
            _dcm.startCaching(className);
        } else {
            _dcm.stopCaching(className);
        }
    }

    public Map<String, Boolean> listKnownTypes() {
        return _dcm.listKnownTypes();
    }
    public void collectStatistics(boolean enable) {
        CacheStatisticsSPI stats = (CacheStatisticsSPI) _dc.getStatistics();
        if (enable) {
            stats.enable();
        } else {
            stats.disable();
        }
    }

    public Boolean getStatisticsEnabled() {
        CacheStatisticsSPI stats = (CacheStatisticsSPI) _dc.getStatistics();
        return stats.isEnabled();
    }
    
    public Map<String, long[]> getCacheStatistics() {
        return _dc.getStatistics().toMap();
    }
    public void clear() {
        _dc.clear();
    }
}

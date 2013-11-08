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
import java.util.HashSet;
import java.util.Set;

import org.apache.openjpa.datacache.AbstractQueryCache;
import org.apache.openjpa.datacache.QueryCache;
import org.apache.openjpa.datacache.QueryKey;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.lib.instrumentation.AbstractInstrument;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;

/**
 * Provides a basic instrument implementation wrapper for the query cache.  This
 * class can be extended to create a provider specific instrument for the
 * query cache.
 */
public abstract class AbstractQueryCacheInstrument extends AbstractInstrument
    implements QueryCacheInstrument {

    /**
     * Value indicating that cache statistics are not available.
     */
    public static final long NO_STATS = -1;
    
    private QueryCache _qc;
    private String _configId = null;
    private String _configRef = null;
        
    public void setQueryCache(QueryCache qc) {
        _qc = qc;
    }
    
    public void setConfigId(String cid) {
        _configId = cid;
    }
    
    public void setContextRef(String cref) {
        _configRef = cref;
    }
    
    public String getConfigId() {
        return _configId;
    }

    public String getContextRef() {
        return _configRef;
    }
    
    public void setPreparedQueryCache(QueryCache qc) {
        _qc = qc;
    }
    
    private QueryStatistics<QueryKey> getStatistics() {
        if (_qc == null)
            return null;
        return _qc.getStatistics();
    }

    public long getExecutionCount() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.getExecutionCount();
        return NO_STATS;
    }

    public long getExecutionCount(String queryKey) {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null) {
            QueryKey qk = findKey(queryKey);
            return stats.getExecutionCount(qk);
        }
        return NO_STATS;
    }

    public long getTotalExecutionCount() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.getTotalExecutionCount();
        return NO_STATS;
    }

    public long getTotalExecutionCount(String queryKey) {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null) {
            QueryKey qk = findKey(queryKey);
            return stats.getTotalExecutionCount(qk);
        }
        return NO_STATS;
    }

    public long getHitCount() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.getHitCount();
        return NO_STATS;
    }

    public long getHitCount(String queryKey) {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null) {
            QueryKey qk = findKey(queryKey);
            return stats.getHitCount(qk);
        }
        return NO_STATS;
    }

    public long getTotalHitCount() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.getTotalHitCount();
        return NO_STATS;
    }

    public long getTotalHitCount(String queryKey) {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null) {
            QueryKey qk = findKey(queryKey);
            return stats.getTotalHitCount(qk);
        }
        return NO_STATS;
    }

    public void reset() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            stats.reset();        
    }
    
    public Date sinceDate() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.since();
        return null;
    }
    
    public Date startDate() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.start();        
        return null;
    }
    
    /**
     * Returns number of total evictions since last reset
     */
    public long getEvictionCount() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.getEvictionCount();
        return NO_STATS;
    }
    
    /**
     * Returns number of total eviction requests since start.
     */
    public long getTotalEvictionCount() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null)
            return stats.getTotalEvictionCount();
        return NO_STATS;
    }

    /**
     * Returns all query keys currently tracked in the cache.
     * @return
     */
    public Set<String> queryKeys() {
        QueryStatistics<QueryKey> stats = getStatistics();
        if (stats != null) {
            Set<String> keys = new HashSet<String>();
            for (QueryKey qk : stats.keys()) {
                keys.add(qk.toString());
            }
            return keys;
        }
        return null;
    }

    private QueryKey findKey(String key) {
        QueryStatistics<QueryKey> stats = getStatistics();
        for (QueryKey qk : stats.keys()) {
            if (qk.toString().equals(key)) {
                return qk;
            }
        }
        return null;
    }
    
    public long count() {
        if (_qc == null) {
            return NO_STATS;
        }
        if (_qc instanceof AbstractQueryCache) {
            AbstractQueryCache aqc = (AbstractQueryCache)_qc;
            return aqc.count();
        }
        return NO_STATS;
    }

    public InstrumentationLevel getLevel() {
        return InstrumentationLevel.FACTORY;
    }
}

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
import java.util.Set;

import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.lib.instrumentation.AbstractInstrument;
import org.apache.openjpa.lib.instrumentation.InstrumentationLevel;

/**
 * Provides a basic instrument implementation wrapper for the prepared query cache.  This
 * class can be extended to create a provider specific instrument for the
 * prepared query cache.
 */
public abstract class AbstractPreparedQueryCacheInstrument extends AbstractInstrument  
    implements PreparedQueryCacheInstrument {

    public static final long NO_STATS = -1;
    
    private PreparedQueryCache _qc;
    private String _configID = null;
    private String _configRef = null;

    public void setConfigId(String cid) {
        _configID = cid;
    }
    
    public void setContextRef(String cref) {
        _configRef = cref;
    }
    
    public String getConfigId() {
        return _configID;
    }

    public String getContextRef() {
        return _configRef;
    }
    
    public void setPreparedQueryCache(PreparedQueryCache qc) {
        _qc = qc;
    }
    
    private QueryStatistics<String> getStatistics() {
        if (_qc == null)
            return null;
        return _qc.getStatistics();
    }

    public long getExecutionCount() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getExecutionCount();
        return NO_STATS;
    }

    public long getExecutionCount(String query) {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getExecutionCount(query);
        return NO_STATS;
    }

    public long getTotalExecutionCount() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getTotalExecutionCount();
        return NO_STATS;
    }

    public long getTotalExecutionCount(String query) {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getTotalExecutionCount(query);
        return NO_STATS;
    }

    public long getHitCount() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getHitCount();
        return NO_STATS;
    }

    public long getHitCount(String query) {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getHitCount(query);
        return NO_STATS;
    }

    public long getTotalHitCount() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getTotalHitCount();
        return NO_STATS;
    }

    public long getTotalHitCount(String query) {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.getTotalHitCount(query);
        return NO_STATS;
    }

    public void reset() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            stats.reset();        
    }
    
    public Date sinceDate() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.since();
        return null;
    }
    
    public Date startDate() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.start();        
        return null;
    }
    
    public Set<String> queries() {
        QueryStatistics<String> stats = getStatistics();
        if (stats != null)
            return stats.keys();
        return null;
    }
    
    public InstrumentationLevel getLevel() {
        return InstrumentationLevel.FACTORY;
    }
}

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
package org.apache.openjpa.jdbc.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.FinderCache;
import org.apache.openjpa.kernel.FinderQuery;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Implementation of FinderCache for JDBC.
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
public class FinderCacheImpl 
    implements FinderCache<ClassMapping, SelectExecutor, Result> {
    private static final String PATTERN_SEPARATOR = "\\;";
    private static final String EXLUDED_BY_USER = "Excluded by user";
     
    private final Map<ClassMapping, FinderQuery<ClassMapping, SelectExecutor, Result>> _delegate;
    // Key: class name Value: Reason why excluded
    private final Map<String, String> _uncachables;
    private List<String> _exclusionPatterns;
    private QueryStatistics<ClassMapping> _stats;
    private ReentrantLock _lock = new ReentrantLock();
    private boolean _enableStats = false;
    
    public FinderCacheImpl() {
        _delegate = new HashMap<ClassMapping, FinderQuery<ClassMapping, SelectExecutor, Result>>();
        _uncachables = new HashMap<String, String>();
        _stats = new QueryStatistics.None<ClassMapping>();
    }
    
    /**
     * Get a map-oriented view of the cache.
     * 
     * @return a map of the query string with class names as key. 
     */
    public Map<String, String> getMapView() {
        lock();
        try {
            Map<String, String> view = new TreeMap<String, String>();
            for (ClassMapping mapping : _delegate.keySet()) {
                view.put(mapping.getDescribedType().getName(), 
                    _delegate.get(mapping).getQueryString());
            }
            return view;
        } finally {
            unlock();
        }
    }

    /**
     * Gets basic statistics of execution and hit count of finder queries. 
     */
    public QueryStatistics<ClassMapping> getStatistics() {
        return _stats;
    }

    /**
     * Gets the finder query for the given mapping. The get operation can be
     * controlled by FetchConfiguration hints. 
     * {@link QueryHints#HINT_IGNORE_FINDER HINT_IGNORE_FINDER} will ignore
     * any cached finder that may exist in this cache and will return null.
     * {@link QueryHints#HINT_INVALIDATE_FINDER HINT_INVALIDATE_FINDER} will 
     * invalidate any cached finder that may exist in this cache and will return
     * null.
     * 
     */
    public FinderQuery<ClassMapping,SelectExecutor,Result> 
        get(ClassMapping mapping, FetchConfiguration fetch) {
        if (fetch.getReadLockLevel() != 0) {
            return null;
        }
        
        // FinderCache only operates with Default Fetch Plans
        if (!fetch.isFetchConfigurationSQLCacheAdmissible()) {
            return null;
        }
        
        boolean ignore = isHinted(fetch, QueryHints.HINT_IGNORE_FINDER);
        boolean invalidate = isHinted(fetch, QueryHints.HINT_INVALIDATE_FINDER);
        if (invalidate) {
            invalidate(mapping);
        }
        if (ignore) {
            return null;
        }
        FinderQuery<ClassMapping, SelectExecutor, Result> result = _delegate.get(mapping);
        _stats.recordExecution(mapping);
        return result;
    }
    
    /**
     * Cache a Finder Query for the given mapping and select. The put operation 
     * can be controlled by FetchConfiguration hints. 
     * If no entry exists for the given mapping then an attempt is made to 
     * create a new FinderQuery. The attempt, however, may not be successful
     * because all Selects can not be cached.
     * @see FinderQueryImpl#newFinder(ClassMapping, Select).
     *  
     * If a entry for the given mapping exists then the value of
     * {@link QueryHints#HINT_RECACHE_FINDER HINT_RECACHE_FINDER} hint 
     * determines whether the existing entry is returned or a new FinderQuery 
     * with the given argument overwrites the existing one.
     * 
     * @param mapping the class for which the finder is to be cached
     * @param select the finder query
     * @param fetch may contain hints to control cache operation
     */
    public FinderQuery<ClassMapping, SelectExecutor, Result> cache
       (ClassMapping mapping, SelectExecutor select, FetchConfiguration fetch) {
        lock();
        try {
            if (fetch.getReadLockLevel() != 0) {
                return null;
            }
            
            // FinderCache only operates with Default Fetch Plans
            if (!fetch.isFetchConfigurationSQLCacheAdmissible()) {
                return null;
            }           
            
            boolean recache = isHinted(fetch, QueryHints.HINT_RECACHE_FINDER);
            if (isExcluded(mapping)) {
                return recache ? put(mapping, select) : null;
            }
            if (_delegate.containsKey(mapping)) {
                return recache ? put(mapping, select) : _delegate.get(mapping);
            }
            return put(mapping, select);
        } finally {
            unlock();
        }
    }
    
    /**
     * Creates and puts a FinderQuery in the internal map indexed by the
     * given ClassMapping.
     * If a new FinderQuery can not be created for the given Select (because
     * some Select are not cached), then the mapping is marked invalid.
     *  
    */
    private FinderQuery<ClassMapping, SelectExecutor, Result> put(ClassMapping mapping, SelectExecutor select) {
        FinderQuery<ClassMapping, SelectExecutor, Result> finder = FinderQueryImpl.newFinder(mapping, select);
        if (finder != null) {
            _delegate.put(mapping, finder);
        } else {
            invalidate(mapping);
        }
        return finder;
    }
    
    /**
     * Affirms if the given mapping is excluded from being cached.
     */
    public boolean isExcluded(ClassMapping mapping) {
        return mapping != null && isExcluded(mapping.getDescribedType().getName());
    }

    /**
     * Searches the exclusion patterns to find out if the given string matches
     * any element.
     */
    private boolean isExcluded(String target) {
        if (_exclusionPatterns != null && _exclusionPatterns.contains(target))
            return true;
        return getMatchedExclusionPattern(target) != null;
    }

    /**
     * Adds a pattern for exclusion. Any cached finder whose class name
     * matches the given pattern will be marked invalidated as a side-effect.
     */
    public void addExclusionPattern(String pattern) {
        lock();
        try {
            if (_exclusionPatterns == null)
                _exclusionPatterns = new ArrayList<String>();
            _exclusionPatterns.add(pattern);
            Collection<ClassMapping> invalidMappings = getMatchedKeys(pattern, 
                    _delegate.keySet());
            for (ClassMapping invalidMapping : invalidMappings)
                markUncachable(invalidMapping, pattern);
        } finally {
            unlock();
        }
    }
    /**
     * Removes a pattern for exclusion. Any query identifier marked as not 
     * cachable due to the given pattern will now be removed from the list of
     * uncachables as a side-effect.
     */
    public void removeExclusionPattern(String pattern) {
        lock();
        try {
            if (_exclusionPatterns == null)
                return;
            _exclusionPatterns.remove(pattern);
            Collection<String> reborns = getMatchedKeys(pattern, 
                _uncachables.keySet());
            for (String rebornKey : reborns)
                _uncachables.remove(rebornKey);
        } finally {
            unlock();
        }
    }
    
    /**
     * Gets the pattern that matches the given identifier.
     */
    private String getMatchedExclusionPattern(String id) {
        if (_exclusionPatterns == null || _exclusionPatterns.isEmpty())
            return null;
        for (String pattern : _exclusionPatterns)
            if (matches(pattern, id))
                return pattern;
        return null;
    }
    
    /**
     * Gets the elements of the given set that match the given pattern. 
     */
    private Collection<ClassMapping> getMatchedKeys(String pattern, Set<ClassMapping> set) {
        List<ClassMapping> result = new ArrayList<ClassMapping>();
        for (ClassMapping entry : set) {
            if (matches(pattern, entry)) {
                result.add(entry);
            }
        }
        return result;
    }
    
    /**
     * Gets the elements of the given list which match the given pattern. 
     */
    private Collection<String> getMatchedKeys(String pattern, Collection<String> coll) {
        List<String> result = new ArrayList<String>();
        for (String key : coll) {
            if (matches(pattern, key)) {
                result.add(key);
            }
        }
        return result;
    }

    boolean matches(String pattern, ClassMapping mapping) {
        return matches(pattern, mapping.getDescribedType().getName());
    }
    
    boolean matches(String pattern, String target) {
        return target != null && (target.equals(pattern) 
          || target.matches(pattern));
    }
    
    public boolean invalidate(ClassMapping mapping) {
        lock();
        try {
            return _delegate.remove(mapping) != null;
        } finally {
            unlock();
        }
    }

    public FinderQuery<ClassMapping, SelectExecutor, Result> markUncachable(ClassMapping mapping) {
        return markUncachable(mapping.getDescribedType().getName());
    }

    public FinderQuery<ClassMapping, SelectExecutor, Result> markUncachable(String id) {
        return markUncachable(id, EXLUDED_BY_USER);
    }
    
    private FinderQuery<ClassMapping, SelectExecutor, Result> markUncachable(String cls, String reason) {
        lock();
        try {
            boolean excludedByUser = _uncachables.get(cls) == EXLUDED_BY_USER;
            if (!excludedByUser)
                _uncachables.put(cls, reason);
            return _delegate.remove(searchMappingByName(cls));
        } finally {
            unlock();
        }
    }
    
    private FinderQuery<ClassMapping, SelectExecutor, Result> markUncachable(ClassMapping mapping, String reason) {
        lock();
        try {
            String cls = mapping.getDescribedType().getName();
            boolean excludedByUser = _uncachables.get(cls) == EXLUDED_BY_USER;
            if (!excludedByUser)
                _uncachables.put(cls, reason);
            return _delegate.remove(mapping);
        } finally {
            unlock();
        }
    }
    
    ClassMapping searchMappingByName(String cls) {
        for (ClassMapping mapping : _delegate.keySet())
            if (matches(cls, mapping))
                return mapping;
        return null;
    }

    
    public void setExcludes(String excludes) {
        lock();
        try {
            if (StringUtils.isEmpty(excludes))
                return;
            if (_exclusionPatterns == null)
                _exclusionPatterns = new ArrayList<String>();
            String[] patterns = excludes.split(PATTERN_SEPARATOR);
            for (String pattern : patterns)
                addExclusionPattern(pattern);
        } finally {
            unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getExcludes() {
        return (List<String>)_exclusionPatterns == null 
            ? Collections.EMPTY_LIST 
            : Collections.unmodifiableList(_exclusionPatterns);
    }
    
    boolean isHinted(FetchConfiguration fetch, String hint) {
        if (fetch == null)
            return false;
        Object result = fetch.getHint(hint);
        return result != null && "true".equalsIgnoreCase(result.toString());
    }
        
    void lock() {
        if (_lock != null)
            _lock.lock();
    }

    void unlock() {
        if (_lock != null && _lock.isLocked())
            _lock.unlock();
    }
     
    public void setEnableStats(boolean b) { 
        _enableStats = b;
        if (_enableStats) {
            _stats = new QueryStatistics.Default<ClassMapping>();
        }
    }

    public boolean getEnableStats() {
        return _enableStats;
    }
    // ----------------------------------------------------
    //  Configuration contract
    // ----------------------------------------------------
    public void startConfiguration() {
    }
    
    public void setConfiguration(Configuration conf) {
    }

    public void endConfiguration() {
    }
}

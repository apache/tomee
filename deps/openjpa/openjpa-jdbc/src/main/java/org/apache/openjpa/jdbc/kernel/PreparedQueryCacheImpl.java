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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.PreparedQuery;
import org.apache.openjpa.kernel.PreparedQueryCache;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.CacheMap;

/**
 * An implementation of the cache of {@link PreparedQuery prepared queries}. 
 * 
 * @author Pinaki Poddar
 *
 * @since 2.0.0
 * 
 * @nojavadoc
 */
public class PreparedQueryCacheImpl implements PreparedQueryCache {
	private static final String PATTERN_SEPARATOR = "\\;";
	// Key: Query identifier 
	private final Map<String, PreparedQuery> _delegate;
	// Key: Query identifier Value: Reason why excluded
	private final Map<String, Exclusion> _uncachables;
	private final List<Exclusion> _exclusionPatterns;
	private QueryStatistics<String> _stats;
	private boolean _statsEnabled;

	private Lock _writeLock;
	private Lock _readLock;
	private Log _log;
    private static Localizer _loc = Localizer.forPackage(PreparedQueryCacheImpl.class);
    
	public PreparedQueryCacheImpl() {
		_delegate = new CacheMap();
		_uncachables = new CacheMap();
		_exclusionPatterns = new ArrayList<Exclusion>();
		
		ReentrantReadWriteLock _rwl = new ReentrantReadWriteLock();
        _writeLock = _rwl.writeLock();
        _readLock = _rwl.readLock();
	}
	
    public Boolean register(String id, Query query, FetchConfiguration hints) {
        if (id == null 
            || query == null 
            || QueryLanguages.LANG_SQL.equals(query.getLanguage()) 
            || QueryLanguages.LANG_METHODQL.equals(query.getLanguage())
            || isHinted(hints, QueryHints.HINT_IGNORE_PREPARED_QUERY)
            || isHinted(hints, QueryHints.HINT_INVALIDATE_PREPARED_QUERY))
            return Boolean.FALSE;
        if (Boolean.FALSE.equals(isCachable(id)))
            return Boolean.FALSE;
        PreparedQuery cached = get(id);
        if (cached != null)
            return null; // implies that it is already cached
        
        PreparedQuery newEntry = new PreparedQueryImpl(id, query); 
        return cache(newEntry);
	}
	
	public Map<String,String> getMapView() {
		lock(false);
		try {
            Map<String, String> view = new TreeMap<String, String>();
            for (Map.Entry<String, PreparedQuery> entry : _delegate.entrySet())
                view.put(entry.getKey(), entry.getValue().getTargetQuery());
			return view;
		} finally {
			unlock(false);
		}
	}
	
	/**
	 * Cache the given query keyed by its identifier. Does not cache if the 
	 * identifier matches any exclusion pattern or has been marked as 
	 * non-cachable. Also register the identifier as not cachable against 
	 * the matched exclusion pattern.
	 */
	public boolean cache(PreparedQuery q) {
		lock(false);
		try {
			String id = q.getIdentifier();
			if (Boolean.FALSE.equals(isCachable(id))) {
				if (_log != null && _log.isTraceEnabled())
                    _log.trace(_loc.get("prepared-query-not-cachable", id));
				return false;
			}
			Exclusion exclusion = getMatchedExclusionPattern(id);
			if (exclusion != null) {
				markUncachable(id, exclusion);
				return false;
			}
			_delegate.put(id, q);
            if (_log != null && _log.isTraceEnabled())
                _log.trace(_loc.get("prepared-query-cached", id));
			return true;
		} finally {
			unlock(false);
		}
	}
	
    public PreparedQuery initialize(String key, Object result) {
        PreparedQuery pq = get(key);
        if (pq == null)
            return null;
        
        Exclusion exclusion = pq.initialize(result);
        if (exclusion != null) {
            markUncachable(key, exclusion);
            return null;
        } 
        return pq;
    }
	
	public boolean invalidate(String id) {
		lock(false);
		try {
			if (_log != null && _log.isTraceEnabled())
                _log.trace(_loc.get("prepared-query-invalidate", id));
			boolean rc = _delegate.remove(id) != null;
			if (_statsEnabled && rc) {
			    _stats.recordEviction(id);
			}
			return rc;
		} finally {
			unlock(false);
		}
	}
	
    public PreparedQuery get(String id) {
        lock(true);
        try {
            return _delegate.get(id);
        } finally {
            unlock(true);
        }
    }
    
	public Boolean isCachable(String id) {
		lock(true);
		try {
			if (_uncachables.containsKey(id))
				return Boolean.FALSE;
			if (_delegate.containsKey(id))
				return Boolean.TRUE;
			return null;
		} finally {
			unlock(true);
		}
	}
	
	public PreparedQuery markUncachable(String id, Exclusion exclusion) {
		lock(false);
		try {
			if (_uncachables.put(id, exclusion) == null) {
			    if (_log != null && _log.isTraceEnabled()) 
			        _log.trace(_loc.get("prepared-query-uncache", id, exclusion));
			}
			PreparedQuery pq = _delegate.remove(id);
            if (_statsEnabled && pq != null) {
                _stats.recordEviction(id);
            }
            return pq;
		} finally {
			unlock(false);
		}
	}
	
	public Exclusion isExcluded(String id) {
		return getMatchedExclusionPattern(id);
	}
	
	public void setExcludes(String excludes) {
		lock(false);
		try {
			if (StringUtils.isEmpty(excludes))
				return;
			String[] patterns = excludes.split(PATTERN_SEPARATOR);
			for (String pattern : patterns)
				addExclusionPattern(pattern);
		} finally {
			unlock(false);
		}
	}

	public List<Exclusion> getExcludes() {
		return Collections.unmodifiableList(_exclusionPatterns);
	}
	
	/**
     * Adds a pattern for exclusion. Any query cached currently whose identifier
     * matches the given pattern will be marked invalidated as a side-effect.
	 */
	public void addExclusionPattern(String pattern) {
		lock(false);
		try {
		    String reason = _loc.get("prepared-query-excluded-by-user", pattern).getMessage();
			Exclusion exclusion = new WeakExclusion(pattern, reason);
			_exclusionPatterns.add(exclusion);
            Collection<String> invalidKeys = getMatchedKeys(pattern, _delegate.keySet());
			for (String invalidKey : invalidKeys) {
			    Exclusion invalid = new WeakExclusion(invalidKey, reason);
				markUncachable(invalidKey, invalid);
			}
		} finally {
			unlock(false);
		}
	}
	
	/**
	 * Removes a pattern for exclusion. Any query identifier marked as not 
     * cachable due to the given pattern will now be removed from the list of
	 * uncachables as a side-effect.
	 */
	public void removeExclusionPattern(String pattern) {
		lock(false);
		try {
            Exclusion exclusion = new WeakExclusion(pattern, null);
			_exclusionPatterns.remove(exclusion);
            Collection<String> reborns = getMatchedKeys(pattern, _uncachables);
			for (String rebornKey : reborns) {
                _uncachables.remove(rebornKey);
	            if (_log != null && _log.isTraceEnabled())
	                _log.trace(_loc.get("prepared-query-remove-pattern", pattern, rebornKey));
			}
		} finally {
			unlock(false);
		}
	}
	
	public QueryStatistics<String> getStatistics() {
		return _stats;
	}
	
	/**
	 * Gets the pattern that matches the given identifier.
	 */
	private Exclusion getMatchedExclusionPattern(String id) {
		for (Exclusion pattern : _exclusionPatterns)
			if (pattern.matches(id))
				return pattern;
		return null;
	}
	
	/**
	 * Gets the keys of the given map whose values match the given pattern. 
	 */
	private Collection<String> getMatchedKeys(String pattern, Map<String,Exclusion> map) {
        List<String> result = new ArrayList<String>();
		for (Map.Entry<String, Exclusion> entry : map.entrySet()) {
		    Exclusion exclusion = entry.getValue();
			if (!exclusion.isStrong() && exclusion.matches(pattern)) {
				result.add(entry.getKey());
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

	/**
     * Note: Care needs to be taken so that a read lock is <b>never</b> held while requesting a write lock. This will
     * result in a deadlock.
     * 
     * @param readOnly
     *            - If true, a read lock will be acquired. Else a write lock will be acquired.
     */
    protected void lock(boolean readOnly) {
        if (readOnly == true) {
            _readLock.lock();
        } else {
            _writeLock.lock();
        }
    }

    /**
     * @param readOnly
     *            - If true, the read lock will be released. Else a write lock will be released.
     */
    protected void unlock(boolean readOnly) {
        if (readOnly == true) {
            _readLock.unlock();
        } else {
            _writeLock.unlock();
        }
    }
    
    boolean matches(String pattern, String target) {
    	return target != null && (target.equals(pattern) 
    	  || target.matches(pattern));
    }
    
    boolean isHinted(FetchConfiguration fetch, String hint) {
        if (fetch == null)
            return false;
        Object result = fetch.getHint(hint);
        return result != null && "true".equalsIgnoreCase(result.toString());
    }
    
    public void clear() {
        _delegate.clear();
        _stats.clear();
    }
    
    public void setEnableStatistics(boolean enable){
        _statsEnabled = enable;
    }
    
    public boolean getEnableStatistics(){
        return _statsEnabled;
    }
        
	//-------------------------------------------------------
	// Configurable contract
	//-------------------------------------------------------
    public void setConfiguration(Configuration conf) {
    	_log = conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        _stats = _statsEnabled ? new QueryStatistics.Default<String>() :
                                 new QueryStatistics.None<String>();
    }
    
    /**
     * An immutable abstract pattern for exclusion.
     *
     */
    private static abstract class ExclusionPattern implements PreparedQueryCache.Exclusion {
        private final boolean _strong;
        private final String  _pattern;
        private final String  _reason;
        
        private static Localizer _loc = Localizer.forPackage(PreparedQueryCacheImpl.class);
        private static String STRONG = _loc.get("strong-exclusion").getMessage();
        private static String WEAK   = _loc.get("weak-exclusion").getMessage();
        
        public ExclusionPattern(boolean strong, String pattern, String reason) {
            super();
            this._strong = strong;
            this._pattern = pattern;
            this._reason = reason;
        }

        public String getPattern() {
            return _pattern;
        }

        public String getReason() {
            return _reason;
        }

        public boolean isStrong() {
            return _strong;
        }

        public boolean matches(String id) {
            return _pattern != null && (_pattern.equals(id) || _pattern.matches(id));
        }
        
        /**
         * Equals by strength and pattern (not by reason).
         */
        @Override
        public final boolean equals(Object other) {
            if (other == this)
                return true;
            if (!(other instanceof Exclusion))
                return false;
            Exclusion that = (Exclusion)other;
            return this._strong == that.isStrong() 
                && StringUtils.equals(this._pattern, that.getPattern());
        }
        
        @Override
        public int hashCode() {
            return (_strong ? 1 : 0) 
                 + (_pattern == null ? 0 : _pattern.hashCode());
        }
        
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(" ").append(_strong ? STRONG : WEAK).append(". ");
            if (_reason != null)
                buf.append(_reason);
            return buf.toString();
        }
    }
    
    /**
     * Strong exclusion.
     *
     */
    public static class StrongExclusion extends ExclusionPattern {

        public StrongExclusion(String pattern, String reason) {
            super(true, pattern, reason);
        }
    }
    
    /**
     * Weak exclusion.
     *
     */
    public static class WeakExclusion extends ExclusionPattern {

        public WeakExclusion(String pattern, String reason) {
            super(false, pattern, reason);
        }
    }
}

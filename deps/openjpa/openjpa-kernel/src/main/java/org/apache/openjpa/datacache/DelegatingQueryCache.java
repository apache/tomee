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

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.kernel.QueryStatistics;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

/**
 * Delegating query cache that can also perform exception translation for
 * use in facades. This cache allows its delegate to be null, in which case
 * it returns default values or all methods.
 *
 * @author Abe White
 * @since 0.4.0
 * @nojavadoc
 */
public class DelegatingQueryCache
    implements QueryCache {

    private final QueryCache _cache;
    private final DelegatingQueryCache _del;
    private final RuntimeExceptionTranslator _trans;

    /**
     * Constructor. Supply delegate.
     */
    public DelegatingQueryCache(QueryCache cache) {
        this(cache, null);
    }

    public DelegatingQueryCache(QueryCache cache,
        RuntimeExceptionTranslator trans) {
        _cache = cache;
        _trans = trans;
        if (cache instanceof DelegatingQueryCache)
            _del = (DelegatingQueryCache) _cache;
        else
            _del = null;
    }

    /**
     * Return the direct delegate.
     */
    public QueryCache getDelegate() {
        return _cache;
    }

    /**
     * Return the native delegate.
     */
    public QueryCache getInnermostDelegate() {
        return (_del == null) ? _cache : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingQueryCache)
            other = ((DelegatingQueryCache) other).getInnermostDelegate();
        return ObjectUtils.equals(getInnermostDelegate(), other);
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public void initialize(DataCacheManager mgr) {
        if (_cache == null)
            return;
        try {
            _cache.initialize(mgr);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void onTypesChanged(TypesChangedEvent e) {
        if (_cache == null)
            return;
        try {
            _cache.onTypesChanged(e);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public QueryResult get(QueryKey qk) {
        if (_cache == null)
            return null;
        try {
            return _cache.get(qk);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public QueryResult put(QueryKey qk, QueryResult oids) {
        if (_cache == null)
            return null;
        try {
            return _cache.put(qk, oids);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public QueryResult remove(QueryKey qk) {
        if (_cache == null)
            return null;
        try {
            return _cache.remove(qk);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void clear() {
        if (_cache == null)
            return;
        try {
            _cache.clear();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean pin(QueryKey qk) {
        if (_cache == null)
            return false;
        try {
            return _cache.pin(qk);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean unpin(QueryKey qk) {
        if (_cache == null)
            return false;
        try {
            return _cache.unpin(qk);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void writeLock() {
        if (_cache == null)
            return;
        try {
            _cache.writeLock();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void writeUnlock() {
        if (_cache == null)
            return;
        try {
            _cache.writeUnlock();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void addTypesChangedListener(TypesChangedListener listen) {
        if (_cache == null)
            return;
        try {
            _cache.addTypesChangedListener(listen);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean removeTypesChangedListener(TypesChangedListener listen) {
        if (_cache == null)
            return false;
        try {
            return _cache.removeTypesChangedListener(listen);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void close() {
        if (_cache == null)
            return;
        try {
            _cache.close();
        } catch (RuntimeException re) {
            throw translate(re);
		}
	}

    public QueryStatistics<QueryKey> getStatistics() {
        if (_cache == null)
            return null;
        try {
            return _cache.getStatistics();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
}

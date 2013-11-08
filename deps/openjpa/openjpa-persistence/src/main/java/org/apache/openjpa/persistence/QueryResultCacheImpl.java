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

import java.util.Collections;
import javax.persistence.Query;

import org.apache.openjpa.datacache.DelegatingQueryCache;
import org.apache.openjpa.datacache.QueryCache;
import org.apache.openjpa.datacache.QueryKey;
import org.apache.openjpa.datacache.TypesChangedEvent;

/**
 * Implements Query result cache via delegation to QueryCache.
 *
 * @author Abe White
 * @since 0.4.1
 * @nojavadoc
 */
public class QueryResultCacheImpl
	implements QueryResultCache {

    private final DelegatingQueryCache _cache;

    /**
     * Constructor; supply delegate.
     */
    public QueryResultCacheImpl(QueryCache cache) {
        _cache = new DelegatingQueryCache(cache,
            PersistenceExceptions.TRANSLATOR);
    }

    /**
     * Delegate.
     */
    public QueryCache getDelegate() {
        return _cache.getDelegate();
    }

    public void pin(Query q) {
        if (_cache.getDelegate() != null)
            _cache.pin(toQueryKey(q));
    }

    public void unpin(Query q) {
        if (_cache.getDelegate() != null)
            _cache.unpin(toQueryKey(q));
    }

    public void evict(Query q) {
        if (_cache.getDelegate() != null)
            _cache.remove(toQueryKey(q));
    }

    public void evictAll() {
        _cache.clear();
    }

    public void evictAll(Class cls) {
        _cache.onTypesChanged(new TypesChangedEvent(this,
            Collections.singleton(cls)));
    }

    /**
     * Return a cache key for the given query.
     */
    private QueryKey toQueryKey(Query q) {
        QueryImpl impl = (QueryImpl) q;
        if (impl.hasPositionalParameters())
            return QueryKey.newInstance(impl.getDelegate(),
                impl.getPositionalParameters());
        return QueryKey.newInstance(impl.getDelegate(),
            impl.getNamedParameters());
    }

    public int hashCode() {
        return (_cache == null) ? 0 : _cache.hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if ((other == null) || (other.getClass() != this.getClass()))
            return false;
        if (_cache == null)
        	return false;
        
        return _cache.equals(((QueryResultCacheImpl) other)._cache);
	}
}

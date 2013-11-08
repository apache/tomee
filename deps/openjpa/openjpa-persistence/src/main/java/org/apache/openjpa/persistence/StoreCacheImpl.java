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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

import org.apache.openjpa.datacache.CacheStatistics;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.datacache.DelegatingDataCache;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;

/**
 * Implements the L2 cache over the data store via delegation to DataCache.
 *
 * @author Abe White
 * @since 0.4.1
 * @nojavadoc
 */
public class StoreCacheImpl 
	implements StoreCache {

    private final MetaDataRepository _repos;
    private final DelegatingDataCache _cache;

    
    /**
     * Constructor; supply delegate.
     */
    public StoreCacheImpl(EntityManagerFactoryImpl emf, DataCache cache) {
        _repos = emf.getConfiguration().getMetaDataRepositoryInstance();
        _cache = new DelegatingDataCache(cache,
            PersistenceExceptions.TRANSLATOR);
    }

    /**
     * Delegate.
     */
    public DataCache getDelegate() {
        return _cache.getDelegate();
    }

    public boolean contains(Class cls, Object oid) {
        return _cache.getDelegate() != null && _cache.contains
            (JPAFacadeHelper.toOpenJPAObjectId(getMetaData(cls), oid));
    }

    public boolean containsAll(Class cls, Object... oids) {
        return containsAll(cls, Arrays.asList(oids));
    }

    public boolean containsAll(Class cls, Collection oids) {
        if (_cache.getDelegate() == null)
            return oids.isEmpty();

        BitSet set = _cache.containsAll(JPAFacadeHelper.toOpenJPAObjectIds
            (getMetaData(cls), oids));
        for (int i = 0; i < oids.size(); i++)
            if (!set.get(i))
                return false;
        return true;
    }

    public void pin(Class cls, Object oid) {
        if (_cache.getDelegate() != null)
            _cache.pin(JPAFacadeHelper.toOpenJPAObjectId(getMetaData(cls),
                oid));
    }

    public void pinAll(Class cls, Object... oids) {
        pinAll(cls, Arrays.asList(oids));
    }

    public void pinAll(Class cls, Collection oids) {
        if (_cache.getDelegate() != null)
            _cache.pinAll(JPAFacadeHelper.toOpenJPAObjectIds
                (getMetaData(cls), oids));
    }

    public void unpin(Class cls, Object oid) {
        if (_cache.getDelegate() != null)
            _cache.unpin(JPAFacadeHelper.toOpenJPAObjectId(getMetaData(cls),
                oid));
    }

    public void unpinAll(Class cls, Object... oids) {
        unpinAll(cls, Arrays.asList(oids));
    }

    public void unpinAll(Class cls, Collection oids) {
        if (_cache.getDelegate() != null)
            _cache.unpinAll(JPAFacadeHelper.toOpenJPAObjectIds
                (getMetaData(cls), oids));
    }

    public void evict(Class cls, Object oid) {
        if (_cache.getDelegate() != null)
            _cache.remove(JPAFacadeHelper.toOpenJPAObjectId(getMetaData(cls),
                oid));
    }

    public void evictAll(Class cls, Object... oids) {
        evictAll(cls, Arrays.asList(oids));
    }

    public void evictAll(Class cls, Collection oids) {
        if (_cache.getDelegate() != null)
            _cache.removeAll(JPAFacadeHelper.toOpenJPAObjectIds
                (getMetaData(cls), oids));
    }

    public void evictAll() {
        _cache.clear();
    }
    
    public CacheStatistics getStatistics() {
    	return (_cache == null) ? null : _cache.getStatistics();
    }

    /**
     * Return metadata for the given class, throwing the proper exception
     * if not persistent.
     */
    private ClassMetaData getMetaData(Class cls) {
        try {
            return _repos.getMetaData(cls, null, true);
        } catch (RuntimeException re) {
            throw PersistenceExceptions.toPersistenceException(re);
        }
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
      return _cache.equals (((StoreCacheImpl) other)._cache);
	}

    @SuppressWarnings("unchecked")
    public void evict(Class cls) {
        // Check MetaData throws a consistent exception with evict(Class,
        // Object)
        if(getMetaData(cls) != null) {
            _cache.removeAll(cls, true);
        }
    }
}

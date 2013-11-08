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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCDataGenerator;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.conf.ObjectValue;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.util.ImplHelper;

/**
 * Default data cache manager provides handle to utilities {@linkplain PCDataGenerator}, {@linkplain ClearableScheduler}
 * and {@linkplain CacheDistributionPolicy} for the cache operation. This implementation also determines whether a
 * managed type is eligible to cache.
 * 
 * @author Abe White
 * @author Patrick Linskey
 * @author Pinaki Poddar
 */
public class DataCacheManagerImpl
    implements Closeable, DataCacheManager {

    private OpenJPAConfiguration _conf;
    private DataCache _cache = null;
    private QueryCache _queryCache = null;
    private DataCachePCDataGenerator _pcGenerator = null;
    private ClearableScheduler _scheduler = null;
    private CacheDistributionPolicy _policy = new DefaultCacheDistributionPolicy();
    private Map<ClassMetaData,Boolean> _cacheable = new HashMap<ClassMetaData, Boolean>();
    
    // Properties that are configured via openjpa.DataCache but need to be used here. This is here to support the 1.2
    // way of doing things with openjpa.DataCache(Types=x;y;z,ExcludedTypes=a)
    private Set<String> _includedTypes;
    private Set<String> _excludedTypes;
    
    public void initialize(OpenJPAConfiguration conf, ObjectValue dataCache, ObjectValue queryCache) {
        _conf = conf;        
        _queryCache = (QueryCache) queryCache.instantiate(QueryCache.class, conf);
        if (_queryCache != null)
            _queryCache.initialize(this);
        _cache = (DataCache) dataCache.instantiate(DataCache.class, conf);

        if (_cache == null)
            return;
         
        // create helpers before initializing caches
        if (conf.getDynamicDataStructs())
            _pcGenerator = new DataCachePCDataGenerator(conf);
        _scheduler = new ClearableScheduler(conf);
        
        _policy = conf.getCacheDistributionPolicyInstance();

        _cache.initialize(this);

    }

    public DataCache getSystemDataCache() {
        return getDataCache(null, false);
    }

    public DataCache getDataCache(String name) {
        return getDataCache(name, false);
    }

    /**
     * Returns the named cache. 
     * If the given name is name or the name of the cache plugin then returns the main cache.
     * Otherwise, {@linkplain DataCache#getPartition(String, boolean) delegates} to the main cache
     * to obtain a partition.   
     */
    public DataCache getDataCache(String name, boolean create) {
        if (name == null || (_cache != null && name.equals(_cache.getName())))
            return _cache;
        if (_cache != null)
            return _cache.getPartition(name, create);
        return null;
    }

    public QueryCache getSystemQueryCache() {
        return _queryCache;
    }

    public DataCachePCDataGenerator getPCDataGenerator() {
        return _pcGenerator;
    }

    public ClearableScheduler getClearableScheduler() {
        return _scheduler;
    }

    public void close() {
        ImplHelper.close(_cache);
        ImplHelper.close(_queryCache);
        if (_scheduler != null)
            _scheduler.stop();
    }

    /**
     * Select cache for the given managed instance.
     * If type based verification affirms the type to be cached then the instance based policy 
     * is called to determine the target cache.  
     */
    public DataCache selectCache(OpenJPAStateManager sm) {
        if (sm == null || !isCachable(sm.getMetaData()))
            return null;
        String name = _policy.selectCache(sm, null);
        return name == null ? null : getDataCache(name);
    }
    
    /**
     * Gets the instance-based cache distribution policy, if configured. 
     */
    public CacheDistributionPolicy getDistributionPolicy() {
        return _policy;
    }
    
    /**
     * Affirms if the given type is eligible for cache.
     */
    public boolean isCachable(ClassMetaData meta) {
        Boolean res = _cacheable.get(meta);
        if(res != null){
            return res;
        }

        Boolean isCachable = isCacheableByMode(meta);
        if (isCachable == null) {
           isCachable = isCacheableByType(meta);
        }
        _cacheable.put(meta, isCachable);
        return isCachable;
    }
    
    public void setTypes(Set<String> includedTypes, Set<String> excludedTypes){
        _includedTypes = includedTypes;
        _excludedTypes = excludedTypes;
    }
    
    /**
     * Affirms the given class is eligible to be cached according to the cache mode
     * and the cache enable flag on the given metadata.
     *  
     * @return TRUE or FALSE if  cache mode is configured. null otherwise.
     */
    private Boolean isCacheableByMode(ClassMetaData meta) { 
        String mode = _conf.getDataCacheMode();
        if (DataCacheMode.ALL.toString().equalsIgnoreCase(mode))
            return true;
        if (DataCacheMode.NONE.toString().equalsIgnoreCase(mode))
            return false;
        if (DataCacheMode.ENABLE_SELECTIVE.toString().equalsIgnoreCase(mode))
            return Boolean.TRUE.equals(meta.getCacheEnabled());
        if (DataCacheMode.DISABLE_SELECTIVE.toString().equalsIgnoreCase(mode))
            return !Boolean.FALSE.equals(meta.getCacheEnabled());
        return null;
    }
    
    /**
     * Is the given type cacheable by @DataCache annotation or openjpa.DataCache(Types/ExcludedTypes)
     *  
     * @see ClassMetaData#getDataCacheName()
     */
    private Boolean isCacheableByType(ClassMetaData meta) { 
        if (_includedTypes != null && _includedTypes.size() > 0) {
            return _includedTypes.contains(meta.getDescribedType().getName());
        }
        if (_excludedTypes != null && _excludedTypes.size() > 0) {
            if (_excludedTypes.contains(meta.getDescribedType().getName())) {
                return false;
            } else {
                // Case where Types is not set, and ExcludedTypes only has a sub set of all
                // Entities.
                return true;
            }
        }
        // Check for @DataCache annotations
        return meta.getDataCacheName() != null;
    }

    public void startCaching(String cls) {
        MetaDataRepository mdr = _conf.getMetaDataRepositoryInstance();
        ClassMetaData cmd = mdr.getCachedMetaData(cls);
        _cacheable.put(cmd, Boolean.TRUE);
    }

    public void stopCaching(String cls) {
        MetaDataRepository mdr = _conf.getMetaDataRepositoryInstance();
        ClassMetaData cmd = mdr.getCachedMetaData(cls);
        _cacheable.put(cmd, Boolean.FALSE);
    }

    public Map<String, Boolean> listKnownTypes() {
        Map<String, Boolean> res = new HashMap<String, Boolean>();
        for (Entry<ClassMetaData, Boolean> entry : _cacheable.entrySet()) {
            res.put(entry.getKey().getDescribedTypeString(), entry.getValue());
        }
        return res;
    }
}

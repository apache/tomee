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

package org.apache.openjpa.persistence.cache.jpa;

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.datacache.ConcurrentDataCache;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

/*
 * When shared-cache-mode is set to ALL, all entities will be cached no matter
 * what is the customized DataCache setting. The other DataCache 
 * config settings like CacheSize will be used to config the data cache.
 */
public class TestCacheModeAllDataCacheCustomized extends TestCacheModeAll {
    @Override
    public OpenJPAEntityManagerFactorySPI getEntityManagerFactory() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("openjpa.DataCache",
            "org.apache.openjpa.persistence.cache.jpa.DataCacheTestExtension(CacheSize=5000, "
                + "ExcludedTypes=org.apache.openjpa.persistence.cache.jpa.model.UncacheableEntity)");
        props.put("openjpa.RemoteCommitProvider", "sjvm");
        if (emf == null) {
            emf = createEntityManagerFactory("cache-mode-all", props);
            assertNotNull(emf);
            ConcurrentDataCache dataCache =
                (ConcurrentDataCache) emf.getConfiguration().getDataCacheManagerInstance().getDataCache("default");
            assertNotNull(dataCache);
            assertEquals(5000, dataCache.getCacheSize());
            assertEquals("org.apache.openjpa.persistence.cache.jpa.DataCacheTestExtension(CacheSize=5000, "
                + "ExcludedTypes=org.apache.openjpa.persistence.cache.jpa.model.UncacheableEntity)", emf
                .getConfiguration().getDataCache());
            cache = emf.getCache();
            assertNotNull(cache);
        }
        return emf;
    }
}

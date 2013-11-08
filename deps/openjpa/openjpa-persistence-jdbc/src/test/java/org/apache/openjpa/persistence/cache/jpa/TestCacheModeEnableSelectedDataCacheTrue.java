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

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
/*
 * When ENABLE_SELECTIVE is set for the shared-cache-mode element and an entity is marked as cacheable(true),
 * the entity will be cached even though DataCache ExcludedTypes indicate it should be excluded 
 * since shared-cache-mode take the precedence.
 */
public class TestCacheModeEnableSelectedDataCacheTrue  extends TestCacheModeEnableSelective{

    @Override
    public OpenJPAEntityManagerFactorySPI getEntityManagerFactory() {
    	Map<String,Object> props = new HashMap<String,Object>();
    	//exclude types will be ignored since shared cache mode take precedence
   	 	props.put("openjpa.DataCache", "true(ExcludedTypes=" +
   	 			"org.apache.openjpa.persistence.cache.jpa.model.CacheableEntity)");
   	 	props.put("openjpa.RemoteCommitProvider", "sjvm");
        if (emf == null) {
            emf = createEntityManagerFactory("cache-mode-enable", props);
            assertNotNull(emf);
            assertEquals("true(ExcludedTypes=" +
   	 			"org.apache.openjpa.persistence.cache.jpa.model.CacheableEntity)",
   	 			emf.getConfiguration().getDataCache());
            cache = emf.getCache();
            assertNotNull(cache);
        }
        return emf;
    }

}

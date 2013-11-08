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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cache;

import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.FilteringJDBCListener;

/*
 * When shared-cache-mode is NONE and dataCache is not set, caching will be off.
 */
public class TestCacheModeNone extends AbstractCacheModeTestCase {

    protected static Cache cache = null;
    private static List<String> sql = new ArrayList<String>();
    private static JDBCListener listener;
    
    private static Class<?>[] expectedInCache = {}; 
    private static Class<?>[] expectedNotInCache = persistentTypes;

    @Override
    public OpenJPAEntityManagerFactorySPI getEntityManagerFactory() {
        if (emf == null) {
            emf = createEntityManagerFactory("cache-mode-none",null);
            assertEquals("false", emf.getConfiguration().getDataCache());
            assertNotNull(emf);
            cache = emf.getCache();
            assertNotNull(cache);
        }
        return emf;
    }

    public JDBCListener getListener() {
        if (listener == null) {
            listener = new FilteringJDBCListener(getSql());
        }
        return listener;
    }

    public List<String> getSql() {
        return sql;
    }
    
    @Override
    public boolean getCacheEnabled() {
        return false;
    }

    public void testCacheables() {
        assertCacheables(cache, false);
    }

    public void testUncacheables() {
        assertUncacheables(cache, false);
    }

    public void testUnspecified() {
        assertUnspecified(cache, false);
    }
    
    @Override
    protected Class<?>[] getExpectedInCache() {
        return expectedInCache;
    }

    @Override
    protected Class<?>[] getExpectedNotInCache() {
        return expectedNotInCache;
    }

}

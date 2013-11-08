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

import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public class TestPropertyCacheModeInvalid extends AbstractCacheTestCase {

    
    @Override
    public void setUp() {}
    
    public void testInvalidPropertyValue() {
        boolean exceptionCaught = false;
        try {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            propertyMap.put("javax.persistence.sharedCache.mode", "INVALID");
            emf = createEntityManagerFactory("cache-mode-empty",propertyMap);
        } catch (Throwable e) {
            exceptionCaught = true;
            assertException(e, java.lang.IllegalArgumentException.class);
            String msg = e.getMessage();
            assertTrue(msg.contains("javax.persistence.SharedCacheMode.INVALID"));
        }
        assertTrue(exceptionCaught);
    }

    @Override
    public OpenJPAEntityManagerFactorySPI getEntityManagerFactory() {
        return null;
    }

    @Override
    public JDBCListener getListener() {
        return null;
    }
 
}

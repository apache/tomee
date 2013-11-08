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
package org.apache.openjpa.kernel;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.apache.openjpa.kernel.QueryImpl.Compilation;
import org.apache.openjpa.kernel.jpql.JPQLExpressionBuilder.ParsedJPQL;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.simple.NamedEntity;

import junit.framework.TestCase;


public class TestQueryCompilationCache
    extends TestCase {
    
    public void testDynamicJPQLWithNamedEntity() {
        Map props = new HashMap(System.getProperties());
        props.put("openjpa.MetaDataFactory", "jpa(Types=" 
            + NamedEntity.class.getName() + ")");
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.cast(
                Persistence.createEntityManagerFactory("test", props));

        Map cache = emf.getConfiguration().getQueryCompilationCacheInstance();
        cache.clear();
        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("select o from named o");
        q.compile();
        em.close();

        // make sure that there's an entry in the cache now
        assertEquals(1, cache.size());
        
        // dig into the entry and check its internal state
        Compilation comp = (Compilation) cache.values().iterator().next();
        assertEquals(NamedEntity.class,
            ((ParsedJPQL) comp.storeData).getCandidateType());
        
        emf.close();
    }
}

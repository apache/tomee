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

package org.apache.openjpa.persistence.entitymanager;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.datacache.common.apps.PObject;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestEntityManagerConfiguration extends SingleEMFTestCase {
    public void setUp() throws Exception {
        super.setUp(PObject.class);
    }
    
    public void testEMConfig001() {
        Map propMap = new HashMap();
        EntityManager em = emf.createEntityManager(propMap);
        
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertTrue(eml.getQuerySQLCache());
    }
    
    public void testEMConfig002() {
        Map propMap = new HashMap();
        propMap.put("openjpa.jdbc.QuerySQLCache", "true(EnableStatistics=true)");
        EntityManager em = emf.createEntityManager(propMap);
        
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertTrue(eml.getQuerySQLCache());
    }
    
    public void testEMConfig003() {
        Map propMap = new HashMap();
        propMap.put("openjpa.jdbc.QuerySQLCache", "false");
        EntityManager em = emf.createEntityManager(propMap);
        
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertFalse(eml.getQuerySQLCache());
    }
    
    public void testEMConfig004() {
        Map propMap = new HashMap();
        propMap.put("openjpa.jdbc.QuerySQLCache", "false(EnableStatistics=true)");
        EntityManager em = emf.createEntityManager(propMap);
        
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertFalse(eml.getQuerySQLCache());
    }
    
    public void testEMConfig005() {
        Map propMap = new HashMap();
        propMap.put("openjpa.jdbc.QuerySQLCache", "notabool");
        EntityManager em = emf.createEntityManager(propMap);
        
        EntityManagerImpl eml = (EntityManagerImpl) em;
        assertFalse(eml.getQuerySQLCache());
    }
}

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
package org.apache.openjpa.persistence.lockmgr;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.apache.openjpa.persistence.test.AllowFailure;

/**
 * Test hints using EntityManager interface.
 */
public class TestEmLockTimeout extends SequencedActionsTest {
    
    public void setUp() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            );
        commonSetUp();
        emf.close();
    }

    /*
     * Test setting lock.timeout at the createEntityManagerFactory.
     */
    public void testSetJavaxLockTimeoutAtProviderCreateEmf() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            , "javax.persistence.lock.timeout", "13"
            );
        
        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();

        int lockTmo1 = fPlan.getLockTimeout();
        assertEquals(13, lockTmo1);

        em.close();
        emf.close();
    }

    /*
     * Test setting lock.timeout at the createEntityManagerFactory,
     * with multiple equivalent entries.
     */
    @AllowFailure(message="OPENJPA-??? - Provider.createEntityManagerFactory" +
        " does not suppport multiple equivalent properties.")
    public void testSetLockTimeoutsAtProviderCreateEmf() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            , "openjpa.LockTimeout", 122
            , "javax.persistence.lock.timeout", "133"
            );
        
        EntityManager em = emf.createEntityManager();
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();

        int lockTmo1 = fPlan.getLockTimeout();
        assertEquals(133, lockTmo1);

        em.close();
        emf.close();
    }

    /*
     * Test setting lock.timeout at the em.find(), overriding
     * value set at createEntityManagerFactory and createEm.
     */
    public void testSetJavaxLockTimeoutAtFind() {
        setUp(LockEmployee.class
            , "openjpa.LockManager", "mixed"
            , "javax.persistence.lock.timeout", "13"
            );
        
        EntityManager em = emf.createEntityManager();
        
        Map<String,Object> props2 = new HashMap<String,Object>();
        props2.put("javax.persistence.lock.timeout", 3333);
        em.find(LockEmployee.class, 1, props2);
        
        OpenJPAEntityManager oem = (OpenJPAEntityManager)em.getDelegate();
        JDBCFetchPlan fPlan = (JDBCFetchPlan) oem.getFetchPlan();
        int lockTmo3 = fPlan.getLockTimeout();
        assertEquals(13, lockTmo3);
        
        em.close();
        emf.close();
    }
}

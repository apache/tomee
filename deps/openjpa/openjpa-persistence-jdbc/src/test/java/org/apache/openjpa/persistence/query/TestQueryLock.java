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
package org.apache.openjpa.persistence.query;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.openjpa.persistence.query.common.apps.Entity1;

public class TestQueryLock extends BaseQueryTest {

    public TestQueryLock(String test) {
        super(test);
    }

    public void setUp() {
        deleteAll(Entity1.class);
    }

    public void testJPQLLock() {
        EntityManager em = currentEntityManager();
        Query q = em.createQuery("SELECT o FROM Entity1 o WHERE o.stringField = 'testSimple'");

        try {
            q.setLockMode(LockModeType.NONE);
            assertEquals("Verify NONE after set", LockModeType.NONE, q.getLockMode());
        } catch (Exception e) {
            fail("Do not expected " + e.getClass().getName());
        }

        try {
            q.setLockMode(LockModeType.PESSIMISTIC_READ);
            fail("Expecting TransactionRequiredException thrown");
        } catch (TransactionRequiredException tre) {
            assertEquals("Verify still NONE after set incorrect lock mode", LockModeType.NONE, q.getLockMode());
        } catch (Exception e) {
            fail("Expecting TransactionRequiredException thrown");
        }

        startTx(em);
        try {
            q.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            assertEquals("Verify changed to OPTIMISTIC_FORCE_INCREMENT", LockModeType.OPTIMISTIC_FORCE_INCREMENT, q
                    .getLockMode());
        } catch (Exception e) {
            fail("Do not expected " + e.getClass().getName());
        }
        endTx(em);
        endEm(em);
    }

    public void testCriteriaLock() {
        EntityManager em = currentEntityManager();
        CriteriaBuilder cb = getEmf().getCriteriaBuilder();
        CriteriaQuery<Entity1> cq = cb.createQuery(Entity1.class);
        Root<Entity1> customer = cq.from(Entity1.class);
        Query q = em.createQuery(cq);

        try {            
            q.setLockMode(LockModeType.NONE);
            assertEquals("Verify NONE after set", LockModeType.NONE, q.getLockMode());
        } catch (Exception e) {
            fail("Do not expected " + e.getClass().getName());
        }

        try {
            q.setLockMode(LockModeType.PESSIMISTIC_READ);
            fail("Expecting TransactionRequiredException thrown");
        } catch (TransactionRequiredException tre) {
            assertEquals("Verify still NONE after set incorrect lock mode", LockModeType.NONE, q.getLockMode());
        } catch (Exception e) {
            fail("Expecting TransactionRequiredException thrown");
        }

        startTx(em);
        try {
            q.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            assertEquals("Verify changed to OPTIMISTIC_FORCE_INCREMENT", LockModeType.OPTIMISTIC_FORCE_INCREMENT, q
                    .getLockMode());
        } catch (Exception e) {
            fail("Do not expected " + e.getClass().getName());
        }
        endTx(em);
        endEm(em);
    }

    public void testNativeLock() {
        EntityManager em = currentEntityManager();
        Query q = em.createNativeQuery("SELECT * FROM Entity1");

        try {
            q.setLockMode(LockModeType.NONE);
            fail("Expecting IllegalStateException thrown");
        } catch (IllegalStateException ise) {
        } catch (Exception e) {
            fail("Expecting IllegalStateException thrown");
        }

        try {
            q.setLockMode(LockModeType.PESSIMISTIC_READ);
            fail("Expecting IllegalStateException thrown");
        } catch (IllegalStateException ise) {
        } catch (Exception e) {
            fail("Expecting IllegalStateException thrown");
        }

        startTx(em);
        try {
            q.setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            fail("Expecting IllegalStateException thrown");
        } catch (IllegalStateException ise) {
        } catch (Exception e) {
            fail("Expecting IllegalStateException thrown");
        }
        endTx(em);
        endEm(em);
    }
}

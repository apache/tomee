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
package org.apache.openjpa.persistence.nullity;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public abstract class AbstractNullityTestCase extends SingleEMFTestCase {
    protected static boolean NEW = true;
    
    /**
     * Asserts that the given instance can not be committed.
     */
    void assertCommitFails(Object pc, boolean isNew,
        Class<? extends Exception> expected) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        if (isNew) {
            em.persist(pc);
        }
        else {
            em.merge(pc);
        }
        try {
            em.getTransaction().commit();
            fail();
        } catch (Exception e) {
            if (!expected.isAssignableFrom(e.getClass())) {
                fail("Expected " + expected.getName() + " receieved " + e);
            } 
        }
    }
    
    void assertCommitSucceeds(Object pc, boolean isNew) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        if (isNew)
            em.persist(pc);
        else 
            em.merge(pc);
        
        em.getTransaction().commit();
    }
}

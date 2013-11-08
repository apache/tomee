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
package org.apache.openjpa.persistence.generationtype;

import javax.persistence.*;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests OpenJPA's support for the GeneratedValue annotation
 * applied to a non-ID field in interaction with the PostPersist callback.  
 * 
 * @author David Ezzio
 */
public class TestEntityWithGeneratedValueAndPostPersist 
        extends SingleEMFTestCase implements ValueCache {

    private int cache;
    
    public void setUp() { 
        setUp(EntityWithGeneratedValueAndPostPersist.class, CLEAR_TABLES);
        cache = 0;
    }

    // satisfy the ValueCache interface
    public void setValue(int val) {
        cache = val;
    }
    
    public void testValueCapturedInPostPersistAfterCommit() {
        
        // get EM and start tx
        EntityManager em = getEM();
        em.getTransaction().begin();

        // create a new entity
        EntityWithGeneratedValueAndPostPersist pc = 
                new EntityWithGeneratedValueAndPostPersist(1);
        pc.setName("TestEntityWithGeneratedValueAndPostPersist-commit");
        pc.setCache(this);

        // persist and commit
        em.persist(pc);
        em.getTransaction().commit();
        
        // check the value captured by the PostPersist callback
        assertEquals("Entity's current value does not match value captured " +
        		"in postPersist", pc.getBingo(), cache);
        closeEM(em);
    }
    
    public void testValueCapturedInPostPersistAfterFlush() {
        
        // get EM and start tx
        EntityManager em = getEM();
        em.getTransaction().begin();

        // create a new Thingamabob
        EntityWithGeneratedValueAndPostPersist pc = 
                new EntityWithGeneratedValueAndPostPersist(1);
        pc.setName("TestEntityWithGeneratedValueAndPostPersist-flush");
        pc.setCache(this);

        // persist and flush
        em.persist(pc);
        em.flush();

        // check the value captured by the PostPersist callback
        assertEquals("Entity's current value does not match value captured " +
        		"in postPersist", pc.getBingo(), cache);
        
        // commit
        em.getTransaction().commit();
        
        // check the value captured by the PostPersist callback
        assertEquals("Entity's current value does not match value captured " +
        		"in postPersist", pc.getBingo(), cache);
        closeEM(em);
    }
    
    private EntityManager getEM() {
        return emf.createEntityManager();
    }
}

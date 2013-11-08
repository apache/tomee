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
package org.apache.openjpa.persistence.callbacks;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests when PostPersist is invoked can be configured.
 *  
 * @author Pinaki Poddar
 *
 */
public class TestPostPersistCallback extends SingleEMFTestCase {
    
    public void testPostPersistCalledAfterFlush() {
        super.setUp(CLEAR_TABLES, PostPersistEntity.class);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PostPersistEntity pc = new PostPersistEntity();
        
        em.persist(pc);
        
        // nor is postPersist() callback invoked
        assertEquals(0, pc.postPersistCallbackCount);
        assertEquals(0, pc.idOnCallback);
        
        em.flush();
        
        // postPersist() callback invoked
        assertFalse(pc.getId() == 0);
        assertEquals(1, pc.postPersistCallbackCount);
        assertEquals(pc.getId(), pc.idOnCallback);
        
        em.getTransaction().commit();
        
        assertFalse(pc.getId() == 0);
        assertEquals(1, pc.postPersistCallbackCount);
        assertEquals(pc.getId(), pc.idOnCallback);
        em.close();
    }
    
    public void testPostPersistCalledAfterCommit() {
        super.setUp(CLEAR_TABLES, PostPersistEntity.class);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PostPersistEntity pc = new PostPersistEntity();
        
        em.persist(pc);
        
        assertEquals(0, pc.postPersistCallbackCount);
        assertEquals(0, pc.idOnCallback);
        
        em.getTransaction().commit();
        
        assertFalse(pc.getId() == 0);
        assertEquals(1, pc.postPersistCallbackCount);
        assertEquals(pc.getId(), pc.idOnCallback);
        em.close();
    }
    
    public void testPostPersistCalledAfterPersist() {
        super.setUp(CLEAR_TABLES, PostPersistEntity.class, 
            "openjpa.Callbacks", "PostPersistCallbackImmediate=true");
        assertTrue(emf.getConfiguration().getCallbackOptionsInstance()
            .getPostPersistCallbackImmediate());
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PostPersistEntity pc = new PostPersistEntity();
        
        em.persist(pc);
        
        assertEquals(1, pc.postPersistCallbackCount);
        assertEquals(pc.getId(), pc.idOnCallback);
        
        em.getTransaction().commit();
        
        assertEquals(1, pc.postPersistCallbackCount);
        assertEquals(pc.getId(), pc.idOnCallback);
        em.close();
    }
}

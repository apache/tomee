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
package org.apache.openjpa.persistence.event;

import javax.persistence.EntityManager;

import org.apache.openjpa.jta.ContainerTest;

/**
 * Tests PostRemove callback within a pseudo-container environment.
 * 
 * According to JPA 2.0 Specification Section 3.5.2 (edited for PostRemove only and readability) 
 * 
 * "a) The PostRemove callback method is invoked for an entity after the entity has been removed. 
 * 
 *  b) This callback will also be invoked on all entities to which these operations are cascaded. 
 *  
 *  c) The PostRemove method will be invoked after the database delete operation. The database operation 
 *     may occur directly after the remove operation have been invoked or they may occur directly after 
 *     a flush operation has occurred (which may be at the end of the transaction)." 
 *  <br>
 *  The test runs within a test harness that emulates application managed transaction semantics
 *  of a container. 
 * 
 * @author Pinaki Poddar
 *
 */
public class TestPostRemove extends ContainerTest {
    @Override
    public void setUp() throws Exception {
        super.setUp(PostRemoveCallbackEntity.class);
    }
    
    @Override
    public String getPersistenceUnitName() {
        return "post-remove";
    }
    
    public void testPostRemoveInvokedOnlyAfterDatabaseDeleteWithLogicalFlush() {
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        PostRemoveCallbackEntity pc = new PostRemoveCallbackEntity();
        em.persist(pc);
        em.flush();
        em.remove(pc);
        commit();
        assertTrue("PostRemove not called after commit", isPostRemovedInvoked(pc) 
                && pc.getPostRemoveTime() <= System.nanoTime());
        em.close();
    }
    
    public void testPostRemoveInvokedAfterDatabaseDeleteWithoutFlush() {
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        PostRemoveCallbackEntity pc = new PostRemoveCallbackEntity();
        em.persist(pc);
        em.remove(pc);
        assertFalse("PostRemove called before commit", isPostRemovedInvoked(pc));
        commit();
        assertTrue("PostRemove not called after commit", pc.getPostRemoveTime() <= System.nanoTime());
        em.close();
    }
    
    public void testPostRemoveNotInvokedAfterRollback() {
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        PostRemoveCallbackEntity pc = new PostRemoveCallbackEntity();
        em.persist(pc);
        em.remove(pc);
        assertFalse("PostRemove called before rollback", isPostRemovedInvoked(pc));
        rollback();
        assertTrue("PostRemove called after rollback", pc.getPostRemoveTime() <= System.nanoTime());
        em.close();
    }
    
    public void testPostRemoveNotInvokedAfterRollbackWithIntermediateFlush() {
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        PostRemoveCallbackEntity pc = new PostRemoveCallbackEntity();
        em.persist(pc);
        em.flush();
        assertFalse("PostRemove called after flush", isPostRemovedInvoked(pc));
        em.remove(pc);
        assertFalse("PostRemove called before rollback", isPostRemovedInvoked(pc));
        rollback();
        assertTrue("PostRemove called after rollback", pc.getPostRemoveTime() <= System.nanoTime());
        em.close();
    }
    
    public void testPostRemoveInvokedOnFlushThatIssuesDatabaseDelete() {
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        PostRemoveCallbackEntity pc = new PostRemoveCallbackEntity();
        em.persist(pc);
        commit();
        em.close();
        
        em = emf.createEntityManager();
        em.joinTransaction();
        pc = em.find(PostRemoveCallbackEntity.class, pc.getId());
        assertNotNull(pc);
        em.remove(pc);
        assertFalse("PostRemove called after logical remove", isPostRemovedInvoked(pc));
        em.flush();
        assertTrue("PostRemove not called after delete flush", isPostRemovedInvoked(pc));
        commit();
        assertTrue("PostRemove not called after commit", pc.getPostRemoveTime() <= System.nanoTime());
        em.close();
    }


    public void testPostRemoveNotInvokedAfterDatabaseInsert() {
        EntityManager em = emf.createEntityManager();
        em.joinTransaction();
        PostRemoveCallbackEntity pc = new PostRemoveCallbackEntity();
        em.persist(pc);
        assertFalse("PostRemove called before commit", isPostRemovedInvoked(pc));
        commit();
        assertFalse("PostRemove called after commit", isPostRemovedInvoked(pc));
        em.close();
    }
    
    boolean isPostRemovedInvoked(PostRemoveCallbackEntity pc) {
        return pc.getPostRemoveTime() != 0;
    }
}

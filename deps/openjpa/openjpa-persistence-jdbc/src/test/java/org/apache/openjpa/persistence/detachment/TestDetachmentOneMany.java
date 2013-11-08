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
package org.apache.openjpa.persistence.detachment;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.AutoDetachType;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests detachment for bidirectional one-many relationship
 *
 * @author David Ezzio
 */
public class TestDetachmentOneMany
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(DetachmentOneManyParent.class, DetachmentOneManyChild.class);
    }

    public void testDetachment() {
        long id = createParentAndChildren();
    
        EntityManager em = emf.createEntityManager();
        OpenJPAPersistence.cast(em).setAutoDetach(
            AutoDetachType.NON_TRANSACTIONAL_READ);
        DetachmentOneManyParent parent = em.find(DetachmentOneManyParent.class,
            id);
        assertNotNull(parent);
        assertFalse("The parent was not detached", em.contains(parent));
        em.close();
    }

    public void testFetchWithDetach() {
        long id = createParentAndChildren();
     
        EntityManager em = emf.createEntityManager();
        OpenJPAPersistence.cast(em).setAutoDetach(
            AutoDetachType.NON_TRANSACTIONAL_READ);
        DetachmentOneManyParent parent = em.find(DetachmentOneManyParent.class,
            id);
        assertNotNull(parent);
        assertEquals("parent", parent.getName());
        assertEquals(2, parent.getChildren().size());
        DetachmentOneManyChild child0 = parent.getChildren().get(0);
        DetachmentOneManyChild child1 = parent.getChildren().get(1);
        assertNotNull("Did not find expected first child", child0);
        assertNotNull("Did not find expected second child", child1);
        assertEquals("child0", child0.getName());
        assertFalse("The first child was not detached", em.contains(child0));
        assertEquals("child1", child1.getName());
        assertFalse("The second child was not detached", em.contains(child1));
        em.close();
    }
    
    public void testFetchWithDetachForToOneRelationship() {
        long id = createParentAndChildren();
        
        EntityManager em = emf.createEntityManager();
        OpenJPAPersistence.cast(em).setAutoDetach(
            AutoDetachType.NON_TRANSACTIONAL_READ);
        DetachmentOneManyParent parent = em.find(DetachmentOneManyParent.class,
            id);
        assertNotNull(parent);
        assertEquals(2, parent.getChildren().size());
        assertEquals("ToOne relationship was not eagerly fetched", 
              parent, parent.getChildren().get(0).getParent());
        em.close();
    }
    
    private long createParentAndChildren() {
        DetachmentOneManyParent parent = new DetachmentOneManyParent();
        parent.setName("parent");
        for (int i = 0; i < 2; i++) {
            DetachmentOneManyChild child = new DetachmentOneManyChild();
            child.setName("child" + i);
            parent.addChild(child);
        }
      
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(parent);
        em.getTransaction().commit();
        long id = parent.getId();
        assertNotNull(parent.getChildren());
        assertEquals(2, parent.getChildren().size());
        assertTrue("The parent is not managed", em.contains(parent));
        DetachmentOneManyChild child0 = parent.getChildren().get(0);
        DetachmentOneManyChild child1 = parent.getChildren().get(1);
        assertEquals("child0", child0.getName());
        assertEquals("child1", child1.getName());
        assertEquals("The first child has no relationship to the parent", 
            parent, child0.getParent());
        assertEquals("The second child has no relationship to the parent", 
            parent, child1.getParent());
        em.close();
        return id;
    }

    public static void main(String[] args) {
        TestRunner.run(TestDetachmentOneMany.class);
    }
}


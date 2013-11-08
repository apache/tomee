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
package org.apache.openjpa.persistence.datacache;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Defined ordering is only applied when the collection is loaded from the
 * datastore. It is not maintained by Openjpa as you modify the collection in
 * memory. Openjpa invalid data cache in case the persistence operation may
 * result cache in wrong order. This test suite tests various cases for the
 * above problem.
 */
public class TestOrderbyInDataCache extends SingleEMFTestCase {
	private long pid;
    public void setUp() {
        setUp("openjpa.DataCache", "true", 
            "openjpa.RemoteCommitProvider", "sjvm", 
            OrderedOneManyParent.class,
            OrderedOneManyChild.class, CLEAR_TABLES);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        OrderedOneManyParent prt = new OrderedOneManyParent();
        prt.setName("Maria");
        em.persist(prt);
        //insert child in reverse order
        for (int i = 4; i > 1; i--) {
        	OrderedOneManyChild child = new OrderedOneManyChild();
            child.setId(i);
            child.setName("child" + i);
            child.setParent(prt);
            prt.getChildren().add(child);
            em.persist(child);
        }        
        em.getTransaction().commit();
        pid = prt.getId();
        em.close();
    }

    /**
     * Test if child list is in order after new child list is added in setup().
     *
     */
    public void testGetChildList(){
    	EntityManager em = emf.createEntityManager();
    	OrderedOneManyParent prt = em.find(OrderedOneManyParent.class, pid);
        assertEquals(3, prt.getChildren().size());
        //the order should be "child2", "child3", "child4"
        for (int i = 1; i < 4; i++) {
            assertEquals("child" + (i + 1), prt.getChildren().
                get(i-1).getName());
        }   
        em.close();
    }
    
    public void testInsertChild() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        OrderedOneManyParent prt = em.find(OrderedOneManyParent.class, pid);
        OrderedOneManyChild child = new OrderedOneManyChild();
        child.setId(1);
        child.setName("child1");
        child.setParent(prt);
        prt.getChildren().add(child);
        em.persist(child);
        em.getTransaction().commit();
        em.close();
        
        //obtain object in new persistence context
        em = emf.createEntityManager();
        prt = em.find(OrderedOneManyParent.class, pid);
        assertEquals(4, prt.getChildren().size());
        //the order should be "child1", "child2", "child3", "child4"
        for (int i = 1; i < 5; i++) {
            assertEquals("child" + i, prt.getChildren().
                get(i-1).getName());
        }          
        em.close();
    }
    
    public void testUpdateChildName() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        OrderedOneManyChild child = em.find(OrderedOneManyChild.class, 4);
        child.setName("child1");
        em.persist(child);
        em.getTransaction().commit();
        OrderedOneManyParent prt = em.find(OrderedOneManyParent.class, pid);
        assertEquals(3, prt.getChildren().size());
        //the order should be "child1", "child2", "child3"
        for (int i = 1; i < 4; i++) {
            assertEquals("child" + i, prt.getChildren().
                get(i-1).getName());
        }          
        em.close();    	
    }
}

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

package org.apache.openjpa.persistence.merge;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestMerge extends SQLListenerTestCase {
    public void setUp() {
        setUp(CLEAR_TABLES, Parent.class,
            Child.class, GrandChild.class);
        assertNotNull(emf);
        populate();
    }

    public void testMergeManagedParent() {
        System.out.println("Running testMergeManagedParent()...");
        
        System.out.println();
        System.out.println("Exercising em #1...");
        EntityManager em = emf.createEntityManager();
        ParentPK pk = new ParentPK(1);
        pk.setKey1("K1");
        Parent parent = em.find(Parent.class, pk);
            
        System.out.println();
        System.out.println("Exercising em #1...");
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Parent mergedParent = em2.merge(parent);
        assertNotNull(mergedParent);
        em2.getTransaction().commit();
        em.close();
        em2.close();
    }
    
    private void populate() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Parent p = new Parent();
        p.setKey1("K1");
        p.setKey2(1);
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }
}

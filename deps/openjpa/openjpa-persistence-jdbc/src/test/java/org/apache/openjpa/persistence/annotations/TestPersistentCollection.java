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
package org.apache.openjpa.persistence.annotations;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for PersistentCollection Annotation
 * 
 * @author Albert Lee
 */
public class TestPersistentCollection extends SingleEMFTestCase {

    public void setUp() {
        setUp(PColl_EntityA.class, PColl_EmbedB.class, PColl_EntityC.class,
                PColl_EntityA1.class, PColl_EntityB.class, PColl_EntityStringEager.class,
                PColl_EntityStringLazy.class, CLEAR_TABLES);
    }

    @SuppressWarnings("unchecked")
    public void testPersistentCollectionOfEmbeddables() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PColl_EntityC c = new PColl_EntityC();
            c.setId(101);

            PColl_EmbedB b01 = new PColl_EmbedB();
            b01.setName("b01");
            b01.setM2oC(c);

            PColl_EntityA a = new PColl_EntityA();
            a.setId(1);
            a.getEmbedCollection().add(b01);
            em.persist(a);
            em.getTransaction().commit();
            em.close();
            em = null;

            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT o FROM PColl_EntityA o"); 
            List<PColl_EntityA> oList = (List<PColl_EntityA>) q.getResultList();
            PColl_EntityA d1 = oList.get(0);
            
            Set<PColl_EmbedB> b1s = d1.getEmbedCollection();

            PColl_EmbedB b1 = b1s.iterator().next();
            PColl_EntityC c1 = b1.getM2oC();
            assertEquals("b01", b1.getName());
            assertEquals(101, c1.getId());
            assertEquals(1, d1.getId());
            em.close();
            em = null;
        } catch (Throwable t) {
            fail(t.getMessage());
        } finally {
            if (em != null)
                em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public void testPersistentCollectionOfEntities() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PColl_EntityC c = new PColl_EntityC();
            c.setId(101);

            PColl_EntityB b01 = new PColl_EntityB();
            b01.setName("b01");
            b01.setM2oC(c);

            PColl_EntityA1 a = new PColl_EntityA1();
            a.setId(1);
            a.getEmbedCollection().add(b01);
            em.persist(a);
            em.getTransaction().commit();
            em.close();
            em = null;

            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT o FROM PColl_EntityA1 o"); 
            List<PColl_EntityA1> oList =
                (List<PColl_EntityA1>) q.getResultList();
            PColl_EntityA1 a1 = oList.get(0);
            
            Set<PColl_EntityB> b1s = a1.getEmbedCollection();

            PColl_EntityB b1 = b1s.iterator().next();
            PColl_EntityC c1 = b1.getM2oC();
            assertEquals("b01", b1.getName());
            assertEquals(101, c1.getId());
            assertEquals(1, a1.getId());
            em.close();
            em = null;
        } catch (Throwable t) {
            fail(t.getMessage());
        } finally {
            if (em != null)
                em.close();
        }
    }    

    public void testPersistentCollectionStringsLazy() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PColl_EntityStringLazy a = new PColl_EntityStringLazy();
            a.setId(1);
            a.getCollectionOfStrings().add("one");
            em.persist(a);
            em.getTransaction().commit();
            em.close();
            em = null;
            
            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT o FROM PColl_EntityStringLazy o"); 
            PColl_EntityStringLazy a1 = (PColl_EntityStringLazy)q.getSingleResult();
            
            assertEquals(1, a1.getCollectionOfStrings().size());
            assertEquals("one", a1.getCollectionOfStrings().toArray()[0]);
            assertEquals(1, a1.getId());
            em.close();
            em = null;
        } catch (Throwable t) {
            fail(t.getMessage());
        } finally {
            if (em != null)
                em.close();
        }
    }    

    public void testPersistentCollectionStringsEager() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PColl_EntityStringEager a = new PColl_EntityStringEager();
            a.setId(1);
            a.getCollectionOfStrings().add("one");
            em.persist(a);
            em.getTransaction().commit();
            em.close();
            em = null;
            
            em = emf.createEntityManager();
            Query q = em.createQuery("SELECT o FROM PColl_EntityStringEager o"); 
            PColl_EntityStringEager a1 = (PColl_EntityStringEager)q.getSingleResult();
            
            assertEquals(1, a1.getCollectionOfStrings().size());
            assertEquals("one", a1.getCollectionOfStrings().toArray()[0]);
            assertEquals(1, a1.getId());
            em.close();
            em = null;
        } catch (Throwable t) {
            fail(t.getMessage());
        } finally {
            if (em != null)
                em.close();
        }
    }    
}

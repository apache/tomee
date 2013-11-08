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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that we can exclude subclass instances from query results.
 *
 * @author Abe White
 */
public class TestQueryExcludingSubclasses
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(ManyOneEntity.class, ManyOneEntitySub.class);

        ManyOneEntity e1 = new ManyOneEntity();
        e1.setName("e1"); 
        ManyOneEntity e2 = new ManyOneEntity();
        e2.setName("e2"); 
        ManyOneEntity invalid = new ManyOneEntity();
        invalid.setName("invalid"); 
        ManyOneEntitySub esub1 = new ManyOneEntitySub();
        esub1.setName("esub1"); 
        ManyOneEntitySub esub2 = new ManyOneEntitySub();
        esub2.setName("esub2"); 
        ManyOneEntitySub invalidsub = new ManyOneEntitySub();
        invalidsub.setName("invalidsub"); 

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e1);
        em.persist(e2);
        em.persist(invalid);
        em.persist(esub1);
        em.persist(esub2);
        em.persist(invalidsub);
        em.getTransaction().commit();
        em.close();
    }

    public void testQuery() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select e from ManyOneEntity e "
            + "where e.name like 'e%' order by e.name asc");
        List<ManyOneEntity> res = (List<ManyOneEntity>) q.getResultList();
        assertEquals(4, res.size());
        for (int i = 0; i < 2; i++) {
            assertEquals(ManyOneEntity.class, res.get(i).getClass());
            assertEquals("e" + (i + 1), res.get(i).getName());
        }
        for (int i = 0; i < 2; i++) {
            assertEquals(ManyOneEntitySub.class, res.get(i + 2).getClass());
            assertEquals("esub" + (i + 1), res.get(i + 2).getName());
        }
        em.close();
    }

    public void testQueryExcludingSubclasses() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select e from ManyOneEntity e "
            + "where e.name like 'e%' order by e.name asc");
        ((OpenJPAQuery) q).setSubclasses(false);
        List<ManyOneEntity> res = (List<ManyOneEntity>) q.getResultList();
        assertEquals(2, res.size());
        for (int i = 0; i < res.size(); i++) {
            assertEquals(ManyOneEntity.class, res.get(i).getClass());
            assertEquals("e" + (i + 1), res.get(i).getName());
        }
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestQueryExcludingSubclasses.class);
    }
}


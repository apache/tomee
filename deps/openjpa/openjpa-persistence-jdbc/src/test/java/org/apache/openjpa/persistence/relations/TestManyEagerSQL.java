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
package org.apache.openjpa.persistence.relations;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.textui.TestRunner;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;


public class TestManyEagerSQL
    extends SQLListenerTestCase {

    public void setUp() {
        setUp(DROP_TABLES,
            OneManyEagerParent.class, OneManyEagerChild.class,
            OneManyLazyChild.class, OneOneParent.class, 
            OneOneChild.class);
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        for (int j = 0; j < 2; j++) {
            OneManyEagerParent parent = new OneManyEagerParent();
            parent.setName("parent"+j);
            for (int i = 0; i < 2; i++) {
                OneManyEagerChild child = new OneManyEagerChild();
                child.setName("eagerchild" + i);
                parent.addEagerChild(child);
                em.persist(child);
                OneManyLazyChild lazychild = new OneManyLazyChild();
                lazychild.setName("lazychild" + i);
                parent.addLazyChild(lazychild);
                em.persist(lazychild);
            }
            em.persist(parent);
        }
        
        for (int i = 0; i < 3; i++) {
        	OneOneParent parent = new OneOneParent();
        	parent.setName("parent" + i);
        	OneOneChild child = new OneOneChild();
        	child.setName("child" + i);
        	parent.setChild(child);
        	child.setParent(parent);
        	em.persist(child);
        	em.persist(parent);
        }
        
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    public void testManyToOneEagerQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        String query = "select c FROM OneManyEagerChild c";
        Query q = em.createQuery(query);
        List list = q.getResultList();
        assertEquals(4, list.size());
        
        // Expected SQLs:
        //   SELECT t0.id, t0.optLock, t0.name, t1.id, t1.optLock, t1.name
        //       FROM OneManyEagerChild t0
        //       LEFT OUTER JOIN OneManyEagerParent t1 ON t0.PARENT_ID = t1.id
        //   SELECT t0.id, t0.optLock, t0.name FROM OneManyEagerChild t0
        //       WHERE t0.PARENT_ID = ? ORDER BY t0.name ASC [params=(long) 2]
        //   SELECT t0.id, t0.optLock, t0.name, t0.PARENT_ID
        //       FROM OneManyLazyChild t0 WHERE t0.PARENT_ID = ?
        //       ORDER BY t0.name ASC [params=(long) 2]
        //   SELECT t0.id, t0.optLock, t0.name FROM OneManyEagerChild t0
        //       WHERE t0.PARENT_ID = ? ORDER BY t0.name ASC [params=(long) 1]
        //   SELECT t0.id, t0.optLock, t0.name, t0.PARENT_ID 
        //       FROM OneManyLazyChild t0 WHERE t0.PARENT_ID = ?
        //       ORDER BY t0.name ASC [params=(long) 1]

        assertEquals(5, sql.size());

        sql.clear();

        for (int i = 0; i < list.size(); i++) {
            OneManyEagerChild child = (OneManyEagerChild) list.get(i);
            assertEquals(2, child.getParent().getLazyChildren().size());
            assertEquals(2, child.getParent().getEagerChildren().size());
        }
        assertEquals(0, sql.size());

        em.close();
    }

    public void testManyToOneLazyQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        String query = "select c FROM OneManyLazyChild c";
        Query q = em.createQuery(query);
        List list = q.getResultList();
        assertEquals(4, list.size());

        // Expected SQL:
        // SELECT t0.id, t0.optLock, t0.name, t0.PARENT_ID
        //     FROM OneManyLazyChild t0
        assertEquals(1, sql.size());

        sql.clear();

        for (int i = 0; i < list.size(); i++) {
            OneManyLazyChild child = (OneManyLazyChild) list.get(i);
            assertEquals(2, child.getParent().getLazyChildren().size());
            assertEquals(2, child.getParent().getEagerChildren().size());
        }

        // Expected SQLs: (fired on child.getParent())
        //  SELECT t0.optLock, t0.name, t1.PARENT_ID, t1.id, t1.optLock, t1.name
        //      FROM OneManyEagerParent t0
        //      LEFT OUTER JOIN OneManyEagerChild t1 ON t0.id = t1.PARENT_ID
        //      WHERE t0.id = ? 
        //      ORDER BY t1.PARENT_ID ASC, t1.name ASC [params=(long) 252]
        //  SELECT t0.id, t0.optLock, t0.name, t0.PARENT_ID
        //      FROM OneManyLazyChild t0 WHERE t0.PARENT_ID = ?
        //      ORDER BY t0.name ASC [params=(long) 252]
        //  SELECT t0.optLock, t0.name, t1.PARENT_ID, t1.id, t1.optLock, t1.name
        //      FROM OneManyEagerParent t0
        //      LEFT OUTER JOIN OneManyEagerChild t1 ON t0.id = t1.PARENT_ID
        //      WHERE t0.id = ? 
        //      ORDER BY t1.PARENT_ID ASC, t1.name ASC [params=(long) 251]
        //  SELECT t0.id, t0.optLock, t0.name, t0.PARENT_ID
        //      FROM OneManyLazyChild t0 WHERE t0.PARENT_ID = ?
        //      ORDER BY t0.name ASC [params=(long) 251]

        assertEquals(4, sql.size());

        em.close();
    }

    public void testOneToManyEagerQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        String query = "select p FROM OneManyEagerParent p";
        Query q = em.createQuery(query);
        List list = q.getResultList();
        assertEquals(2, list.size());

        // Expected SQLs:
        //   SELECT t0.id, t0.optLock, t0.name FROM OneManyEagerParent t0
        //   SELECT t0.id, t1.id, t1.optLock, t1.name FROM OneManyEagerParent t0
        //       INNER JOIN OneManyEagerChild t1 ON t0.id = t1.PARENT_ID
        //       ORDER BY t0.id ASC, t1.name ASC
        //   SELECT t0.id, t1.id, t1.optLock, t1.name, t1.PARENT_ID
        //       FROM OneManyEagerParent t0
        //       INNER JOIN OneManyLazyChild t1 ON t0.id = t1.PARENT_ID
        //       ORDER BY t0.id ASC, t1.name ASC

        assertEquals(3, sql.size());

        sql.clear();

        for (int i = 0; i < list.size(); i++) {
            OneManyEagerParent p = (OneManyEagerParent) list.get(i);
            long id = p.getId();
            assertEquals(2, p.getEagerChildren().size());
            assertEquals(p, p.getEagerChildren().get(0).getParent());
            assertEquals(p, p.getEagerChildren().get(1).getParent());
            assertEquals(id, p.getEagerChildren().get(0).getParent().getId());
            assertEquals(id, p.getEagerChildren().get(1).getParent().getId());
            assertEquals("eagerchild0", p.getEagerChildren().get(0).getName());
            assertEquals("eagerchild1", p.getEagerChildren().get(1).getName());
            assertEquals(2, p.getLazyChildren().size());
            assertEquals(p, p.getLazyChildren().get(0).getParent());
            assertEquals(p, p.getLazyChildren().get(1).getParent());
            assertEquals(id, p.getLazyChildren().get(0).getParent().getId());
            assertEquals(id, p.getLazyChildren().get(1).getParent().getId());
            assertEquals("lazychild0", p.getLazyChildren().get(0).getName());
            assertEquals("lazychild1", p.getLazyChildren().get(1).getName());
        }

        assertEquals(0, sql.size());
        em.close();
    }

    public void testOneToOneParentQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        String query = "select p FROM OneOneParent p";
        Query q = em.createQuery(query);
        List list = q.getResultList();
        assertEquals(3, list.size());

        // Expected SQLs:
        //   SELECT t0.id, t0.optLock, t1.id, t1.optLock, t1.name, t0.name
        //   FROM OneOneParent t0 
        //   LEFT OUTER JOIN OneOneChild t1 ON t0.id = t1.PARENT_ID

        assertEquals(1, sql.size());

        sql.clear();

        for (int i = 0; i < list.size(); i++) {
            OneOneParent p = (OneOneParent) list.get(i);
            assertEquals(p, p.getChild().getParent());
        }

        assertEquals(0, sql.size());
        em.close();
    }

    public void testOneToOneChildQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        String query = "select c FROM OneOneChild c";
        Query q = em.createQuery(query);
        List list = q.getResultList();
        assertEquals(3, list.size());

        // Expected SQLs:
        //   SELECT t0.id, t0.optLock, t1.id, t1.optLock, t1.name, t0.name 
        //   FROM OneOneParent t0 
        //   LEFT OUTER JOIN OneOneChild t1 ON t0.id = t1.PARENT_ID

        assertEquals(1, sql.size());

        sql.clear();

        for (int i = 0; i < list.size(); i++) {
            OneOneChild c = (OneOneChild) list.get(i);
            assertEquals(c, c.getParent().getChild());
        }

        assertEquals(0, sql.size());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestManyEagerSQL.class);
    }
}


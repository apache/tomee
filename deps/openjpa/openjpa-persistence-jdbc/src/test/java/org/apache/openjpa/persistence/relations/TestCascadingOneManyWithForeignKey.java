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

import junit.textui.TestRunner;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests a cascading one-many backed by a foreign key.
 *
 * @author Abe White
 */
public class TestCascadingOneManyWithForeignKey
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(CascadingOneManyParent.class, CascadingOneManyChild.class);
    }

    public void testPersist() {
        CascadingOneManyParent parent = new CascadingOneManyParent();
        parent.setName("parent");
        for (int i = 0; i < 2; i++) {
            CascadingOneManyChild child = new CascadingOneManyChild();
            child.setName("child" + i);
            parent.addChild(child);
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(parent);
        em.getTransaction().commit();
        long id = parent.getId();
        assertEquals(2, parent.getChildren().size());
        assertEquals("child0", parent.getChildren().get(0).getName());
        assertEquals("child1", parent.getChildren().get(1).getName());
        em.close();

        em = emf.createEntityManager();
        parent = em.find(CascadingOneManyParent.class, id);
        assertNotNull(parent);
        assertEquals("parent", parent.getName());
        assertEquals(2, parent.getChildren().size());
        assertEquals("child0", parent.getChildren().get(0).getName());
        assertEquals("child1", parent.getChildren().get(1).getName());
        em.close();
    }

    public void testDelete() {
        CascadingOneManyParent parent = new CascadingOneManyParent();
        parent.setName("parent");
        for (int i = 0; i < 2; i++) {
            CascadingOneManyChild child = new CascadingOneManyChild();
            child.setName("child" + i);
            parent.addChild(child);
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(parent);
        em.getTransaction().commit();
        long id = parent.getId();
        em.close();

        em = emf.createEntityManager();
        parent = em.find(CascadingOneManyParent.class, id);
        assertNotNull(parent);
        assertEquals(2, parent.getChildren().size());
        em.getTransaction().begin();
        em.remove(parent);
        em.getTransaction().commit();
        assertRemoved(em, id);
        em.close();

        em = emf.createEntityManager();
        assertRemoved(em, id);
        em.close();
    }

    private void assertRemoved(EntityManager em, long id) {
        assertNull(em.find(CascadingOneManyParent.class, id));
        List res = em.createQuery("select c from CascadingOneManyChild c").
            getResultList();
        assertEquals(0, res.size());
    }

    public void testForeignKey() {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        if (!conf.getDBDictionaryInstance().supportsForeignKeys)
            return;

        CascadingOneManyParent parent = new CascadingOneManyParent();
        parent.setName("parent");
        CascadingOneManyChild child;
        for (int i = 0; i < 2; i++) {
            child = new CascadingOneManyChild();
            child.setName("child" + i);
            parent.addChild(child);
        }

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(parent);
        em.getTransaction().commit();
        long id = parent.getId();
        em.close();

        OpenJPAEntityManager oem = (OpenJPAEntityManager) emf.
            createEntityManager();
        parent = oem.find(CascadingOneManyParent.class, id);
        assertNotNull(parent);
        assertEquals(2, parent.getChildren().size());
        child = parent.getChildren().get(0); 
        oem.getTransaction().begin();
        oem.remove(parent);
        // undelete one child
        assertTrue(oem.isRemoved(child));
        oem.persist(child);
        assertFalse(oem.isRemoved(child));
        assertEquals(parent, child.getParent());
        try {
            oem.getTransaction().commit();
            fail("Commit should have failed due to FK constraint violation.");
        } catch (Exception e) {
        }
        oem.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestCascadingOneManyWithForeignKey.class);
    }
}


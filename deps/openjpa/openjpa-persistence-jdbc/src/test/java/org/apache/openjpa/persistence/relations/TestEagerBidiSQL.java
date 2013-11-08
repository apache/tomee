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
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test that when both sides of a mapped-by relation are eager fetched, 
 * traversing from the mapped-by side to the mapping side cuts off -- that
 * we don't traverse in a loop back to the mapped-by side in the generated
 * SQL.
 *
 * @author Abe White
 */
public class TestEagerBidiSQL
    extends SQLListenerTestCase {

    private long id1;
    private long id2;

    public void setUp() {
        setUp(BidiParent.class, BidiChild.class);
        
        
        // If using an Oracle DB, use sql92 syntax in order to get a correct
        // comparison of SQL.  This may not work on Oracle JDBC drivers
        // prior to 10.x
        DBDictionary dict = ((JDBCConfiguration) emf.getConfiguration())
            .getDBDictionaryInstance();
        if (dict instanceof OracleDictionary) {
            dict.setJoinSyntax("sql92");
        }
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        for (int i = 1; i <= 2; i++) {
            BidiParent parent = new BidiParent();
            parent.setName("parent" + i);
            em.persist(parent);

            BidiChild oneOneChild = new BidiChild();
            oneOneChild.setName("oneToOneChild" + i);
            oneOneChild.setOneToOneParent(parent);
            parent.setOneToOneChild(oneOneChild);
            em.persist(oneOneChild);

            for (int j = 1; j <= 3; j++) {
                BidiChild oneManyChild = new BidiChild();
                oneManyChild.setName("oneToManyChild" + i + "::" + j);
                oneManyChild.setOneToManyParent(parent);
                parent.getOneToManyChildren().add(oneManyChild);
                em.persist(oneManyChild);
            }

            if (i == 1)
                id1 = parent.getId();
            else
                id2 = parent.getId();
        }

        em.getTransaction().commit();
        em.close();
    }

    public void testEagerOwnerOneToManyFind() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getFetchPlan().addField(BidiParent.class, "oneToManyChildren");
        em.getFetchPlan().addField(BidiChild.class, "oneToManyParent");
        BidiParent parent = em.find(BidiParent.class, id1);
        assertEquals(1, sql.size());
        assertNotSQL(".* LEFT OUTER JOIN BidiParent .*");
        assertEquals("parent1", parent.getName());
        assertEquals(3, parent.getOneToManyChildren().size());
        for (int i = 0; i < 3; i++) {
            assertEquals("oneToManyChild1::" + (i + 1), 
                parent.getOneToManyChildren().get(i).getName());
            assertEquals(parent,
                parent.getOneToManyChildren().get(i).getOneToManyParent());
        }
        assertEquals(1, sql.size());
        em.close();
    }

    public void testEagerOwnerOneToOneFind() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        em.getFetchPlan().addField(BidiChild.class, "oneToOneParent");
        BidiParent parent = em.find(BidiParent.class, id1);
        assertEquals(1, sql.size());
        assertNotSQL(".* LEFT OUTER JOIN BidiParent .*");
        assertEquals("parent1", parent.getName());
        assertNotNull(parent.getOneToOneChild());
        assertEquals("oneToOneChild1", parent.getOneToOneChild().getName());
        assertEquals(parent, parent.getOneToOneChild().getOneToOneParent());
        assertEquals(1, sql.size());
        em.close();
    }

    public void testEagerOwnerOneToManyQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("SELECT o FROM BidiParent o "
            + "ORDER BY o.name ASC");
        q.getFetchPlan().addField(BidiParent.class, "oneToManyChildren");
        q.getFetchPlan().addField(BidiChild.class, "oneToManyParent");
        List<BidiParent> res = (List<BidiParent>) q.getResultList(); 

        assertEquals(2, res.size());
        assertEquals(sql.toString(), 2, sql.size());
        assertNotSQL(".* LEFT OUTER JOIN BidiParent .*");

        for (int i = 0; i < res.size(); i++) {
            assertEquals("parent" + (i + 1), res.get(i).getName());
            assertEquals(3, res.get(i).getOneToManyChildren().size());
            for (int j = 0; j < 3; j++) {
                assertEquals("oneToManyChild" + (i + 1) + "::" + (j + 1), 
                    res.get(i).getOneToManyChildren().get(j).getName());
                assertEquals(res.get(i), res.get(i).getOneToManyChildren().
                    get(j).getOneToManyParent());
            }
        }
        assertEquals(2, sql.size());
        em.close();
    }

    public void testEagerOwnerOneToOneQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("SELECT o FROM BidiParent o "
            + "ORDER BY o.name ASC");
        q.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        q.getFetchPlan().addField(BidiChild.class, "oneToOneParent");
        List<BidiParent> res = (List<BidiParent>) q.getResultList(); 

        assertEquals(2, res.size());
        assertEquals(1, sql.size());
        assertNotSQL(".* LEFT OUTER JOIN BidiParent .*");

        for (int i = 0; i < res.size(); i++) {
            assertEquals("parent" + (i + 1), res.get(i).getName());
            assertNotNull(res.get(i).getOneToOneChild());
            assertEquals("oneToOneChild" + (i + 1),
                res.get(i).getOneToOneChild().getName());
            assertEquals(res.get(i), 
                res.get(i).getOneToOneChild().getOneToOneParent());
        }
        assertEquals(1, sql.size());
        em.close();
    }

    public void testEagerNonOwnerOneToOneQuery() {
        sql.clear();

        OpenJPAEntityManager em = emf.createEntityManager();
        OpenJPAQuery q = em.createQuery("SELECT o FROM BidiParent o "
            + "ORDER BY o.name ASC");
        q.getFetchPlan().addField(BidiParent.class, "oneToOneChild");
        q.getFetchPlan().addField(BidiChild.class, "oneToManyParent");
        List<BidiParent> res = (List<BidiParent>) q.getResultList(); 

        assertEquals(2, res.size());
        assertEquals(1, sql.size());
        assertSQL(".* LEFT OUTER JOIN BidiParent .*");

        for (int i = 0; i < res.size(); i++) {
            assertEquals("parent" + (i + 1), res.get(i).getName());
            assertNotNull(res.get(i).getOneToOneChild());
            assertEquals("oneToOneChild" + (i + 1),
                res.get(i).getOneToOneChild().getName());
            assertNull(res.get(i).getOneToOneChild().getOneToManyParent());
        }
        assertEquals(1, sql.size());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestEagerBidiSQL.class);
    }
}


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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.NoResultException;


import org.apache.openjpa.persistence.query.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.jdbc.FetchMode;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;

/**
 * Test that ResultList objects behaver correctly.
 *
 * @author Marc Prud'hommeaux
 */
public class TestQueryResults extends BaseQueryTest {

    public TestQueryResults(String test) {
        super(test);
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);

        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        em.persist(new RuntimeTest1("TestQueryResults1", 10));
        em.persist(new RuntimeTest1("TestQueryResults3", 10));
        em.persist(new RuntimeTest1("TestQueryResults5", 10));
        em.persist(new RuntimeTest3("TestQueryResults2", 10));
        em.persist(new RuntimeTest3("TestQueryResults4", 10));
        em.persist(new RuntimeTest3("TestQueryResults6", 10));
        endTx(em);
        endEm(em);
    }

    public void testQueryIteratorsReturnFalseForClosedQuery() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery("SELECT o FROM RuntimeTest1 o");
        List c = q.getResultList();
        Iterator i = c.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        q.closeAll();
        endTx(em);
        endEm(em);

        if (i.hasNext())
            fail("Iterator obtained from Query should return false "
                + "for hasNext() after Query has been closed");
    }

    public void testQueryIteratorsThrowExceptionForClosedQuery() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery("SELECT o FROM RuntimeTest1 o");
        List c = q.getResultList();
        Iterator i = c.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        q.closeAll();
        endTx(em);
        endEm(em);

        try {
            i.next();
            fail("Iterator.next() should have thrown Exception "
                + "after query.closeAll() was called");
        }
        catch (Exception e) {
            //
        }
    }

    public void testLazyQueryIteratorsReturnFalseForClosedem() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        String query = "SELECT o FROM RuntimeTest1 o";
        OpenJPAQuery q = em.createQuery(query);
        q.getFetchPlan().setFetchBatchSize(5);
        List c = q.getResultList();

        Iterator i = c.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        endTx(em);
        endEm(em);

        if (i.hasNext())
            fail("Lazy result iterator obtained from Query should return "
                + "false for hasNext() after em has been closed");
    }

    public void testEagerQueryIteratorsWorkForClosedem() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        String query = "SELECT o FROM RuntimeTest1 o";
        OpenJPAQuery q = em.createQuery(query);
        q.getFetchPlan().setFetchBatchSize(-1);

        List c = q.getResultList();

        Iterator i = c.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        endTx(em);
        endEm(em);

        if (!i.hasNext())
            fail("Eager result iterator obtained from Query should return "
                + "true for hasNext() after em has been closed");
    }

    public void testQueryResultIsList() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        String query = "SELECT o FROM RuntimeTest1 o";

        Collection c = (Collection) em.createQuery(query).getResultList();
        if (!(c instanceof List))
            fail("Collection (" + c.getClass() + ") should have "
                + "been a List instance");

        endTx(em);
        endEm(em);
    }

    public void testQueryResultSizeIsCorrect() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        em.persist(new RuntimeTest2("TestQueryResults1", 10));
        endTx(em);
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        String query = "SELECT r FROM RuntimeTest2 r WHERE r.stringField = "
            + "\'TestQueryResults1\'";
        List c = em.createQuery(query).getResultList();

        assertEquals(1, c.size());
        endTx(em);
        endEm(em);
    }

    public void testExtentIteratorsReturnFalseForClosedExtent() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        Extent extent = em.createExtent(RuntimeTest1.class, true);

        Iterator i = extent.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        extent.closeAll();

        if (i.hasNext())
            fail("Iterator obtained from Extent should return false "
                + "for hasNext() after Extent has been closed");

        endTx(em);
        endEm(em);
    }

    public void testExtentIteratorsThrowExceptionForClosedExtent() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        Extent extent = em.createExtent(RuntimeTest1.class, true);

        Iterator i = extent.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        extent.closeAll();

        try {
            i.next();
            fail("Iterator.next() should have thrown Exception "
                + "after Extent.closeAll() was called");
        } catch (Exception e) {
            // this is a *good* thing.
        }

        endTx(em);
        endEm(em);
    }

    public void testExtentIteratorsReturnFalseForClosedem() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        Extent extent = em.createExtent(RuntimeTest1.class, true);
        Iterator i = extent.iterator();
        if (!(i.hasNext()))
            fail("Iterator should have had next()");
        endTx(em);
        endEm(em);

        if (i.hasNext())
            fail("Iterator obtained from Extent should return false "
                + "for hasNext() after em has been closed");
    }

    public void testUniqueReturnsSingleResult() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        String query = "SELECT DISTINCT r FROM RuntimeTest1 r WHERE "
            + "r.stringField = \'TestQueryResults1\'";
        Object obj = em.createQuery(query).getSingleResult();

        assertTrue(obj instanceof RuntimeTest1);

        query = "SELECT DISTINCT r FROM RuntimeTest1 r WHERE r.stringField = "
            + "\'xxxx\'";
        OpenJPAQuery q = em.createQuery(query);
        try {
            Object l = q.getSingleResult();
            fail("Expected NoResultException since there is no RuntimeTest1 "
                 + "instance with stringfield=xxxx");
        } catch (NoResultException e) {
            // good
        }

        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testUniqueThrowsExceptionIfMultipleResults() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        String query = "SELECT DISTINCT r FROM RuntimeTest1 r";
        OpenJPAQuery q = em.createQuery(query);

        try {
            Object l = q.getSingleResult();
            fail("Unique query matched multiple results.");
        }
        catch (Exception jue) {
        }
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testImpossibleRangeReturnsEmptyList() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        String query = "SELECT r FROM RuntimeTest1 r";
        OpenJPAQuery q = em.createQuery(query);
        q.setFirstResult(2);
        q.setMaxResults(0);

        List results = q.getResultList();

        assertEquals(0, results.size());
        assertFalse(results.iterator().hasNext());
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testImpossibleUniqueRangeReturnsNull() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery("SELECT DISTINCT r FROM RuntimeTest1 r "
            + "WHERE r.stringField = \'TestQueryResults1\'");
        q.setFirstResult(2);
        q.setMaxResults(0);
        assertTrue(
            "resultlist is not null its size is: " + q.getResultList().size(),
            q.getResultList().isEmpty());
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testSingleResultUniqueRange() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery("SELECT DISTINCT r FROM RuntimeTest1 r "
            + "WHERE r.stringField = \'TestQueryResults1\'");
        q.setFirstResult(1);
        q.setMaxResults(1000000);

        assertTrue("resultlist is not empty", (q.getResultList()).isEmpty());
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testMultiResultUniqueRange() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT DISTINCT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
        q.setFirstResult(1);
        q.setMaxResults(2);

        assertEquals("TestQueryResults2",
            ((RuntimeTest1) q.getResultList().get(0)).getStringField());
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    /* This test is being commented because it was supposed to be a converted
     * test complementing the original JDO test which uses the setUnique()
     * method available in JDO Query. OpenJPAQuery does not have such a method
     * and hence this test does not make sense.
      public void testUniqueThrowsExceptionIfNonUniqueRange()
     {
         OpenJPAEntityManager em =
             (OpenJPAEntityManager) currentEntityManager();
         startTx(em);

         OpenJPAQuery q = em.createQuery(
            "SELECT DISTINCT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
         q.setFirstResult(1);
         q.setMaxResults(3);

         try
         {
             q.getResultList();
             fail("Unique allowed non-unique range.");
         }
         catch (Exception jue)
         {
         }
         q.closeAll();
         endTx(em);
         endEm(em);
     }
     */
    public void testFullRange() {
        try {
            OpenJPAEntityManager em =
                (OpenJPAEntityManager) currentEntityManager();
            startTx(em);

            OpenJPAQuery q = em.createQuery(
                "SELECT r FROM RuntimeTest1 ORDER BY r.stringField ASC");
            q.setSubclasses(false);
            q.setFirstResult(0);
            Long l = new Long(Long.MAX_VALUE);
            q.setMaxResults(l.intValue());

            List res = (List) q.getResultList();
            assertEquals(3, res.size());
            for (int i = 0; i < res.size(); i++)
                assertEquals("TestQueryResults" + (i * 2 + 1),
                    ((RuntimeTest1) res.get(i)).getStringField());
            q.closeAll();
            endTx(em);
            endEm(em);
        }
        catch (Exception uoe) {
            //FIXME:AFAM -- Figure out JPA Equivalence of createExtent(class,
            //false) ie how to restrict the query result to the base entity and
            //not the subclasses
        }
    }

    public void testFullRangeSubs() {
        try {
            OpenJPAEntityManager em =
                (OpenJPAEntityManager) currentEntityManager();
            startTx(em);

            OpenJPAQuery q = em.createQuery(
                "SELECT r FROM RuntimeTest1 ORDER BY r.stringField ASC");
            q.setFirstResult(0);
            Long l = new Long(Long.MAX_VALUE);
            q.setMaxResults(l.intValue());

            List res = (List) q.getResultList();
            assertEquals(6, res.size());
            for (int i = 0; i < res.size(); i++)
                assertEquals("TestQueryResults" + (i + 1),
                    ((RuntimeTest1) res.get(i)).getStringField());
            q.closeAll();
            endTx(em);
            endEm(em);
        }
        catch (Exception uoe) {
        }
    }

    public void testBeginRange() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
        q.setSubclasses(false);
        for (int i = 0; i < 4; i++) {
            q.setFirstResult(i);
            q.setMaxResults(100000);

            List res = (List) q.getResultList();
            assertEquals("they are not equal", 3 - i, res.size());
            int idx = 0;

            // try both random acess and iteration
            for (int j = 0; j < res.size(); j++)
                assertEquals("TestQueryResults" + (j * 2 + 1 + i * 2),
                    (((RuntimeTest1) res.get(j)).getStringField()));
            for (Iterator itr = res.iterator(); itr.hasNext(); idx++)
                assertEquals("TestQueryResults" + (idx * 2 + 1 + i * 2),
                    ((RuntimeTest1) itr.next()).getStringField());
        }
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testBeginRangeSubs() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");

        for (int i = 0; i < 7; i++) {
            q.setFirstResult(i);
            Long l = new Long(Long.MAX_VALUE);
            q.setMaxResults(100000);

            List res = (List) q.getResultList();
            assertEquals(6 - i, res.size());
            int idx = 0;

            // try both random acess and iteration
            for (int j = 0; j < res.size(); j++)
                assertEquals("TestQueryResults" + (j + 1 + i),
                    ((RuntimeTest1) res.get(j)).getStringField());
            for (Iterator itr = res.iterator(); itr.hasNext(); idx++)
                assertEquals("TestQueryResults" + (idx + 1 + i),
                    ((RuntimeTest1) itr.next()).getStringField());
        }
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testEndRange() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
        q.setSubclasses(false);

        for (int i = 0; i < 4; i++) {
            q.setFirstResult(0);
            q.setMaxResults(i);

            List res = (List) q.getResultList();
            assertEquals(i, res.size());
            int idx = 0;

            // try both random acess and iteration
            for (int j = 0; j < res.size(); j++)
                assertEquals("TestQueryResults" + (j * 2 + 1),
                    ((RuntimeTest1) res.get(j)).getStringField());
            for (Iterator itr = res.iterator(); itr.hasNext(); idx++)
                assertEquals("TestQueryResults" + (idx * 2 + 1),
                    ((RuntimeTest1) itr.next()).getStringField());
        }
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testEndRangeSubs() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");

        for (int i = 0; i < 7; i++) {
            q.setFirstResult(0);
            q.setMaxResults(i);
            List res = (List) q.getResultList();
            assertEquals(i, res.size());
            int idx = 0;

            // try both random acess and iteration
            for (int j = 0; j < res.size(); j++)
                assertEquals("TestQueryResults" + (j + 1),
                    ((RuntimeTest1) res.get(j)).getStringField());
            for (Iterator itr = res.iterator(); itr.hasNext(); idx++)
                assertEquals("TestQueryResults" + (idx + 1),
                    ((RuntimeTest1) itr.next()).getStringField());
        }
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testMidRange() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
        q.setSubclasses(false);

        q.setFirstResult(1);
        q.setMaxResults(3);
        List res = (List) q.getResultList();
        assertEquals(2, res.size());
        for (int i = 0; i < res.size(); i++)
            assertEquals("TestQueryResults" + (i * 2 + 1 + 2),
                ((RuntimeTest1) res.get(i)).getStringField());
        int idx = 0;
        for (Iterator itr = res.iterator(); itr.hasNext(); idx++)
            assertEquals("TestQueryResults" + (idx * 2 + 1 + 2),
                ((RuntimeTest1) itr.next()).getStringField());
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testMidRangeSubs() {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
        q.setFirstResult(1);
        q.setMaxResults(3);

        List res = (List) q.getResultList();
        assertEquals(3, res.size());
        for (int i = 0; i < res.size(); i++)
            assertEquals("TestQueryResults" + (i + 1 + 1),
                ((RuntimeTest1) res.get(i)).getStringField());
        int idx = 0;
        for (Iterator itr = res.iterator(); itr.hasNext(); idx++)
            assertEquals("TestQueryResults" + (idx + 1 + 1),
                ((RuntimeTest1) itr.next()).getStringField());
        q.closeAll();
        endTx(em);
        endEm(em);
    }

    public void testPessimisticOrderedRange() {
        // test to make sure whatever machinations we do to get a range doesn't
        // interfere with FOR UPDATE
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        OpenJPAQuery q = em.createQuery(
            "SELECT r FROM RuntimeTest1 r ORDER BY r.stringField ASC");
        q.setSubclasses(false);
        q.setFirstResult(0);
        q.setMaxResults(2);

        ((JDBCFetchPlan) q.getFetchPlan()).setEagerFetchMode(FetchMode.NONE);

        List res = (List) q.getResultList();
        assertEquals(2, res.size());
        assertEquals("TestQueryResults1",
            ((RuntimeTest1) res.get(0)).getStringField());
        assertEquals("TestQueryResults3",
            ((RuntimeTest1) res.get(1)).getStringField());
        q.closeAll();
        endTx(em);
        endEm(em);
    }
}


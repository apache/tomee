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
package org.apache.openjpa.persistence.jdbc.kernel;

import java.util.*;

import org.apache.openjpa.jdbc.sql.*;
import org.apache.openjpa.persistence.jdbc.common.apps.*;


import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
//import org.apache.openjpa.kernel.Extent;
import org.apache.openjpa.persistence.Extent;

/**
 * <p>Test large result, to-many eager paging.</p>
 *
 */
public class TestPagingResultObjectProvider
    extends TestSQLListenerTestCase {

	private OpenJPAEntityManagerFactory emf;

	public TestPagingResultObjectProvider(String name)
	{
		super(name);
	}

    public void setUp() throws Exception{
		super.setUp();
		emf = getEmf(getProps());
    	EntityManager em =currentEntityManager();
    }



    public boolean skipTest() {
        JDBCConfiguration conf =
            (JDBCConfiguration) ((OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.cast(emf)).getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        if (dict.joinSyntax == Join.SYNTAX_TRADITIONAL)
            return true;

        // skip test case that requires subselect for DBs that don't support it
        if ("testPagingRangeQuery".equals(getName())
            || "testNonPagingRangeQuery".equals(getName())) {
            if (!conf.getDBDictionaryInstance().supportsSubselect)
                return true;
        }

        return false;
    }


    public void testNonPagingExtent() {
        initializePagingPCs();

        EntityManager em= currentEntityManager();
        //EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        Extent extent = kem.createExtent(PagingPC.class, true);

        extent.getFetchPlan().setFetchBatchSize(0);
        extent.getFetchPlan().addFetchGroup("rel");

        // if we only access the rel field, we should only have used 1 select
        List list = extent.list();
        assertEquals(4, list.size());
        assertRel(list, 4);
        assertEquals(sql.toString(), 1, sql.size());
        sql.clear();

        // now accessing the rels collection should add selects
        assertRels(list, 4);
        assertEquals(sql.toString(), 4, sql.size());
        sql.clear();

        em.close();
    }

    public void testNonPagingQuery()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        String theQuery = "select a FROM " + PagingPC.class.getSimpleName()
            + " a where a.intField >= 0";

        //OpenJPAQuery q = kem.createQuery(PagingPC.class.getSimpleName(),
		//    "intField >= 0");
        OpenJPAQuery q = kem.createQuery(theQuery);

        q.getFetchPlan().setFetchBatchSize(0);
        q.getFetchPlan().addFetchGroups("rel");

        // if we only access the rel field, we should only have used two
        // selects: one for the query and one for the size
        List list = (List) q.getResultList();
        assertEquals(4, list.size());
        assertRel(list, 4);
        assertEquals(sql.toString(), 2, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        assertTrue((String) sql.get(1),
            matches(" COUNT", (String) sql.get(1)));
        sql.clear();

        // now accessing the rels collection should add selects
        assertRels(list, 4);
        assertEquals(sql.toString(), 4, sql.size());
        sql.clear();

        em.close();
    }

    /**
     * Check that the rel field was retrieved correctly.
     */
   private void assertRel(List pcs, int size) {
        PagingPC pc;
        for (int i = 0; i < size; i++) {
            pc = (PagingPC) pcs.get(i);
            assertNotNull(pc.getRel());
            assertEquals(pc.getIntField() + 1, pc.getRel().getIntField());
        }
        try {
            pcs.get(size + 1);
            fail("Retrieved past end of list");
        } catch (Exception e) {
        }
    }

    /**
     * Check that the rels field was retrieved correctly.
     */
    private void assertRels(List pcs, int size) {
        PagingPC pc;
        for (int i = 0; i < size; i++) {
            pc = (PagingPC) pcs.get(i);
            assertEquals(2, pc.getRels().size());
            assertEquals(pc.getIntField() + 1, ((PagingHelperPC)
                pc.getRels().get(0)).getIntField());
            assertEquals(pc.getIntField() + 2, ((PagingHelperPC)
                pc.getRels().get(1)).getIntField());
        }
        try {
            pcs.get(size + 1);
            fail("Retrieved past end of list");
        } catch (Exception e) {
        }
    }

    public void testPagingExtent()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        Extent extent = kem.createExtent(PagingPC.class, true);
        extent.getFetchPlan().setFetchBatchSize(0);
        extent.getFetchPlan().addFetchGroup("rel");
        extent.getFetchPlan().addFetchGroup("rels");


        // we should get two selects: the initial select and the IN eager stmnt
        List list = extent.list();
        assertEquals(4, list.size());
        assertRel(list, 4);
        assertRels(list, 4);
        assertEquals(sql.toString(), 2, sql.size());
        assertTrue((String) sql.get(1),
            matches(" IN ", (String) sql.get(1)));
        sql.clear();

        em.close();
    }

    public void testPagingQuery()
        throws Exception {
        initializePagingPCs();


        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);

        String theQuery = "select a FROM " + PagingPC.class.getSimpleName()
            + " a where a.intField >= 0";
        OpenJPAQuery q = kem.createQuery(theQuery);
        q.getFetchPlan().setFetchBatchSize(0);
        q.getFetchPlan().addFetchGroups("rel");
        q.getFetchPlan().addFetchGroups("rels");


        // we should get three selects: the initial select, the COUNT for the
        // size, and the IN eager stmnt
        List list = (List) q.getResultList();
        assertEquals(4, list.size());
        assertRel(list, 4);
        assertRels(list, 4);
        assertEquals(sql.toString(), 3, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        assertTrue((String) sql.get(1),
            matches(" COUNT", (String) sql.get(1)));
        assertTrue((String) sql.get(2),
            matches(" IN ", (String) sql.get(2)));
        assertFalse((String) sql.get(2),
            matches(" >= ", (String) sql.get(2)));
        sql.clear();

        // if we execute and traverse a result before getting the size, we
        // should only get 2 selects, since the caching of the page should
        // reach the end of the list and therefore know the total size
        list = (List) q.getResultList();
        list.get(0);
        assertEquals(4, list.size());
        assertRel(list, 4);
        assertRels(list, 4);
        assertEquals(sql.toString(), 2, sql.size());
        assertTrue((String) sql.get(1),
            matches(" IN ", (String) sql.get(1)));
        sql.clear();

        em.close();
    }

    public void testPagingAppIdExtent()
        throws Exception {
        initializePagingAppIdPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        Extent extent = kem.createExtent(PagingAppIdPC.class, true);

        extent.getFetchPlan().setFetchBatchSize(0);
        extent.getFetchPlan().addFetchGroup("rel");
        extent.getFetchPlan().addFetchGroup("rels");

        // we should get two selects: the initial select and the IN eager stmnt
        List list = extent.list();
        assertEquals(4, list.size());
        assertAppIdRel(list, 4);
        assertAppIdRels(list, 4);
        assertEquals(sql.toString(), 2, sql.size());
        assertTrue((String) sql.get(1),
            matches(" OR ", (String) sql.get(1)));
        sql.clear();

        em.close();
    }

    public void testPagingAppIdQuery()
        throws Exception {
        initializePagingAppIdPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        String theQuery = "select a FROM " + PagingAppIdPC.class.getSimpleName()
            + " a where a.intField >= 0";
        OpenJPAQuery q = kem.createQuery(theQuery);


        q.getFetchPlan().setFetchBatchSize(0);
        q.getFetchPlan().addFetchGroups("rel");
        q.getFetchPlan().addFetchGroups("rels");

        // we should get three selects: the initial select, the COUNT for the
        // size, and the OR eager stmnt
        List list = (List) q.getResultList();
        assertEquals(4, list.size());
        assertAppIdRel(list, 4);
        assertAppIdRels(list, 4);
        assertEquals(sql.toString(), 3, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        assertTrue((String) sql.get(1),
            matches(" COUNT", (String) sql.get(1)));
        assertTrue((String) sql.get(2),
            matches(" OR ", (String) sql.get(2)));
        assertFalse((String) sql.get(2),
            matches(" >= ", (String) sql.get(2)));
        sql.clear();

        em.close();
    }

    /**
     * Check that the rel field was retrieved correctly.
     */
    private void assertAppIdRel(List pcs, int size) {
        PagingAppIdPC pc;
        for (int i = 0; i < size; i++) {
            pc = (PagingAppIdPC) pcs.get(i);
            assertNotNull(pc.getRel());
            assertEquals(pc.getIntField() + 1, pc.getRel().getIntField());
        }
        try {
            pcs.get(size + 1);
            fail("Retrieved past end of list");
        } catch (Exception e) {
        }
    }

    /**
     * Check that the rels field was retrieved correctly.
     */
    private void assertAppIdRels(List pcs, int size) {
        PagingAppIdPC pc;
        for (int i = 0; i < size; i++) {
            pc = (PagingAppIdPC) pcs.get(i);
            assertEquals(2, pc.getRels().size());
            assertEquals(pc.getIntField() + 1, ((PagingHelperPC)
                pc.getRels().get(0)).getIntField());
            assertEquals(pc.getIntField() + 2, ((PagingHelperPC)
                pc.getRels().get(1)).getIntField());
        }
        try {
            pcs.get(size + 1);
            fail("Retrieved past end of list");
        } catch (Exception e) {
        }
    }

    public void testMultiPageExtent()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);
        Extent extent = kem.createExtent(PagingPC.class, true);

        extent.getFetchPlan().setFetchBatchSize(2);    // 2 pages of 2
        extent.getFetchPlan().addFetchGroup("rel");
        extent.getFetchPlan().addFetchGroup("rels");

        // we should get two selects: the initial select and the IN eager stmnt
        List list = extent.list();
        assertEquals(list.toString(), 4, list.size());
        assertRel(list, 4);
        assertRels(list, 4);
        assertEquals(sql.toString(), 3, sql.size());
        assertTrue((String) sql.get(1),
            matches(" IN ", (String) sql.get(1)));
        assertTrue((String) sql.get(2),
            matches(" IN ", (String) sql.get(2)));
        sql.clear();

        em.close();
    }

    public void testMultiPageQuery()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        String theQuery = "select a FROM " + PagingPC.class.getSimpleName()
            + " a where a.intField >= 0";
        OpenJPAQuery q = kem.createQuery(theQuery);


		//FIXME jthomas commenting till we find how ordering can be done
        //q.setOrdering("intField ascending");
        q.getFetchPlan().setFetchBatchSize(2);    // 2 pages of 2
        q.getFetchPlan().addFetchGroup("rel");
        q.getFetchPlan().addFetchGroup("rels");

        // we should get four selects: the initial select, the count, and the
        // two IN eager stmnts
        List list = (List) q.getResultList();
        assertEquals(list.toString(), 4, list.size());
        // so we don't have to re-execute to move back to beginning
        list = new ArrayList(list);
        assertRel(list, 4);
        assertRels(list, 4);

        assertEquals(sql.toString(), 4, sql.size());
        assertTrue((String) sql.get(1),
            matches(" COUNT", (String) sql.get(1)));
        assertTrue((String) sql.get(2),
            matches(" IN ", (String) sql.get(2)));
        assertTrue((String) sql.get(3),
            matches(" IN ", (String) sql.get(3)));
        sql.clear();

        // if we access a result object on the second page, we should get only
        // three selects: the initial select, the count, and the IN eager stmnt
        // for page 2
        list = (List) q.getResultList();
        assertEquals(list.toString(), 4, list.size());
        PagingPC pc = (PagingPC) list.get(2);
        assertEquals(2, pc.getIntField());
        assertEquals(sql.toString(), 3, sql.size());
        assertTrue((String) sql.get(2),
            matches(" IN", (String) sql.get(2)));

        assertRel(list, 4);
        assertRels(list, 4);
        sql.clear();

        em.close();
    }

    public void testLastPageQuery()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);

        String theQuery = "select a FROM " + PagingPC.class.getSimpleName()
            + " a where a.intField >= 0";
        OpenJPAQuery q = kem.createQuery(theQuery);


        //FIXME jthomas commenting till we find how ordering can be done
        //q.setOrdering("intField ascending");
        q.getFetchPlan().setFetchBatchSize(3);    // 1 on page 2
        q.getFetchPlan().addFetchGroups("rel");
        q.getFetchPlan().addFetchGroups("rels");

        // if we access a result object on the second page, we should get only
        // two selects: the initial select and the eager stmnt for page
        List list = (List) q.getResultList();
        PagingPC pc = (PagingPC) list.get(3);
        assertEquals(3, pc.getIntField());
        assertEquals(list.toString(), 4, list.size());
        assertEquals(sql.toString(), 2, sql.size());
        // for single result, should use standard select, not IN
        assertFalse((String) sql.get(1),
            matches(" IN ", (String) sql.get(1)));
        sql.clear();

        em.close();
    }

    public void testSingleResultPage()
        throws Exception {
        initializePagingPCs();

	    EntityManager em =currentEntityManager();
        OpenJPAEntityManager kem = OpenJPAPersistence.cast (em);
        Extent extent = kem.createExtent(PagingPC.class, true);
        extent.getFetchPlan().setFetchBatchSize(3);    // 1 on page 2
        extent.getFetchPlan().addFetchGroup("rel");
        extent.getFetchPlan().addFetchGroup("rels");

        // we should get two selects: the initial select and the IN eager stmnt
        List list = extent.list();
        assertEquals(list.toString(), 4, list.size());
        assertRel(list, 4);
        assertRels(list, 4);
        assertEquals(sql.toString(), 3, sql.size());
        assertTrue((String) sql.get(1),
            matches(" IN ", (String) sql.get(1)));
        assertFalse((String) sql.get(2),
            matches(" IN ", (String) sql.get(2)));
        sql.clear();

        em.close();
    }

  /*  public void testNonPagingRangeQuery()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        Query q = em.newQuery(PagingPC.class, "intField >= 0");

        //FIXME jthomas commenting till we find how ordering can be done
        //q.setOrdering("intField ascending");
        q.setRange(1, 3);
        q.getFetchPlan().setFetchSize(0);
        q.getFetchPlan().addGroup("rel");

        // if we only access the rel field, we should only have used one
        // select; there won't be a size select since the range will be
        // greedily consumed as it's so small
        List list = (List) q.execute();
        assertEquals(2, list.size());
        assertRel(list, 2);
        assertEquals(sql.toString(), 1, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        sql.clear();

        // now accessing the rels collection should add selects
        assertRels(list, 2);
        assertEquals(sql.toString(), 2, sql.size());
        sql.clear();

        // now execute with a big range and small fetch size and bigger range
        // and this time we should get a select for the size too
        q.getFetchPlan().setFetchSize(2);
        q.setRange(1, 4);
        list = (List) q.execute();
        assertEquals(3, list.size());
        // so we don't have to re-execute to move back to beginning
        list = new ArrayList(list);
        assertRel(list, 3);
        assertEquals(sql.toString(), 2, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        assertTrue((String) sql.get(1),
            matches(" COUNT", (String) sql.get(1)));
        sql.clear();

        em.close();
    }
/*
    public void testPagingRangeQuery()
        throws Exception {
        initializePagingPCs();

        EntityManager em =currentEntityManager();
        Query q = em.newQuery(PagingPC.class, "intField >= 0");

        //FIXME jthomas commenting till we find how ordering can be done
        //q.setOrdering("intField ascending");
        q.setRange(1, 3);
        q.getFetchPlan().setFetchSize(0);
        q.getFetchPlan().addGroup("rel");
        q.getFetchPlan().addGroup("rels");

        // expect two selects: the primary select and the IN select; no size
        // select expected because the range will be consumed greedily since
        // it's so small
        List list = (List) q.execute();
        assertEquals(2, list.size());
        assertRel(list, 2);
        assertRels(list, 2);
        assertEquals(sql.toString(), 2, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        assertTrue(sql + "",
            matches(" IN ", (String) sql.get(1)));
        sql.clear();

        // now execute with a big range and small fetch size and bigger range
        // and this time we should get a select for the size too
        q.getFetchPlan().setFetchSize(2);
        q.setRange(1, 4);
        list = (List) q.execute();
        assertEquals(3, list.size());
        // so we don't have to re-execute to move back to beginning
        list = new ArrayList(list);
        assertRel(list, 3);
        assertRels(list, 3);
        assertEquals(sql.toString(), 4, sql.size());
        assertTrue((String) sql.get(0),
            matches(" >= ", (String) sql.get(0)));
        assertTrue((String) sql.get(1),
            matches(" COUNT", (String) sql.get(1)));
        assertTrue((String) sql.get(2),
            matches(" IN ", (String) sql.get(2)));
        assertFalse((String) sql.get(3),
            matches(" IN ", (String) sql.get(3)));
        sql.clear();

        em.close();
    }
*/
    private void initializePagingPCs() {
        EntityManager em =currentEntityManager();
        startTx(em);

       deleteAll(PagingPC.class,em);
       deleteAll(PagingHelperPC.class,em);

        endTx(em);

        startTx(em);
        PagingPC pc;
        PagingHelperPC rel;
        for (int i = 0; i < 4; i++) {
            pc = new PagingPC();
            pc.setIntField(i);
            rel = new PagingHelperPC();
            rel.setIntField(i + 1);
            pc.setRel(rel);
            pc.getRels().add(rel);
            rel = new PagingHelperPC();
            rel.setIntField(i + 2);
            pc.getRels().add(rel);
            em.persist(pc);
        }

        endTx(em);
        em.close();
        sql.clear();
    }

    private void initializePagingAppIdPCs() {
        EntityManager em =currentEntityManager();
        startTx(em);
       deleteAll(PagingAppIdPC.class,em);
       deleteAll(PagingHelperPC.class,em);
        startTx(em);

        startTx(em);
        PagingAppIdPC pc;
        PagingHelperPC rel;
        for (int i = 0; i < 4; i++) {
            pc = new PagingAppIdPC();
            pc.setIntField(i);
            pc.setLongField(i + 100);
            rel = new PagingHelperPC();
            rel.setIntField(i + 1);
            pc.setRel(rel);
            pc.getRels().add(rel);
            rel = new PagingHelperPC();
            rel.setIntField(i + 2);
            pc.getRels().add(rel);
            em.persist(pc);
        }

        endTx(em);
     	em.close();
        sql.clear ();
	}

	private Map getProps() {
		Map props=new HashMap();
		props.put("openjpa.DataCache", "true");
		props.put("openjpa.RemoteCommitProvider", "sjvm");
		props.put("openjpa.FlushBeforeQueries", "true");
		props.put("javax.jdo.option.IgnoreCache", "false");
        //propsMap.put("openjpa.BrokerImpl", "kodo.datacache.CacheTestBroker");
        //CacheTestBroker.class.getName ());
		return props;
	}


}

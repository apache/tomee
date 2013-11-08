/*
 * TestQueries.java
 *
 * Created on October 13, 2006, 4:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.kernel;

import java.util.Collection;
import java.util.Iterator;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

public class TestQueries extends BaseKernelTest {

    private static final int CHILD_COUNT = 3;
    private int id = 10000;

    public TestQueries(String name) {
        super(name);
    }

    public TestQueries() {
    }

    public void setUp()
        throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);
    }

    public void testSimpleQuery() {
        OpenJPAEntityManager pm = getPM();

        persist(newRuntimeTest1("SimpleQueryA", 50), pm);
        persist(newRuntimeTest2("SimpleQueryB", 50), pm);

        Collection results = runQuery(RuntimeTest1.class, false,
            "stringField = \'SimpleQueryA\'", pm);
        assertEquals(1, results.size());

        results = runQuery(RuntimeTest1.class, true, "intField1 = 50", pm);
        assertEquals(2, results.size());

        results = runQuery(RuntimeTest2.class, true, "intField1 = 50", pm);
        assertEquals(1, results.size());

        results = runQuery(RuntimeTest2.class, false, "intField1 = 50", pm);
        assertEquals(1, results.size());

        endEm(pm);
    }

    public void testAndQuery() {
        String query = "intField1 > 10 AND o.intField1 < 50";

        for (int currentCount = 1; currentCount < 15; currentCount++) {
            OpenJPAEntityManager pm = getPM();

            // make sure that none exist
            deleteByQuery(RuntimeTest1.class, false, query, pm);

            startTx(pm);
            for (int i = 1; i <= currentCount; i++)
                pm.persist(newRuntimeTest1("AndQueryTest", 30 + i));
            endTx(pm);

            // verify that the query works.
            Collection results = runQuery(RuntimeTest1.class, false,
                query, pm);
            assertEquals(currentCount, results.size());
            endEm(pm);
        }
    }

    public void testRelationQuery() {
        relationQuery(10, "JOE", 20);
        relationQuery(99, "BOB", 2);
        relationQuery(3, "MARTHA", 1);
        relationQuery(5, "brenda", 43);
        relationQuery(43, "SarA", 55);
    }

    public void relationQuery(int intField, String stringField, int count) {
        OpenJPAEntityManager pm = getPM();

        String query = "selfOneOne.intField1 = " + intField
            + " AND o.selfOneOne.stringField = '" + stringField + "'";
        deleteByQuery(RuntimeTest1.class, true, query, pm);

        // we go up the to max count, adding to the people, and validate
        // with a query each time.
        for (int currentcount = 0; currentcount <= count; currentcount++) {
            if (currentcount != 0)
                persist(newRuntimeTest2(stringField, intField), pm);

            Collection results = runQuery(RuntimeTest1.class, true, query, pm);
            assertEquals("query (" + query + ") failed to yield "
                + currentcount + " instances",
                currentcount * CHILD_COUNT, results.size());
        }

        endEm(pm);
    }

    public void testQueryExecuteThrowsExceptionWhenNoNTR() {
        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        pm.persist(newRuntimeTest1("Query", 10));
        endTx(pm);

        pm.setNontransactionalRead(false);
//        OpenJPAQuery q = pm.createNativeQuery("",RuntimeTest1.class);
        OpenJPAQuery q = pm.createQuery("SELECT o FROM RuntimeTest1 o");
        try {
            q.getResultList();
            fail("Query.execute() should have thrown a JDOException when "
                + "PM is outside a Transaction and NTR==false");
        } catch (Exception jdoe) {
            // good
            startTx(pm);
            q.getResultList();
            rollbackTx(pm);
        }
    }

    /**
     * Delete the results of a query, so we can reinsert and test.
     */
    private void deleteByQuery(Class type, boolean subs, String filter,
        OpenJPAEntityManager pm) {
        startTx(pm);
        pm.createExtent(type, subs);
        //FIXME jthomas
//        OpenJPAQuery query = pm.newQuery(extent, filter);
//        Collection items = (Collection) query.execute();
        String cstrng = type.getSimpleName();

        OpenJPAQuery query =
            pm.createQuery("SELECT o FROM " + cstrng + " o WHERE o." + filter);
        query.setSubclasses(subs);
        Collection items = query.getResultList();
        for (Iterator i = items.iterator(); i.hasNext();)
            pm.remove(i.next());

        //FIXME jthomas
        //query = pm.newQuery(extent, filter);

        query =
            pm.createQuery("SELECT o FROM " + cstrng + " o WHERE o." + filter);
        query.setSubclasses(subs);
        items = query.getResultList();
        endTx(pm);
        assertEquals("after deleting from query (" + query
            + "), there should have been zero items",
            0, items.size());
    }

    private Collection runQuery(Class type, boolean subs, String filter,
        OpenJPAEntityManager pm) {
        pm.createExtent(type, subs);
        //FIXME jthomas
        //Query query = pm.newQuery(extent, filter);
        String cstrng = type.getName();
        OpenJPAQuery query =
            pm.createQuery("SELECT o FROM " + cstrng + " o WHERE o." + filter);
        query.setSubclasses(subs);
        Collection results = query.getResultList();
        return results;
    }

    private void persist(RuntimeTest1 parent, OpenJPAEntityManager pm) {
        RuntimeTest1 child;
        for (int i = 0; i < CHILD_COUNT; i++) {
            child = newRuntimeTest1("CHILD" + i, i * 10);
            child.setSelfOneOne(parent);
            parent.getSelfOneMany().add(child);
        }

        startTx(pm);
        pm.persist(parent);
        endTx(pm);
    }

    private RuntimeTest1 newRuntimeTest1(String stringField, int intField) {
        RuntimeTest1 pc = new RuntimeTest1(stringField, intField);
        pc.setIntField(id++);
        return pc;
    }

    private RuntimeTest2 newRuntimeTest2(String stringField, int intField) {
        RuntimeTest2 pc = new RuntimeTest2(stringField, intField);
        pc.setIntField(id++);
        return pc;
    }
}

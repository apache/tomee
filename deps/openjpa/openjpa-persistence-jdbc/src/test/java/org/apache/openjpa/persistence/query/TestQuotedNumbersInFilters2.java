/*
 * TestQuotedNumbersInFilters.java
 *
 * Created on October 18, 2006, 2:29 PM
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
package org.apache.openjpa.persistence.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Query;


import org.apache.openjpa.persistence.query.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestQuotedNumbersInFilters2 extends BaseQueryTest {

    public TestQuotedNumbersInFilters2(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
        OpenJPAEntityManager pm = getEM();
        startTx(pm);
        pm.persist(new RuntimeTest1("foo", 3));
        pm.persist(new RuntimeTest1("bar", 15));
        pm.persist(new RuntimeTest1("baz", -8));
        pm.persist(new RuntimeTest1("baz2", 45)); // 45 is '-'
        pm.persist(new RuntimeTest1("3", (int) '4'));
        endTx(pm);
        endEm(pm);

        // make sure everything is working as expected for the base case.
        assertEquals(1, helper("intField = -8"));
        assertEquals(1, helper("intField = 15"));
        assertEquals(1, helper("intField = 3"));
        assertEquals(0, helper("intField = 51")); // the int value of '3'
        assertEquals(0, helper("intField = 4"));
        assertEquals(1, helper("intField = 52")); // the int value of '4'
        assertEquals(1, helper("stringField = \'foo\'"));
        assertEquals(1, helper("stringField = \'bar\'"));
    }

    public void testUnquotedNumbersWithExtraPrecision() {
        assertEquals(1, helper("intField = 15"));
        assertEquals(1, helper("intField = -8"));
        assertEquals(1, helper("intField = 3"));
        assertEquals(1, helper("intField = 45"));

//    try {
//        // test without casting ... some DBs don't like this
////        assertEquals(1, helper("intField = 15.0"));
////        assertEquals(1, helper("intField = -8.0"));
//        assertEquals(1, helper("intField = 3.0"));
//        assertEquals(1, helper("intField = 45.0"));
//    } catch (Exception jdoe) {
//        bug(AbstractTestCase.Platform.HYPERSONIC, 414, jdoe,
//            "Some databases require explicit casts");
//    }
    }

    public void testSingleQuotedStrings() {
        assertEquals(1, helper("stringField = 'foo'"));
        assertEquals(1, helper("stringField = '3'"));
    }

    public void testDoubleQuotedStrings() {
        assertEquals(1, helper("stringField = \'foo\'"));
        assertEquals(1, helper("stringField = \'3\'"));
    }

    /**
     * Kodo 3.1 and prior treated single-quoted numbers as character literals,
     * to the degree that prepared statement setInt() calls were made.
     * Only the first digit of multiple-digit single-quoted numbers was used.
     * FIX ME: aokeke - commenting this --> applies to kodo 3.1 and prior
     */
    public void testKodo31SingleQuotedMultipleCharacterBehavior() {
        assertEquals(0, helper31("intField = '15'", true)); // looks like '1'
        assertEquals(0, helper31("intField = '52'", true)); // looks like '5'
        assertEquals(1, helper31("intField = '49'", true)); // looks like '4'
        assertEquals(1, helper31("intField = '-8'", true)); // looks like '-'

        assertEquals(0, helper31("intField = '15'", false));
        assertEquals(0, helper31("intField = '52'", false));
    }

    /**
     * Kodo 3.1 and prior did not match negative numbers of different types
     * in in-mem queries.
     */
    public void testKodo31UnquotedInMemBehavior() {
        assertEquals(1, helper31("intField = 3", false));
        assertEquals(1, helper31("intField = -8", false));
        assertEquals(1, helper31("intField = 15", false));
        assertEquals(1, helper31("intField = 45", false));
    }

    public void testKodo31UnquotedDatastoreBehavior() {
        assertEquals(1, helper31("intField = 3", false));
        assertEquals(1, helper31("intField = -8", false));
        assertEquals(1, helper31("intField = 15", false));
        assertEquals(1, helper31("intField = 45", false));
    }

    /**
     * Returns the # of matches to the query.
     */
    private long helper(String filter) {
        return helper(filter, false);
    }

    /**
     * Returns the # of matches to the query. Returns -1 if shouldFail
     * is true and the query raised an exception in both in-mem and datastore
     * queries.
     */
    private long helper(String filter, boolean shouldFail) {
        OpenJPAEntityManager pm = getEM();

        OpenJPAQuery q =
            pm.createQuery("SELECT r FROM RuntimeTest1 r WHERE r." + filter);

        long datastore = getResults(q, shouldFail);

        q.setCandidateCollection((Collection) q.getResultList());
        long inmem = getResults(q, shouldFail);

        if (datastore != inmem)
            fail("datastore query returned " + datastore + " values; " +
                "inmem query returned " + inmem);

        endEm(pm);
        return datastore;
    }

    /**
     * Returns the # of matches to the query. Performs the query in datastore
     * or memory as appropriate.
     */
    private long helper31(String filter, boolean datastore) {
        Map props = new HashMap();
        props.put("openjpa.Compatibility", "QuotedNumbersInQueries=true");
        OpenJPAEntityManager pm = getEmf(props).createEntityManager();

        try {
            OpenJPAQuery q = pm.createQuery(
                "SELECT r FROM RuntimeTest1 r WHERE r." + filter);

            if (!datastore)
                q.setCandidateCollection((Collection) q.getResultList());
            return getResults(q, false);
        }
        finally {
            endEm(pm);
        }
    }

    private long getResults(Query q, boolean shouldFail) {
        try {

            Integer result = new Integer(q.getResultList().size());
            if (shouldFail) {
                fail("should have failed");
            }

            return ((Number) result).longValue();
        } catch (IllegalArgumentException e) {
            if (!shouldFail)
                throw e;
            return -1;
        }
    }
}

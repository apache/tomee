/*
 * TestDateQueries.java
 *
 * Created on October 10, 2006, 1:28 PM
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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;



import org.apache.openjpa.persistence.kernel.common.apps.AllFieldTypesTest;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestDateQueries extends BaseKernelTest {

    private OpenJPAEntityManager _pm = null;
    private Date _date = null;
    private Date _before = null;
    private Date _after = null;

    /**
     * Creates a new instance of TestDateQueries
     */
    public TestDateQueries() {
    }

    public TestDateQueries(String name) {
        super(name);
    }

    public void setUp()
        throws Exception {
        super.setUp(AllFieldTypesTest.class);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd, yyyy",
            Locale.US);
        _date = sdf.parse("April 26, 1978");
        _before = sdf.parse("April 25, 1978");
        _after = sdf.parse("April 27, 1978");
        _pm = getPM();

        // create some instances to query on
        startTx(_pm);
        AllFieldTypesTest test = new AllFieldTypesTest();
        test.setTestDate(_date);
        _pm.persist(test);

        test = new AllFieldTypesTest();
        test.setTestDate(_before);
        _pm.persist(test);

        test = new AllFieldTypesTest();
        test.setTestDate(_after);
        _pm.persist(test);
        endTx(_pm);
    }

    public void testEquals() {
        Collection vals = executeQuery("testDate = :date");
        assertEquals(1, vals.size());
        assertEquals(_date, ((AllFieldTypesTest) vals.iterator().next()).
            getTestDate());
    }

    public void testNotEquals() {
        Collection vals = executeQuery("testDate <> :date");
        assertEquals(2, vals.size());
    }

    public void testBefore() {
        Collection vals = executeQuery("testDate < :date");
        assertEquals(1, vals.size());
        assertEquals(_before, ((AllFieldTypesTest) vals.iterator().next()).
            getTestDate());
    }

    public void testAfter() {
        Collection vals = executeQuery("testDate > :date");
        assertEquals(1, vals.size());
        assertEquals(_after, ((AllFieldTypesTest) vals.iterator().next()).
            getTestDate());
    }

    public void testOrderBy() {
        String query =
            "SELECT o FROM AllFieldTypesTest o ORDER BY o.testDate ASC";
        OpenJPAQuery q = _pm.createQuery(query);
        List vals = q.getResultList();
        assertEquals(3, vals.size());

        Iterator i = vals.iterator();
        assertEquals(_before, ((AllFieldTypesTest) i.next()).getTestDate());
        assertEquals(_date, ((AllFieldTypesTest) i.next()).getTestDate());
        assertEquals(_after, ((AllFieldTypesTest) i.next()).getTestDate());
    }

    private List executeQuery(String filter) {
        String query = "SELECT o FROM AllFieldTypesTest o WHERE o." + filter;
        OpenJPAQuery q = _pm.createQuery(query);
        q.setParameter("date", _date);
        return q.getResultList();
    }
}

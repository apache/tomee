/*
 * TestSimple.java
 *
 * Created on October 13, 2006, 5:03 PM
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

import java.util.List;



import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;

import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestSimple extends BaseKernelTest {

    /**
     * Creates a new instance of TestSimple
     */
    public TestSimple(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);
    }

    public void testSimple() {
        // test create
        {
            OpenJPAEntityManager pm = getPM();
            startTx(pm);
            pm.persist(new RuntimeTest1("testSimple", 12));
            endTx(pm);
            endEm(pm);
        }

        // test Query
        {
            OpenJPAEntityManager pm = getPM();
            startTx(pm);
            String theQuery = "SELECT r FROM RuntimeTest1 r "
                + "WHERE r.stringField = \'testSimple\'";
            OpenJPAQuery query = pm.createQuery(theQuery);
            List list = query.getResultList();
            assertSize(1, list);
            endTx(pm);
            endEm(pm);
        }

        // test Update
        {
            OpenJPAEntityManager pm = getPM();
            startTx(pm);
            String theQuery = "SELECT r FROM RuntimeTest1 r "
                + "WHERE r.stringField = \'testSimple\'";
            OpenJPAQuery query = pm.createQuery(theQuery);
            RuntimeTest1 toUpdate = (RuntimeTest1) query.getSingleResult();
            toUpdate.setStringField("testSimple2");
            endTx(pm);
            endEm(pm);

            pm = getPM();
            startTx(pm);
            String query1 = "SELECT r FROM RuntimeTest1 r "
                + "WHERE r.stringField = \'testSimple\'";
            String query2 = "SELECT r FROM RuntimeTest1 r "
                + "WHERE r.stringField = \'testSimple2\'";
            OpenJPAQuery q1 = pm.createQuery(query1);
            OpenJPAQuery q2 = pm.createQuery(query2);
            assertSize(0, q1.getResultList());
            assertSize(1, q2.getResultList());
            endTx(pm);
            endEm(pm);
        }

        // test Extent
        {
            OpenJPAEntityManager pm = getPM();
            startTx(pm);
            Extent e = pm.createExtent(RuntimeTest1.class, true);
            assertTrue(e.iterator().hasNext());
            assertEquals("testSimple2", ((RuntimeTest1) e.iterator().next()).
                getStringField());
            endTx(pm);
            endEm(pm);
        }

        // test delete
        {
            OpenJPAEntityManager pm = getPM();
            startTx(pm);
            String delete = "DELETE FROM RuntimeTest1 r "
                + "WHERE r.stringField = \'testSimple2\'";
            OpenJPAQuery deleteQuery = pm.createQuery(delete);
            deleteQuery.executeUpdate();
            endTx(pm);
            endEm(pm);

            pm = getPM();
            startTx(pm);
            String select = "SELECT r FROM RuntimeTest1 r "
                + "WHERE r.stringField = \'testSimple2\'";
            OpenJPAQuery selectQuery = pm.createQuery(select);

            assertSize(0, selectQuery.getResultList());

            endTx(pm);
            endEm(pm);
        }
    }
}

/*
 * TestMathQueries.java
 *
 * Created on October 18, 2006, 1:06 PM
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

import java.util.List;


import org.apache.openjpa.persistence.query.common.apps.QueryTest1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestMathQueries extends BaseQueryTest {

    /**
     * Creates a new instance of TestMathQueries
     */

    public TestMathQueries(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(QueryTest1.class);
        OpenJPAEntityManager pm = getEM();
        startTx(pm);

        for (int i = 0; i <= 100; i++) {
            QueryTest1 ob = new QueryTest1();
            ob.setNum(i);
            pm.persist(ob);
        }
        endTx(pm);

        for (long i = 0; i < 100; i++) {
            OpenJPAQuery q =
                pm.createQuery("SELECT q FROM QueryTest1 q WHERE q.numb = :ind")
                    .setParameter("ind", i);
            List l = q.getResultList();
            assertSize(1, l);
        }
    }

    public void testMultipleQuery() {
        try {

            OpenJPAQuery q1, q2;
            q1 = getEM().createQuery(
                "SELECT q FROM QueryTest1 q WHERE q.numb * q.numb = 25");
            q2 = getEM().createQuery(
                "SELECT q FROM QueryTest1 q WHERE q.numb * q.numb > 25");

            assertSize(95, q2.getResultList());
            assertSize(1, q1.getResultList());
        }
        catch (Exception e) {
            bug(AbstractTestCase.Platform.EMPRESS, 890, e,
                "Empress cannot handle certain "
                    + "aggregate functions");
        }
    }
}

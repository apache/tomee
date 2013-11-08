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


import org.apache.openjpa.persistence.query.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestJPAQuerySubclassFunc extends BaseQueryTest {

    public TestJPAQuerySubclassFunc(String name) {
        super(name);
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);

        OpenJPAEntityManager pm = getEM();
        startTx(pm);

        pm.persist(new RuntimeTest1("TestQueryResults1", 10));
        pm.persist(new RuntimeTest1("TestQueryResults3", 10));
        pm.persist(new RuntimeTest1("TestQueryResults5", 10));

        pm.persist(new RuntimeTest3("TestQueryResults2", 10));
        pm.persist(new RuntimeTest3("TestQueryResults4", 10));
        pm.persist(new RuntimeTest3("TestQueryResults6", 10));

        endTx(pm);
        endEm(pm);
    }

    public void testSubClassIsTrueFunction() {
        OpenJPAEntityManager pm = getEM();
        startTx(pm);

        OpenJPAQuery query =
            pm.createQuery("SELECT r.stringField FROM RuntimeTest1 r");
        query.setSubclasses(true);

        List list = query.getResultList();

        assertEquals("I expect size to be 6 since subclasses are included", 6,
            list.size());
        assertTrue(list.contains("TestQueryResults1"));
        assertTrue(list.contains("TestQueryResults2"));

        endTx(pm);
        endEm(pm);
    }

    public void testSubClassIsFalseFunction() {
        OpenJPAEntityManager pm = getEM();
        startTx(pm);

        OpenJPAQuery query =
            pm.createQuery("SELECT r.stringField FROM RuntimeTest1 r");
        query.setSubclasses(false);

        List list = query.getResultList();

        assertEquals("I expect size to be 3 since subclasses are not included",
            3, list.size());
        assertTrue(list.contains("TestQueryResults1"));
        assertTrue(list.contains("TestQueryResults3"));
        assertTrue(list.contains("TestQueryResults5"));

        endTx(pm);
        endEm(pm);
    }
}

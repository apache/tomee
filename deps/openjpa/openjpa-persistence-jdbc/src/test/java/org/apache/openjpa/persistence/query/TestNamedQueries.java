/*
 * TestNamedQueries.java
 *
 * Created on October 18, 2006, 1:17 PM
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

import javax.persistence.TypedQuery;



import org.apache.openjpa.persistence.query.common.apps.EntityInterface;
import org.apache.openjpa.persistence.query.common.apps.QueryTest1;

import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAQuery;

public class TestNamedQueries extends BaseQueryTest {

    /**
     * Creates a new instance of TestNamedQueries
     */

    public TestNamedQueries(String test) {
        super(test);
    }

    public void setUp() {
        deleteAll(QueryTest1.class);

        OpenJPAEntityManager pm = getEM();
        startTx(pm);
        QueryTest1 pc = null;
        for (int i = 0; i < 10; i++) {
            pc = new QueryTest1();
            if (i < 5)
                pc.setNum(4);
            else
                pc.setNum(i + 10);
            pm.persist(pc);
        }
        endTx(pm);
        endEm(pm);
    }

    public void testNamedClassQuery() {
        OpenJPAEntityManager pm = getEM();

        OpenJPAQuery query = pm.createQuery("SELECT o FROM QueryTest1 o");
        query.setResultClass(QueryTest1.class);

        assertEquals(QueryTest1.class, query.getResultClass());
        assertEquals("SELECT o FROM QueryTest1 o", query.getQueryString());
        assertEquals(QueryTest1.class, query.getResultClass());
        //FIXME jthomas
        /*
        assertEquals("import java.util.*", query.getImports());
        assertEquals("int pnum", query.getParameters());
        assertEquals("QueryTest4 v4", query.getVariables());
        assertEquals("sum(num)", query.getResult());
        assertEquals("num", query.getGrouping());
        */
        QueryMetaData meta = ((OpenJPAEntityManagerSPI) pm).getConfiguration().
            getMetaDataRepositoryInstance().getQueryMetaData(null,
            "named", pm.getClassLoader(), true);
        assertNotNull(meta);
        assertEquals("SELECT o FROM QueryTest1 o", meta.getQueryString());
        assertEquals(null, meta.getResultType());

        endEm(pm);
    }

    public void testNamespace() {
        OpenJPAEntityManager pm = getEM();
        OpenJPAQuery query = (OpenJPAQuery) pm.createNamedQuery("named");
        assertEquals("SELECT o FROM QueryTest1 o", query.getQueryString());
        query.closeAll();
        endEm(pm);
    }

    public void testSystemJDOQL() {
        // make sure local query metadata is parsed
        OpenJPAEntityManager pm = getEM();

        OpenJPAQuery query = (OpenJPAQuery) pm.createNamedQuery("named");
        assertEquals("SELECT o FROM QueryTest1 o", query.getQueryString());
        assertEquals(QueryTest1.class, query.getResultClass());
        assertEquals(10, ((Collection) query.getResultList()).size());
        query.closeAll();
        endEm(pm);
    }
    
    public void testInterfaceResultClass() {
        OpenJPAEntityManager pm = getEM();

        TypedQuery<EntityInterface> query = pm.createNamedQuery("named", EntityInterface.class);
        assertEquals(10, query.getResultList().size());
    }
}

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
package org.apache.openjpa.persistence.jdbc.meta;

import java.util.*;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAQuery;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestRangeQuery extends 
        org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {

    /** Creates a new instance of TestRangeQuery */
    public TestRangeQuery(String name) {
        super(name);
    }

    public boolean skipTest() {
        DBDictionary dict = ((JDBCConfiguration) getConfiguration()).
                getDBDictionaryInstance();
        return !dict.supportsSubselect;
    }

    public void setUp() {

       deleteAll(HelperPC.class);
       deleteAll(EagerOuterJoinPC2.class);
       deleteAll(EagerOuterJoinPC.class);
    }

    public void testQueryRange() {
        insertManyStringList();

        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
        FetchPlan fetch = (FetchPlan) em.getFetchPlan();
        fetch.addField(EagerOuterJoinPC.class, "stringList");
        fetch.setFetchBatchSize(3);
        OpenJPAQuery q = em.createQuery(JPQLParser.LANG_JPQL,
                "select x from EagerOuterJoinPC x order by x.name asc");
        q.setFirstResult(5).setMaxResults(15);

        List results = (List) q.getResultList();

        for (int i = 0; i < results.size(); i++) {
            EagerOuterJoinPC pc = (EagerOuterJoinPC) results.get(i);
            assertEquals(String.valueOf(i + 5), pc.getName());
            System.err.println("Row " + i + " " + pc.getName());
        }
        assertEquals(5, results.size());
        q.closeAll();
        em.close();
    }

    @SuppressWarnings("unchecked")
    private void insertManyStringList() {
        OpenJPAEntityManager em =(OpenJPAEntityManager)currentEntityManager();
        startTx(em);;
        for (int i = 0; i < 10; i++) {
            EagerOuterJoinPC pc = new EagerOuterJoinPC();
            pc.setName(String.valueOf(i));
            pc.getStringList().add(i + ".1");
            pc.getStringList().add(i + ".2");
            em.persist(pc);
        }
        endTx(em);;
        em.close();
    }
}

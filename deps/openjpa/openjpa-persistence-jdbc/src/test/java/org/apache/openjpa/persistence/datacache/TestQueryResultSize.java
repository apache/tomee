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
package org.apache.openjpa.persistence.datacache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.datacache.ConcurrentQueryCache;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectE;

public class TestQueryResultSize
    extends AbstractTestCase {

    public TestQueryResultSize(String test) {
        super(test, "datacachecactusapp");
    }

    private EntityManagerFactory _pmf;
    private OpenJPAEntityManager pm;

    public void setUp() {
        System.out.println("****Deleted Records "
            + deleteAll(CacheObjectE.class));
        deleteAll(CascadeParent.class);
        deleteAll(CascadeChild.class);
        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.QueryCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        _pmf = getEmf(propsMap);
    }

    public void test() {
        CacheObjectE pc1 = new CacheObjectE();
        pc1.setStr("pc1");

        pm = (OpenJPAEntityManager) _pmf.createEntityManager();

        startTx(pm);
        pm.persist(pc1);
        endTx(pm);

        pm.getFetchPlan().setQueryResultCacheEnabled(false);
        OpenJPAQuery q = pm.createQuery(
            "select a FROM " + CacheObjectE.class.getSimpleName() +
                " a where a.str = 'pc1'");
        List res = (List) q.getResultList();
        assertEquals(0, getQueryCacheSize());
        endEm(pm);

        System.out.println("****Deleted Records " + 
            deleteAll(CacheObjectE.class));
    }

    private int getQueryCacheSize() {
        return ( ((ConcurrentQueryCache)(OpenJPAPersistence.cast(
            pm.getEntityManagerFactory()).getQueryResultCache().getDelegate())).
            getCacheMap().size());
    }

    public void testCrossJoinQueryCache() {
        pm = (OpenJPAEntityManager) _pmf.createEntityManager();
        // create
        startTx(pm);
        CascadeParent p = new CascadeParent();
        p.setName("p1");
        CascadeChild c = new CascadeChild();
        c.setName("p1");
        p.setChild(c);
        pm.persist(p);
        endTx(pm);

        // query
        String jpql = "select p.name, c.name from CascadeParent p, CascadeChild c where p.name = c.name "
                + "and p.name = 'p1'";
        javax.persistence.Query query = pm.createQuery(jpql);
        List result1 = query.getResultList();
        assertEquals(1, result1.size());

        // update
        startTx(pm);
        c.setName("c1");
        endTx(pm);

        // query again
        List result2 = query.getResultList();
        assertEquals(0, result2.size());
        endEm(pm);
    }


}

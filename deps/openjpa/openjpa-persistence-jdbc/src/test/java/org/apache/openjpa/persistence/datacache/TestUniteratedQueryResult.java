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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectA;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAQuery;

@AllowFailure(message="surefire excluded")
public class TestUniteratedQueryResult
    extends AbstractTestCase {

    public TestUniteratedQueryResult(String test) {
        super(test, "datacachecactusapp");
    }

    private EntityManagerFactory _pmf;

    public void setUp() {
        System.out.println("****Deleted Records "
            + deleteAll(CacheObjectA.class));
        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.QueryCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.FetchBatchSize", "0");

        _pmf = getEmf(propsMap);
    }

    public void test() {
        CacheObjectA pc1 = new CacheObjectA();
        pc1.setName("pc1");
        CacheObjectA pc2 = new CacheObjectA();
        pc2.setName("pc2");
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) _pmf.createEntityManager();
        startTx(pm);
        pm.persist(pc1);
        pm.persist(pc2);
        endTx(pm);
        Object oid1 = pm.getObjectId(pc1);
        Object oid2 = pm.getObjectId(pc2);
        endEm(pm);

        pm = (OpenJPAEntityManager) _pmf.createEntityManager();

        OpenJPAQuery q = pm.createQuery(
            "select a FROM " + CacheObjectA.class.getSimpleName() +
                " a where a.name = 'pc1'");
        List res = (List) q.getResultList();

        OpenJPAEntityManager pm2 =
            (OpenJPAEntityManager) _pmf.createEntityManager();
        startTx(pm2);
        pc1 = (CacheObjectA) pm2.find(CacheObjectA.class, oid1);
        pc2 = (CacheObjectA) pm2.find(CacheObjectA.class, oid2);
        pc1.setName("pc2");
        pc2.setName("pc1");
        endTx(pm2);

        assertEquals(1, res.size());
        for (Iterator itr = res.iterator(); itr.hasNext();)
            assertEquals(oid1, pm2.getObjectId(itr.next()));
        endEm(pm2);
        endEm(pm);

        pm = (OpenJPAEntityManager) _pmf.createEntityManager();

        q = pm.createQuery(
            "select a FROM " + CacheObjectA.class.getSimpleName() +
                " a where a.name = 'pc1'");
        res = (List) q.getResultList();

        assertEquals(oid2, pm.getObjectId(res.iterator().next()));
        endEm(pm);
    }
}

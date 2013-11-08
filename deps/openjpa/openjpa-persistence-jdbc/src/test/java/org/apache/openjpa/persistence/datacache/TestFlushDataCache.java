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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.datacache.common.apps.
        FlushDataCacheObject;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

@AllowFailure(message="surefire excluded")
public class TestFlushDataCache
    extends AbstractTestCase {

    public TestFlushDataCache(String str) {
        super(str, "datacachecactusapp");
    }

    public void setUp() {
        deleteAll(FlushDataCacheObject.class);
    }

    public void testQueryFlushPlusDataCache() {
        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.FlushBeforeQueries", "true");
        //propsMap.put("javax.jdo.option.IgnoreCache", "false");
        //propsMap.put("openjpa.BrokerImpl", "kodo.datacache.CacheTestBroker");
        //CacheTestBroker.class.getName ());
        EntityManagerFactory emf = getEmf(propsMap);

        try {

            //assertEquals(Class.forName("openjpa.datacache.CacheTestBroker",
            //    true,emf.getClass().getClassLoader()).getClassLoader(),
            //    emf.getClass().getClassLoader());
            //Thread.currentThread().setContextClassLoader(
            //    emf.getClass().getClassLoader());
            Class.forName(
                    "org.apache.openjpa.persistence.datacache.CacheTestBroker",
                    true,
                Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {

        }

        EntityManager em = emf.createEntityManager();
        startTx(em);
        FlushDataCacheObject o = new FlushDataCacheObject();
        o.setString("foo");
        em.persist(o);
        endTx(em);
        //Object oid = JDOHelper.getObjectId (o);
        endEm(em);

        em = emf.createEntityManager();
        startTx(em);

        Collection c = (Collection) em.createQuery(
            "select a FROM " + FlushDataCacheObject.class.getSimpleName() +
                " a where a.string = 'foo'").getResultList();

        assertEquals(1, c.size());
        em.remove(c.iterator().next());

        c = (Collection) em.createQuery(
            "select a FROM " + FlushDataCacheObject.class.getSimpleName() +
                " a where a.string = 'foo'").getResultList();
        assertEquals(0, c.size());

        endTx(em);
        endEm(em);
    }

    public void testEmptyCommit() {
        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.FlushBeforeQueries", "true");
        //propsMap.put("javax.jdo.option.IgnoreCache", "false");
        propsMap.put("openjpa.BrokerImpl", CacheTestBroker.class.getName());
        EntityManagerFactory emf = getEmf(propsMap);
        EntityManager em = emf.createEntityManager();
        startTx(em);
        endTx(em);
        endEm(em);
    }
}

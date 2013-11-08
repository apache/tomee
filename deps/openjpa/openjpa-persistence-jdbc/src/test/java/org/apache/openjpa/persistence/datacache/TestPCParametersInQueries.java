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
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectA;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectE;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectJ;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.JPAFacadeHelper;

/**
 * New test case.
 */
@AllowFailure(message="surefire excluded")
public class TestPCParametersInQueries extends AbstractTestCase {

    public TestPCParametersInQueries(String test) {
        super(test, "datacachecactusapp");
    }

    private BrokerFactory _factory;
    private Object _eId;

    public void setUp() {
        deleteAll(CacheObjectA.class);

        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.BrokerImpl", CacheTestBroker.class.getName());
        EntityManagerFactory emf = getEmf(propsMap);
        _factory = JPAFacadeHelper.toBrokerFactory(emf);

        Broker broker = _factory.newBroker();
        broker.begin();
        CacheObjectE e = new CacheObjectE("bar");
        CacheObjectJ j = new CacheObjectJ("foo", e);
        broker.persist(j, null);
        broker.persist(e, null);
        broker.commit();
        _eId = broker.getObjectId(e);
        broker.close();
    }

    public void testPCParameter() {
        Broker broker = _factory.newBroker();
        broker.begin();
        CacheObjectE e = (CacheObjectE) broker.find(_eId, true, null);
        Query q = broker.newQuery(JPQLParser.LANG_JPQL,
            "select a from " +
                CacheObjectJ.class.getSimpleName() + " a where a.e = :param");

        Collection c = (Collection) q.execute(new Object[]{ e });
        CacheTestHelper.iterate(c);
        Object o = c.iterator().next();
        assertTrue(o instanceof CacheObjectJ);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE, new Object[]{ e });

        q = broker.newQuery(JPQLParser.LANG_JPQL,
            "select a from " +
                CacheObjectJ.class.getSimpleName() + " a where a.e = :param");

        c = (Collection) q.execute(new Object[]{ e });
        Object o2 = c.iterator().next();
        assertTrue(o2 instanceof CacheObjectJ);
        assertTrue(o == o2);

        broker.commit();
        broker.close();
    }
}

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
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectAChild1;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.JPAFacadeHelper;

@AllowFailure(message="surefire excluded")
public class TestJPQL2Queries
    extends AbstractTestCase {

    public TestJPQL2Queries(String test) {
        super(test, "datacachecactusapp");
    }

    private BrokerFactory _factory;

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
        for (int i = 0; i < 50; i++)
            broker.persist(new CacheObjectAChild1("", "JPQL2Queries", i),
                null);
        broker.commit();
        broker.close();

        CacheTestHelper.cacheManager(_factory).getSystemQueryCache().clear();
    }

    public void tearDown()
        throws Exception {
        try {
            _factory.close();
            _factory = null;
        } catch (Exception e) {
        }

        super.tearDown();
    }

    public void testUpperRange() {
        rangeTestHelper(0, 10);
    }

    public void testLowerRange() {
        rangeTestHelper(5, 50);
    }

    public void testBothRanges() {
        rangeTestHelper(20, 28);
    }

    private void rangeTestHelper(final int start, final int end) {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        q.setRange(start, end);

        // should not yet be in the cache
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE);
        Collection c = (Collection) q.execute();

        // iterate the results. This will cause the query to be
        // enlisted in the cache.
        CacheTestHelper.iterate(c);
        assertEquals(end - start, c.size());
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        broker.close();

        broker = _factory.newBroker();
        q = broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        q.setRange(start, end);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);
        c = (Collection) q.execute();
        assertEquals(end - start, c.size());

        // now check if a smaller range is in cache
        q = broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        q.setRange(start, end - 1);
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE);
        c = (Collection) q.execute();
        assertEquals(end - start - 1, c.size());
        broker.close();
    }

    public void testResultClassIsCached() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        q.setResultType(Object.class);
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);
    }
}

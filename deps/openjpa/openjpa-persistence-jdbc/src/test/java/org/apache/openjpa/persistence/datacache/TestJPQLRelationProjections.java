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
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectAChild1;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectE;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectJ;
import org.apache.openjpa.persistence.datacache.common.apps.
        SelfReferencingCacheTestObject;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.util.UserException;

@AllowFailure(message="surefire excluded")
public class TestJPQLRelationProjections
    extends AbstractTestCase {

    public TestJPQLRelationProjections(String test) {
        super(test, "datacachecactusapp");
    }

    private BrokerFactory _factory;

    public void setUp() {
        deleteAll(CacheObjectJ.class);
        deleteAll(CacheObjectE.class);
        deleteAll(SelfReferencingCacheTestObject.class);

        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.BrokerImpl", CacheTestBroker.class.getName());

        EntityManagerFactory emf = getEmf(propsMap);
        _factory = JPAFacadeHelper.toBrokerFactory(emf);
        Broker broker = _factory.newBroker();
        try {
            broker.begin();
        } catch (Exception e) {
            fail("Set up failed due to exception : \n" +
                getStackTrace(e));
        }
        int j = 0;
        for (int i = 0; i < 6; i++) {
            CacheObjectE e;
            if (i < 3)
                e = new CacheObjectE(i + "");
            else
                e = null;

            // make some common names so that GROUP BY is useful.
            if (i % 2 == 0)
                j++;
            broker.persist(new CacheObjectJ("projections-" + j, e), null);
        }

        broker.persist(new SelfReferencingCacheTestObject("foo",
            new SelfReferencingCacheTestObject("bar", null)), null);

        broker.commit();
        broker.close();

        CacheTestHelper.cacheManager(_factory).getSystemQueryCache().clear();
    }

    public void tearDown()
        throws Exception {
        _factory.close();
        _factory = null;
        super.tearDown();
    }

    public void testNullRelatedPCIsCached() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.e FROM " +
            CacheObjectJ.class.getSimpleName() + " a where a.e is null");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        assertNull(c.iterator().next());
    }

    public void testNullRelatedPCAndProjectionIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.str,a.e FROM " +
                CacheObjectJ.class.getSimpleName() + " a where a.e is null");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object[] result = (Object[]) c.iterator().next();
        assertEquals(2, result.length);
        assertEquals(String.class, result[0].getClass());
        assertNull(result[1]);
    }

    public void testNonNullRelatedPCIsCached() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.e FROM " +
            CacheObjectJ.class.getSimpleName() + " a where a.e  is not null");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        assertEquals(CacheObjectE.class, c.iterator().next().getClass());
    }

    public void testNonNullRelatedPCAndProjectionIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.str,a.e FROM " +
                CacheObjectJ.class.getSimpleName() +
                " a where a.e is not null");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object[] result = (Object[]) c.iterator().next();
        assertEquals(2, result.length);
        assertEquals(String.class, result[0].getClass());
        assertEquals(CacheObjectE.class, result[1].getClass());
    }

    public void testEmbeddedFields() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select embedded FROM " +
                SelfReferencingCacheTestObject.class.getSimpleName() +
                " a where a.str='foo'");

        List l = null;
        try {
            l = (List) q.execute();
            assertEquals(CacheObjectAChild1.class, l.get(0).getClass());
        } catch (UserException ue) {
            //bug(1150, "embedded-field projections cause exception");
            ue.printStackTrace();
            return;
        }

        CacheTestHelper.iterate(l);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        l = (List) q.execute();
        assertEquals(CacheObjectAChild1.class, l.get(0).getClass());
    }

    public void testNonNullRelationOfSameTypeAsCandidate() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.o FROM " +
            SelfReferencingCacheTestObject.class.getSimpleName() +
            " a where a.o IS NOT NULL");

        List l = (List) q.execute();
        assertEquals(SelfReferencingCacheTestObject.class,
            l.get(0).getClass());
        assertEquals("bar",
            ((SelfReferencingCacheTestObject) l.get(0)).getStr());
        CacheTestHelper.iterate(l);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        l = (List) q.execute();
        assertEquals(SelfReferencingCacheTestObject.class,
            l.get(0).getClass());
        assertEquals("bar",
            ((SelfReferencingCacheTestObject) l.get(0)).getStr());
    }

    public void testNullRelationOfSameTypeAsCandidate() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.o FROM " +
            SelfReferencingCacheTestObject.class.getSimpleName() +
            " a where a.o IS NULL");

        List l = (List) q.execute();
        assertNull(l.get(0));
        CacheTestHelper.iterate(l);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        l = (List) q.execute();
        assertNull(l.get(0));
    }
}

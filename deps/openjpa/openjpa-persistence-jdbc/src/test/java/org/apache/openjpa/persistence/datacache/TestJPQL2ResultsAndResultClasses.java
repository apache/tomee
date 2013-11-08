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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectA;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectAChild1;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectB;
import org.apache.openjpa.persistence.datacache.common.apps.
        CacheObjectWithExternalizedFields;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.JPAFacadeHelper;

@AllowFailure(message="surefire excluded")
public class TestJPQL2ResultsAndResultClasses
    extends AbstractTestCase {

    public TestJPQL2ResultsAndResultClasses(String test) {
        super(test, "datacachecactusapp");
    }

    private BrokerFactory _factory;

    public void setUp() {
        deleteAll(CacheObjectA.class);
        deleteAll(CacheObjectWithExternalizedFields.class);

        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.BrokerImpl", CacheTestBroker.class.getName());
        EntityManagerFactory emf =
            getEmf(propsMap);
        _factory = JPAFacadeHelper.toBrokerFactory(emf);
        Broker broker = _factory.newBroker();
        try {
            broker.begin();
        } catch (Exception e) {
            System.out.println(
                "Exception in TestJPQL2ResultsAndResultClasses setup : \n" +
                    getStackTrace(e));
        }

        int j = 0;
        for (int i = 0; i < 10; i++) {
            // make some common names so that GROUP BY is useful.
            if (i % 2 == 0)
                j++;
            CacheObjectA o;
            broker.persist(o = new CacheObjectAChild1("", "results-" + j, i),
                null);

            o.setDate(new Date());
            o.setDateArray(new Date[]{ new Date(10), new Date(20) });

            if (i < 5)
                o.setRelatedB(new CacheObjectB());
        }

        //Seetha Oct 30,2006
        //deleteAll closes the TX.  So use the local
        //deleteAll fn.
        //deleteAll(CacheObjectWithExternalizedFields.class,());
        //deleteAll(broker,CacheObjectWithExternalizedFields.class,true);

        CacheObjectWithExternalizedFields o =
            new CacheObjectWithExternalizedFields();
        broker.persist(o, null);
        o.setCls(Broker.class);

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

    public void testAggregateResultIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select max(a.age) FROM " +
                CacheObjectAChild1.class.getSimpleName() + " a");

        Object o = q.execute();
        assertEquals(Long.class, o.getClass());
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        o = q.execute();
        assertEquals(Long.class, o.getClass());
    }

    public void testAggregateNonUniqueResultIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select max(a.age) FROM " +
                CacheObjectAChild1.class.getSimpleName() + " a");
        q.setUnique(false);
        List res = (List) q.execute();
        assertEquals(1, res.size());
        Object o = res.get(0);
        assertEquals(Long.class, o.getClass());
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        res = (List) q.execute();
        assertEquals(1, res.size());
        o = res.get(0);
        assertEquals(Long.class, o.getClass());
    }

    public void testProjectionResultIsCached() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.age FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object o = c.iterator().next();
        assertEquals(Long.class, o.getClass());
    }

    public void testProjectionOfThisIsCached() {
        // ##### need to test single projection
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        //CacheObjectAChild1.class, "select this");
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object o = c.iterator().next();
        assertEquals(CacheObjectAChild1.class, o.getClass());
        assertNotNull(broker.getObjectId(o));
    }

    public void testProjectionResultWithThisIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.name,a FROM " +
                CacheObjectAChild1.class.getSimpleName() + " a");
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object[] result = (Object[]) c.iterator().next();
        assertEquals(2, result.length);
        assertEquals(String.class, result[0].getClass());
        assertEquals(CacheObjectAChild1.class, result[1].getClass());
        assertNotNull(broker.getObjectId(result[1]));
    }

    public void testNullProjectionValues() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.locale FROM " +
                CacheObjectAChild1.class.getSimpleName() + " a");
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        assertNull(c.iterator().next());
    }

    public void testNullAndNotNullProjectionValues() {
        Broker broker = _factory.newBroker();
        Query q =
            broker
                .newQuery(JPQLParser.LANG_JPQL, "select a.name,a.locale FROM " +
                    CacheObjectAChild1.class.getSimpleName() + " a");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object[] result = (Object[]) c.iterator().next();
        assertEquals(2, result.length);
        assertEquals(String.class, result[0].getClass());
        assertNull(result[1]);
    }

    public void XXXtestNullAggregateValues() {
        // ???
    }

    public void testMultipleAggregateResultIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL,
                "select max(a.age), avg(a.age), count(a.age) FROM " +
                    CacheObjectAChild1.class.getSimpleName() + " a");

        Object o = q.execute();
        assertEquals(Object[].class, o.getClass());
        assertEquals(3, ((Object[]) o).length);
        assertEquals(Long.class, ((Object[]) o)[0].getClass());

        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        o = q.execute();
        assertEquals(Object[].class, o.getClass());
        assertEquals(3, ((Object[]) o).length);
        assertEquals(Long.class, ((Object[]) o)[0].getClass());
    }

    public void testFieldUsedTwice() {
        // Postgres bug
        Broker broker = _factory.newBroker();
        // group avoids postgres bug
        Query q = broker
            .newQuery(JPQLParser.LANG_JPQL, "select a.age, avg(a.age) FROM " +
                CacheObjectAChild1.class.getSimpleName() +
                " a  group by a.age");
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);
    }

    public void testAggregateAndProjection() {
        // Postgres bug
        Broker broker = _factory.newBroker();
        // group avoids postgres bug
        Query q = broker
            .newQuery(JPQLParser.LANG_JPQL, "select a.name, avg(a.age) FROM " +
                CacheObjectAChild1.class.getSimpleName() +
                " a  group by a.name");

        List l = (List) q.execute();
        CacheTestHelper.iterate(l);
        assertEquals(Object[].class, l.get(0).getClass());
        assertEquals(2, ((Object[]) l.get(0)).length);
        assertEquals(String.class, ((Object[]) l.get(0))[0].getClass());

        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        l = (List) q.execute();
        assertEquals(Object[].class, l.get(0).getClass());
        assertEquals(2, ((Object[]) l.get(0)).length);
        assertEquals(String.class, ((Object[]) l.get(0))[0].getClass());
    }

    //FIXME Seetha Dec 19,2006
    /*public void testMath() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL,"select avg(a.age) FROM "+
                    CacheObjectAChild1.class.getSimpleName()+" a");
        Number n = (Number) q.execute();
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);
        n = (Number) q.execute();
    }*/

    public void testResultClassIsCached() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a");
        q.setResultType(Object.class);
        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);
    }

    public void testGroupingIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select max(a.age) FROM " +
                CacheObjectAChild1.class.getSimpleName() +
                " a  group by a.name");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object o = c.iterator().next();
        assertEquals(Long.class, o.getClass());
    }

    public void testAggregateProjectionGroupingIsCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL,
                "select a.name, max(a.age) FROM " +
                    CacheObjectAChild1.class.getSimpleName() +
                    " a  group by a.name");

        Collection c = (Collection) q.execute();
        CacheTestHelper.iterate(c);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        c = (Collection) q.execute();
        Object[] result = (Object[]) c.iterator().next();
        assertEquals(2, result.length);
        assertEquals(String.class, result[0].getClass());
        assertEquals(Long.class, result[1].getClass());
    }

    public void testUniqueResultsAreCachedAndConsistent() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a FROM " +
                CacheObjectAChild1.class.getSimpleName() +
                " a  where a.age =1");

        q.setUnique(true);
        CacheObjectAChild1 a = (CacheObjectAChild1) q.execute();
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        CacheObjectAChild1 a2 = (CacheObjectAChild1) q.execute();
        assertTrue(a == a2);
    }

    public void testMutableProjectionFieldsAreCopied() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.date FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a  where a.age=1");

        q.setUnique(true);
        Date d0 = (Date) q.execute(); // get it in cache
        Date d1 = (Date) q.execute();
        assertNotSame(d0, d1);

        Date d2 = (Date) q.execute();
        assertNotSame(d1, d2);
    }

    public void testArrayProjectionFieldsAreNotCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.dateArray FROM " +
                CacheObjectAChild1.class.getSimpleName() + " a");

        try {
            q.execute();
            fail("Allowed array projection query.");
        } catch (Exception e) {
        }
    }

    public void testCollectionProjectionFieldsAreNotCached() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.stringColl FROM " +
                CacheObjectAChild1.class.getSimpleName() + " a");

        try {
            q.execute();
            fail("Allowed array projection query.");
        } catch (Exception e) {
        }
    }

    public void testExternalizedSingleValueFieldIsNotCached() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a.cls FROM " +
            CacheObjectWithExternalizedFields.class.getSimpleName() + " a");

        q.setUnique(true);
        Object o = q.execute(); // get it in cache
        // ##### assertEquals (Class.class, o);
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE);
    }

    public void testMutatedQueryReturnsNewResults() {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL,
                "select a.name, max(a.age) FROM " +
                    CacheObjectAChild1.class.getSimpleName() +
                    " a group by a.name");

        List l = (List) q.execute();
        CacheTestHelper.iterate(l);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        l = (List) q.execute();
        Object[] result = (Object[]) l.get(0);
        assertEquals(2, result.length);
        assertEquals(String.class, result[0].getClass());
        assertEquals(Long.class, result[1].getClass());

        // now, mutate the query and see what happens
        q.setQuery("select max(a.age),a.name FROM " +
            CacheObjectAChild1.class.getSimpleName() + " a group by a.name");
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE);
        l = (List) q.execute();
        result = (Object[]) l.get(0);
        assertEquals(2, result.length);
        assertEquals(Long.class, result[0].getClass());
        assertEquals(String.class, result[1].getClass());
        CacheTestHelper.iterate(l);
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE);

        l = (List) q.execute();
        result = (Object[]) l.get(0);
        assertEquals(2, result.length);
        assertEquals(Long.class, result[0].getClass());
        assertEquals(String.class, result[1].getClass());
    }

    public void XXXtestExternalizedContainerFieldIsExternalized() {
    }

    public void XXXtestSerializedSingleValueFieldIsSerialized() {
    }

    public void XXXtestSerializedContainerFieldIsSerialized() {
    }

    public void XXXtestCustomMappedSingleValueFieldIsHandled() {
    }

    public void XXXtestCustomMappedContainerFieldIsHandled() {
    }

    private static int deleteAll(Broker broker, Class clazz,
        boolean subclasses) {
        final boolean useDeleteByQuery = false;

        if (useDeleteByQuery) {
            org.apache.openjpa.kernel.Query query =
                broker.newQuery(JPQLParser.LANG_JPQL, "Select a FROM " +
                    clazz.getSimpleName() + " a");
            query.setCandidateType(clazz, subclasses);
            return (int) query.deleteAll();
        } else {
            org.apache.openjpa.kernel.Extent extent =
                broker.newExtent(clazz, subclasses);
            List list = extent.list();
            int size = list.size();
            broker.deleteAll(list, null);
            return size;
        }
    }
}

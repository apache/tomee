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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectAChild1;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.JPAFacadeHelper;

/**
 * Test that we marshall and unmarshall result types appropriately.
 */
@AllowFailure(message="surefire excluded")
public class TestResultShapes extends AbstractTestCase {

    public TestResultShapes(String test) {
        super(test, "datacachecactusapp");
    }

    private Broker _broker;

    public void setUp() {
        deleteAll(CacheObjectAChild1.class);

        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        EntityManagerFactory emf = getEmf(propsMap);
        BrokerFactory factory = JPAFacadeHelper.toBrokerFactory(emf);

        Broker broker = factory.newBroker();
        broker.begin();
        broker.persist(new CacheObjectAChild1("foo", "foo", 0), null);
        broker.persist(new CacheObjectAChild1("bar", "bar", 1), null);
        broker.commit();
        broker.close();

        _broker = factory.newBroker();
    }

    public void tearDown() throws Exception {
        _broker.close();
        _broker = null;
        super.tearDown();
    }

    // FIXME Seetha Nov 3,2006
    // need to know about the 'this' parameter

//	public void testCollectionOfCandidateClass() 
//	{ 
//		Collection<String> ac = new ArrayList<String>();
//		ac.add("this");
//
//		Collection<Class> bd = new ArrayList<Class>();
//		bd.add(CacheObjectAChild1.class);
//		
//		arrayHelper(false, bd, null, true); 
//		mapHelper(false, bd, ac, true);
//		rawHelper(false, CacheObjectAChild1.class, null, true); 
//	}
//	
//	public void testUniqueCandidateClass() 
//	{ 
//		Collection<String> ac = new ArrayList<String>();
//		ac.add("this");
//
//		Collection<Class> bd = new ArrayList<Class>();
//		bd.add(CacheObjectAChild1.class);
//		
//		arrayHelper(true, bd, null, true); 
//		mapHelper(true, bd, ac, true);
//		rawHelper(true, CacheObjectAChild1.class, null, true); 
//	}

    public void testCollectionOfSingleValuedProjection() {

        Collection<String> ac = new ArrayList<String>();
        ac.add("age");

        Collection<Class> bd = new ArrayList<Class>();
        bd.add(Long.class);

        arrayHelper(false, bd, ac, true);
        mapHelper(false, bd, ac, true);
        rawHelper(false, Long.class, "age", true);
    }

    public void testUniqueSingleValuedProjection() {
        Collection<String> ac = new ArrayList<String>();
        ac.add("age");

        Collection<Class> bd = new ArrayList<Class>();
        bd.add(Long.class);

        arrayHelper(true, bd, ac, true);
        mapHelper(true, bd, ac, true);
        rawHelper(true, Long.class, "age", true);
    }

    public void testCollectionOfMultiValuedProjection() {
        Collection<String> ac = new ArrayList<String>();
        ac.add("age");
        ac.add("name");

        Collection<Class> bd = new ArrayList<Class>();
        bd.add(Long.class);
        bd.add(String.class);

        arrayHelper(false, bd, ac, true);

        mapHelper(false, bd, ac, true);
        // we put both
        // projections in single slot in array because this is a
        // single result that we're
        // looking
        // for. It just happens that the // single result format we expect is
        // an Object[]. rawHelper(false, Object[].class, "age, name", true);
    }

    public void testUniqueMultiValuedProjection() {
        Collection<String> ac = new ArrayList<String>();
        ac.add("age");
        ac.add("name");

        Collection<Class> bd = new ArrayList<Class>();
        bd.add(Long.class);
        bd.add(String.class);

        arrayHelper(true, bd, ac, true);
        mapHelper(true, bd, ac, true);
        // we put
        // both
        // projections in single slot in array because this is a
        // single result that
        // we're looking
        // for.	It just happens that the // single result format we
        // expect is an	Object[]. rawHelper(true, Object[].class, "age, name",
        //     true);
    }

    public void testUncachedQueryHasCorrectShape() {
        Collection<String> ac = new ArrayList<String>();
        ac.add("age");
        ac.add("name");

        Collection<Class> bd = new ArrayList<Class>();
        bd.add(Long.class);
        bd.add(String.class);

        _broker.getFetchConfiguration().setQueryCacheEnabled(false);

        arrayHelper(false, bd, ac, false);
        mapHelper(false, bd, ac, false);
        rawHelper(false, Object[].class, "age, a.name", false);
    }

    private void arrayHelper(boolean unique, Collection recordClasses,
        Collection results, boolean inCache) {
        Query q = setUpQuery(unique, results);
        q.setResultType(Object[].class);
        if (unique) {
            arrayResultHelper(q.execute(), recordClasses, inCache);
        } else {
            for (Iterator iter = ((List) q.execute()).iterator(); iter
                .hasNext();)
                arrayResultHelper(iter.next(), recordClasses, inCache);
        }

        CacheTestHelper.assertInCache(this, q, new Boolean(inCache));

        if (unique) {
            arrayResultHelper(q.execute(), recordClasses, inCache);
        } else {
            for (Iterator iter = ((List) q.execute()).iterator(); iter
                .hasNext();)
                arrayResultHelper(iter.next(), recordClasses, inCache);
        }
    }

    private void arrayResultHelper(Object result, Collection recordClasses,
        boolean inCache) {
        assertEquals(Object[].class, result.getClass());
        Object[] os = (Object[]) result;
        assertEquals(recordClasses.size(), os.length);
        for (int i = 0; i < recordClasses.size(); i++)
            assertEquals(((ArrayList) recordClasses).get(i), os[i].getClass());
    }

    private void mapHelper(boolean unique, Collection recordClasses,
        Collection results, boolean inCache) {
        Query q = setUpQuery(unique, results);
        System.out.println("Query String " + q.getQueryString());
        Collection coll = null;
        if (q.execute() != null && (q.execute() instanceof Collection)) {
            coll = (Collection) q.execute();
        }
        System.out.println("Type of q.execute is : " + q.execute().getClass());

        Iterator it = coll.iterator();
        while (it.hasNext())
            System.out.println("Query result is " + it.next().getClass());

        q.setResultType(HashMap.class);

        if (unique) {
            mapResultHelper(q.execute(), recordClasses, results, inCache);
        } else {
            for (Iterator iter = ((Collection) q.execute()).iterator();
                iter.hasNext();)
                mapResultHelper(iter.next(), recordClasses, results, inCache);
        }

        CacheTestHelper.assertInCache(this, q, new Boolean(inCache));

        if (unique) {
            mapResultHelper(q.execute(), recordClasses, results, inCache);
        } else {
            for (Iterator iter = ((List) q.execute()).iterator(); iter
                .hasNext();)
                mapResultHelper(iter.next(), recordClasses, results, inCache);
        }
    }

    private void mapResultHelper(Object result, Collection recordClasses,
        Collection results, boolean inCache) {
        assertEquals(HashMap.class, result.getClass());
        HashMap m = (HashMap) result;
        assertEquals(recordClasses.size(), m.size());
        for (int i = 0; i < recordClasses.size(); i++)
            assertEquals("Map Contents " + m.toString() + " result: " + result
                + " loop size: " + recordClasses.size()
                + "Value of m.get(results[i]) "
                + m.get(((ArrayList) results).get(i).getClass()),
                ((ArrayList) recordClasses).get(i),
                m.get("jpqlalias" + (i + 1)).getClass());
    }

    private void rawHelper(boolean unique, Class recordClass, String result,
        boolean inCache) {
        ArrayList<String> l = new ArrayList<String>();
        l.add(result);
        Collection res = (result == null) ? null : l;
        Query q = setUpQuery(unique, res);
        if (unique)
            assertEquals(recordClass, q.execute().getClass());
        else {
            q.setUnique(unique);
            for (Iterator iter = ((List) q.execute()).iterator(); iter
                .hasNext();)
                assertEquals(recordClass, iter.next().getClass());
        }

        CacheTestHelper.assertInCache(this, q, new Boolean(inCache));

        if (unique) {
            assertEquals(recordClass, q.execute().getClass());
        } else {
            for (Iterator iter = ((List) q.execute()).iterator(); iter
                .hasNext();)
                assertEquals(recordClass, iter.next().getClass());
        }
    }

    private Query setUpQuery(boolean unique, Collection results) {
        String filter = "select";
        ArrayList resultsl = (ArrayList) results;
        if (results != null) {
            String resultString = "";
            for (int i = 0; i < resultsl.size(); i++) {
                resultString += "a." + resultsl.get(i);
                if (i < results.size() - 1)
                    resultString += ", ";
            }
            filter += " " + resultString;
            filter += " " + "from "
                + CacheObjectAChild1.class.getSuperclass().getSimpleName()
                + " a";
        } else {
            filter += " " + "a from "
                + CacheObjectAChild1.class.getSuperclass().getSimpleName()
                + " a";
        }

        if (unique)
            filter += " where a.age = 0";
        System.out.println("****Query: " + filter);
        Query q = _broker.newQuery(JPQLParser.LANG_JPQL, filter);
        q.setUnique(unique);
        q.setCandidateType(CacheObjectAChild1.class, false);

        return q;
    }
}

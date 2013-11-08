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
public class TestMutableParameters
    extends AbstractTestCase {

    public TestMutableParameters(String test) {
        super(test, "datacachecactusapp");
    }

    private BrokerFactory _factory;
    private Date _startDate;

    public void setUp()
        throws Exception {
        deleteAll(CacheObjectA.class);
        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.BrokerImpl", CacheTestBroker.class.getName());

        EntityManagerFactory emf = getEmf(propsMap);
        _factory = JPAFacadeHelper.toBrokerFactory(emf);

        // create a very early date so that when we mutate it, we
        // won't need to worry about precision issues.
        _startDate = new java.text.SimpleDateFormat("dd/MM/yyyy").
            parse("01/01/1990");
        Broker broker = _factory.newBroker();
        broker.begin();
        for (int i = 0; i < 50; i++) {
            CacheObjectAChild1 o = new CacheObjectAChild1
                ("", "JPQL2Queries", i);
            o.setDate(_startDate);
            broker.persist(o, null);
        }
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

    public void testMutatedDateParameter() {
        Broker broker = _factory.newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL,
            "select a from " +
                CacheObjectAChild1.class.getSimpleName() +
                " a where a.date < :p_date");
        Date d = new Date();
        Collection c = (Collection) q.execute(new Object[]{ d });
        CacheTestHelper.iterate(c);
        int initialSize = c.size();
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE,
            new Object[]{ d });
        d.setTime(_startDate.getTime());
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE,
            new Object[]{ d });
        c = (Collection) q.execute(new Object[]{ d });

        assertFalse(new Integer(initialSize).equals(new Integer(c.size())));
    }

    //FIXME Seetha Nov 10,2006
    //need to find the JPQL query for :p_age contains
    /*
    public void testMutatedSetParameter() {
        mutatedCollectionParameterHelper(true);
    }

    public void testMutatedListParameter() {
        mutatedCollectionParameterHelper(false);
    }

    public void mutatedCollectionParameterHelper(boolean set) {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL,
                    "select a from "+
                    CacheObjectAChild1.class.getSimpleName() +
                    "a :p_ages.contains (age)");
        Collection c_param;
        if (set)
            c_param = new HashSet();
        else
            c_param = new LinkedList();
        c_param.add(new Long(0));
        c_param.add(new Long(1));
        Collection c = (Collection) q.execute(new Object[]{ c_param });
        CacheTestHelper.iterate(c);
        int initialSize = c.size();
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE,
            new Object[]{ c_param });
        c_param.add(new Long(2));
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE,
            new Object[]{ c_param });
        c = (Collection) q.execute(new Object[]{ c_param });
        assertFalse(new Integer(initialSize).equals(new Integer(c.size())));
    }

    public void testMutatedSetParameterDates() {
        mutatedCollectionParameterDatesHelper(true);
    }

    public void testMutatedListParameterDates() {
        mutatedCollectionParameterDatesHelper(false);
    }

    public void mutatedCollectionParameterDatesHelper(boolean set) {
        Broker broker = _factory.newBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, CacheObjectAChild1.class,
                ":p_dates.contains (date)");
        Collection c_param;
        if (set)
            c_param = new HashSet();
        else
            c_param = new LinkedList();
        c_param.add(new Date());
        c_param.add(new Date(System.currentTimeMillis() - 1000));
        Collection c = (Collection) q.execute(new Object[]{ c_param });
        CacheTestHelper.iterate(c);
        int initialSize = c.size();
        CacheTestHelper.assertInCache(this, q, Boolean.TRUE,
            new Object[]{ c_param });
        c_param.add(new Date(System.currentTimeMillis() - 500));
        CacheTestHelper.assertInCache(this, q, Boolean.FALSE,
            new Object[]{ c_param });
    }*/
}

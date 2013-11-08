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


import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectA;
import org.apache.openjpa.persistence.datacache.common.apps.CacheObjectB;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.persistence.JPAFacadeHelper;

/**
 * Test some assumptions about query result metadata that the query
 * cache relies on.
 */
@AllowFailure(message="surefire excluded")
public class TestQueryResultTypes extends AbstractTestCase {

    public TestQueryResultTypes(String test) {
        super(test, "datacachecactusapp");
    }

    public void testQueryResultTypesWithThis() {
        Broker broker = getBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.name,a FROM " +
                CacheObjectA.class.getSimpleName() + " a");

        Class[] types = q.getProjectionTypes();
        assertEquals(2, types.length);
        assertEquals(String.class, types[0]);
        assertEquals(CacheObjectA.class, types[1]);
    }

    public void testQueryResultTypesWithRelated() {
        Broker broker = getBroker();
        Query q = broker
            .newQuery(JPQLParser.LANG_JPQL, "select a.name,a.relatedB FROM " +
                CacheObjectA.class.getSimpleName() + " a");

        //CacheObjectA.class,           "select name, relatedB");
        Class[] types = q.getProjectionTypes();
        assertEquals(2, types.length);
        assertEquals(String.class, types[0]);
        assertEquals(CacheObjectB.class, types[1]);
    }

    public void testSingleProjectionOfNonThisRelation() {
        Broker broker = getBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a.relatedB FROM " +
                CacheObjectA.class.getSimpleName() + " a");

        Class[] types = q.getProjectionTypes();
        assertEquals(1, types.length);
        assertEquals(CacheObjectB.class, types[0]);
    }

    public void testSingleProjectionOfThisRelation() {
        Broker broker = getBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL, "select a FROM " +
            CacheObjectA.class.getSimpleName() + " a");
        Class[] types = q.getProjectionTypes();
        assertEquals(0, types.length);
    }

    public void testNoResultClause() {
        Broker broker = getBroker();
        Query q =
            broker.newQuery(JPQLParser.LANG_JPQL, "select a FROM " +
                CacheObjectA.class.getSimpleName() + " a");
        Class[] types = q.getProjectionTypes();
        assertEquals(0, types.length);
    }

    private Broker getBroker() {
        return JPAFacadeHelper
            .toBroker(currentEntityManager());
    }
}

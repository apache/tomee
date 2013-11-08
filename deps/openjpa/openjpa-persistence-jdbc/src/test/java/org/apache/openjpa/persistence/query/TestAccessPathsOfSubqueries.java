/*
 * TestAccessPathsOfSubqueries.java
 *
 * Created on October 17, 2006, 2:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.query;

import java.util.Arrays;
import java.util.Collection;


import org.apache.openjpa.persistence.query.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest4;
import org.apache.openjpa.persistence.query.common.apps.RuntimeTest5;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.meta.ClassMetaData;

public class TestAccessPathsOfSubqueries extends BaseQueryTest {

    /**
     * Creates a new instance of TestAccessPathsOfSubqueries
     */

    public TestAccessPathsOfSubqueries(String name) {
        super(name);
    }

    public void testSimpleSubqueryAccessPath() {
        Broker broker = getBrokerFactory().newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL,
            "SELECT o FROM RuntimeTest1 o WHERE EXISTS ("
            + "SELECT rt5.name FROM RuntimeTest5 rt5 "
            + "WHERE rt5.name IS NOT NULL)");
        ClassMetaData[] metas = q.getAccessPathMetaDatas();
        Collection c = Arrays.asList(metas);
        ClassMetaData rt1 = broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(RuntimeTest1.class,
            broker.getClassLoader(), true);
        ClassMetaData rt5 = broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(RuntimeTest5.class,
            broker.getClassLoader(), true);
        assertTrue(c.contains(rt1));
        assertTrue(c.contains(rt5));
        assertEquals(2, c.size());
    }

    public void testRelationTraversalSubqueryAccessPath() {
        Broker broker = getBrokerFactory().newBroker();
        Query q = broker.newQuery(JPQLParser.LANG_JPQL,
            "SELECT o FROM RuntimeTest1 o WHERE EXISTS ("
            + "SELECT rt5.runtimeTest4.name FROM RuntimeTest5 rt5 "
            + "WHERE rt5.name IS NOT NULL)");

        ClassMetaData[] metas = q.getAccessPathMetaDatas();
        Collection c = Arrays.asList(metas);
        ClassMetaData rt1 = broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(RuntimeTest1.class,
            broker.getClassLoader(), true);
        ClassMetaData rt4 = broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(RuntimeTest4.class,
            broker.getClassLoader(), true);
        ClassMetaData rt5 = broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(RuntimeTest5.class,
            broker.getClassLoader(), true);
        assertTrue(c.contains(rt1));
        assertTrue(c.contains(rt4));
        assertTrue(c.contains(rt5));
        assertEquals(3, c.size());
    }
}

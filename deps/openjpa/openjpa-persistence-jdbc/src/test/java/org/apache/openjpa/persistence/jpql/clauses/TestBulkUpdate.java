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
package org.apache.openjpa.persistence.jpql.clauses;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.common.apps.*;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestBulkUpdate extends AbstractTestCase {

    public TestBulkUpdate(String name) {
        super(name, "jpqlclausescactusapp");
    }

    public void setUp() {
        deleteAll(Entity1.class);
        deleteAll(Entity2.class);
    }

    public void testSimpleBulkUpdate() {
        for (int i = 1; i < 5; i++)
            testSimpleBulkUpdate(i);
    }

    public void testSimpleBulkUpdate(int num) {
        deleteAll(Entity1.class);

        EntityManager em = currentEntityManager();
        startTx(em);
        for (int i = 0; i < num; i++) {
            Entity1 e = new Entity1(i, "value1", i);
            em.persist(e);
        }
        endTx(em);

        // test update
        startTx(em);
        Query updateQuery1 = em.createQuery("update Entity1 e"
            + " set e.stringField = 'value2', e.intField = 2"
            + " where e.stringField = :val").
            setParameter("val", "value1");
        assertEquals(num, updateQuery1.executeUpdate());
        assertEquals(0, updateQuery1.executeUpdate()); // should be updated
        endTx(em);

        // test update with parameter
        startTx(em);
        Query updateQuery2 = em.createQuery("update Entity1 e"
            + " set e.stringField = :newval where e.stringField = :val").
            setParameter("val", "value2").
            setParameter("newval", "value3");
        assertEquals(num, updateQuery2.executeUpdate());
        assertEquals(0, updateQuery2.executeUpdate()); // should be updated
        endTx(em);

        // test update with 2 parameters
        startTx(em);
        Query updateQuery3 = em.createQuery("update Entity1 e"
            + " set e.stringField = :newval, e.intField = 999"
            + " where e.stringField = :val").
            setParameter("val", "value3").
            setParameter("newval", "value4");
        assertEquals(num, updateQuery3.executeUpdate());
        assertEquals(0, updateQuery3.executeUpdate()); // should be updated
        endTx(em);

        // test update with null value
        startTx(em);
        Query updateQuery4 = em.createQuery("update Entity1 e"
            + " set e.stringField = :nullval, e.intField = :intval"
            + " where e.stringField = :val"
            + " and e.intField = 999").
            setParameter("val", "value4").
            setParameter("intval", new Integer(987)).
            setParameter("nullval", null);
        assertEquals(num, updateQuery4.executeUpdate());
        assertEquals(0, updateQuery4.executeUpdate()); // should be updated
        endTx(em);

        // test update with field refernece in update value
        // ### this seems to not be working
        startTx(em);
        Query updateFieldValue = em.createQuery("update Entity1 e"
            + " set e.intField = e.intField + 1");
        assertEquals(num, updateFieldValue.executeUpdate());
        endTx(em);

        startTx(em);
        Query deleteQuery = em.createQuery
            ("delete from Entity1 e where e.stringField = :val").
            setParameter("val", null);
        assertEquals(num, deleteQuery.executeUpdate());
        assertEquals(0, deleteQuery.executeUpdate());
        endTx(em);

        endEm(em);
    }
}


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
package org.apache.openjpa.persistence.kernel;

import java.util.List;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBRetainValues extends AbstractTestCase {

    public TestEJBRetainValues(String testName) {
        super(testName, "kernelcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
    }

    /**
     * tests the behavior of the same object being viewed from
     * different PersistenceManagers with RetainValues set to true
     */
    public void testRetainValues() {
        // persist the object with field == "x"
        RuntimeTest1 test = new RuntimeTest1();
        test.setStringField("x");
        EntityManager pm = currentEntityManager();
        startTx(pm);
        pm.persist(test);
        endTx(pm);

        //	find the object in a different PM
        EntityManager pm2 = currentEntityManager();
        List l = (buildSelectQuery(pm2, "x"));
        assertNotNull("l is null in testRetainValues", l);
        RuntimeTest1 sameThing = (RuntimeTest1) l.iterator().next();

        assertEquals("x", sameThing.getStringField());
        assertEquals("x", test.getStringField());

        //	set the second object field to "y"
        startTx(pm2);
        sameThing.setStringField("y");
        endTx(pm2);
        assertEquals("y", sameThing.getStringField());
        assertEquals("x", test.getStringField());

        //	do some searching in the first PM
        List l2 = buildSelectQuery(pm, "x");
        //should be zero if retainvalue is set to true but that is kodo
        //specific.
        assertEquals(0, l2.size());

        List l3 = buildSelectQuery(pm, "y");
        assertEquals(1, l3.size());
        assertEquals(test, l3.iterator().next());
        assertEquals("x", test.getStringField());

        endEm(pm2);
        endEm(pm);
    }

    public static List buildSelectQuery(EntityManager em, String param) {
        return em.createQuery(
            "SELECT c FROM RuntimeTest1 c where c.stringField = :username")
            .setParameter("username", param)
            .getResultList();
	}
}

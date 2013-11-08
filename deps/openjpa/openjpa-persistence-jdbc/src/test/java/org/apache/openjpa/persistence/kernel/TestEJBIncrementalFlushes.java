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

import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.InstanceCallbacksTest;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBIncrementalFlushes extends AbstractTestCase {

    public TestEJBIncrementalFlushes(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);

        EntityManager em = currentEntityManager();
        startTx(em);

        endTx(em);
        endEm(em);
    }

//	public void testBasicJdoPreStore ()
//	{
//		EntityManager em = getEm ();
//		em.getTransaction ().begin ();
//		InstanceCallbacksTest a = new InstanceCallbacksTest ("foo", 10);
//		em.persist(a);
//		em.flush ();
//		assertTrue (a.preStoreCalled);
//		em.getTransaction ().commit ();
//	}

    public void testFlushNoChange() {
        EntityManager em = currentEntityManager();
        startTx(em);
        InstanceCallbacksTest a = new InstanceCallbacksTest("foo", 10);
        em.persist(a);
        em.flush();
        endTx(em);
        //assertTrue (a.preStoreCalled);
        assertEquals(10, a.getIntField());

        endTx(em);
        endEm(em);
    }

    public void testOptimisticLockGivesCorrectError() {
        EntityManager pm1 = currentEntityManager();
        EntityManager pm2 = currentEntityManager();

        RuntimeTest1 a1 = new RuntimeTest1("foo", 10);
        startTx(pm1);
        pm1.persist(a1);
        endTx(pm1);

        RuntimeTest1 a2 = (RuntimeTest1) pm2.find(RuntimeTest1.class, 10);
        startTx(pm2);
        a2.setStringField("foobar");
        startTx(pm2);

        startTx(pm1);
        a1.setStringField("foobarbaz");
        try {
            endTx(pm1);
        }
        catch (Exception ole) {
            // expected case
        }
        finally {
            if (pm1.getTransaction().isActive())
                pm1.getTransaction().rollback();

            endEm(pm1);
            endEm(pm2);
        }
    }
}

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
package org.apache.openjpa.kernel;

import org.apache.openjpa.jdbc.kernel.A;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestBroker extends SingleEMFTestCase {
    public void setUp() {
        super.setUp(A.class, CLEAR_TABLES);
    }

    /**
     * This test asserts that the ObjectId obtained from the Broker is the same for the following cases: [ An Entity
     * that exists in the current persistence context, a detached Entity with a DetachedStateManager, and a detached
     * Entity with no DetachedStateManager]
     */
    public void testGetObjectId() {
        EntityManagerImpl em = (EntityManagerImpl) emf.createEntityManager();
        Broker broker = em.getBroker();

        em.getTransaction().begin();
        A a = new A();
        em.persist(a);
        Object oidInPersistenceContext = broker.getObjectId(a);
        em.getTransaction().commit();
        em.clear();
        Object oidDetached = broker.getObjectId(a);
        em.close();

        em = (EntityManagerImpl) emf.createEntityManager();
        broker = em.getBroker();
        A a2 = new A();
        a2.setId(a.getId());

        Object oidDetachedNoSm = broker.getObjectId(a2);

        assertTrue(oidInPersistenceContext.equals(oidDetached));
        assertTrue(oidDetached.equals(oidDetachedNoSm));
    }
}

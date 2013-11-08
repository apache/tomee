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
package org.apache.openjpa.persistence.lifecycle;

import javax.persistence.EntityManager;

import org.apache.openjpa.enhance.UnenhancedPropertyAccess;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestPessimisticPNonTransToPDirty
    extends SingleEMTestCase {

    public void setUp() {
        setUp(UnenhancedPropertyAccess.class, CLEAR_TABLES, "openjpa.RuntimeUnenhancedClasses", "supported");

        UnenhancedPropertyAccess o = new UnenhancedPropertyAccess();
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(o);
        em.getTransaction().commit();
        em.close();
    }

    public void testPNonTransToPDirty() {
        em.setOptimistic(false);
        UnenhancedPropertyAccess o = (UnenhancedPropertyAccess)
            em.createQuery("select o from UnenhancedPropertyAccess o")
                .getSingleResult();
        em.getTransaction().begin();
        try {
            Broker b = JPAFacadeHelper.toBroker(em);
            OpenJPAStateManager sm = b.getStateManager(o);
            assertEquals(PCState.PNONTRANS, sm.getPCState());
            o.setLazyField("foo");
            assertEquals(PCState.PDIRTY, sm.getPCState());
        } finally {
                em.getTransaction().rollback();
        }
    }
}

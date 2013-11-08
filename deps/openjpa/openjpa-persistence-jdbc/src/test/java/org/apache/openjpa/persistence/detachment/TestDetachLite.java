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
package org.apache.openjpa.persistence.detachment;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.persistence.detachment.model.DMCustomer;
import org.apache.openjpa.persistence.detachment.model.DMCustomerInventory;
import org.apache.openjpa.persistence.detachment.model.DMItem;
import org.apache.openjpa.util.Proxy;

public class TestDetachLite extends TestDetach {
    Object[] props =
        new Object[] { "openjpa.DetachState", "loaded(LiteAutoDetach=true)", DMCustomer.class,
            DMCustomerInventory.class, DMItem.class, CLEAR_TABLES };

    public void setUp() {
        super.setUp(props);

        Compatibility compat = emf.getConfiguration().getCompatibilityInstance();
        compat.setCopyOnDetach(false);
        compat.setFlushBeforeDetach(false);
        em = emf.createEntityManager();
        root = createData();
    }

    public void testPendingClear() {
        EntityManager em = emf.createEntityManager();
        DMCustomer dm = em.find(DMCustomer.class, root.getId());
        dm.setLastName(System.currentTimeMillis() + "--last");
        em.clear();
        em.getTransaction().begin();
        // Pre OPENJPA-2136 this commit call would fail.
        em.getTransaction().commit();
    }

    public void testLeaveProxy() {
        Object[] p = props;
        p[1] = "loaded(LiteAutoDetach=true,DetachProxyFields=false)";
        EntityManagerFactory iemf = createEMF(p);
        try {
            DMCustomer dc = new DMCustomer();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            dc.setCal(cal);

            EntityManager iem = iemf.createEntityManager();
            try {
                iem.getTransaction().begin();
                iem.persist(dc);
                iem.getTransaction().commit();
                Calendar beforeDetachCal = dc.getCal();
                iem.clear();
                Calendar afterDetachCal = dc.getCal();

                assertTrue(beforeDetachCal instanceof Proxy);
                assertTrue(afterDetachCal instanceof Proxy);
                
                // Make sure that we get rid of the StateManager.
                assertNull(((Proxy)afterDetachCal).getOwner());
            } finally {
                if (iem.getTransaction().isActive()) {
                    iem.getTransaction().rollback();
                }
                iem.close();
            }

        } finally {
            iemf.close();
        }
    }

    public void testProxyClear() {
        DMCustomer dc = new DMCustomer();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        dc.setCal(cal);

        EntityManager iem = emf.createEntityManager();
        try {
            iem.getTransaction().begin();
            iem.persist(dc);
            iem.getTransaction().commit();
            Calendar beforeDetachCal = dc.getCal();
            iem.clear();
            Calendar afterDetachCal = dc.getCal();

            assertTrue(beforeDetachCal instanceof Proxy);
            assertFalse(afterDetachCal instanceof Proxy);
        } finally {
            if (iem.getTransaction().isActive()) {
                iem.getTransaction().rollback();
            }
            iem.close();
        }
    }

    public void testCloseDetach() {
        root = em.merge(root);
        PersistenceCapable pc = (PersistenceCapable) root;
        assertFalse(pc.pcIsDetached());
        em.close();
        assertTrue(pc.pcIsDetached());
        // Make sure everything is detached and we can still use the Entity
        for (DMCustomerInventory c : root.getCustomerInventories()) {
            pc = (PersistenceCapable) c;
            assertTrue(pc.pcIsDetached());
            pc = (PersistenceCapable) c.getItem();
            assertTrue(pc.pcIsDetached());

        }
    }
}

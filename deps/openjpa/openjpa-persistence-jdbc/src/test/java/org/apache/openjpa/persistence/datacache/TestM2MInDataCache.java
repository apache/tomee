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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.datacache.common.apps.M2MEntityE;
import org.apache.openjpa.persistence.datacache.common.apps.M2MEntityF;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestM2MInDataCache extends SingleEMFTestCase {
    public void setUp() {
        setUp("openjpa.DataCache", "true", "openjpa.RemoteCommitProvider", "sjvm", M2MEntityE.class, M2MEntityF.class,
            CLEAR_TABLES);
    }

    public void populate(EntityManager em) {
        em.getTransaction().begin();
        M2MEntityE e1 = new M2MEntityE();
        e1.setId(1);
        e1.setName("ABC");
        em.persist(e1);
        M2MEntityE e2 = new M2MEntityE();
        e2.setId(2);
        e2.setName("DEF");
        em.persist(e2);

        M2MEntityF f1 = new M2MEntityF();
        f1.setId(10);
        em.persist(f1);
        M2MEntityF f2 = new M2MEntityF();
        f2.setId(20);
        em.persist(f2);

        e1.getEntityF().put(f1.getId(), f1);
        e1.getEntityF().put(f2.getId(), f2);
        e2.getEntityF().put(f1.getId(), f1);
        e2.getEntityF().put(f2.getId(), f2);

        f1.getEntityE().put(e1.getName(), e1);
        f1.getEntityE().put(e2.getName(), e2);
        f2.getEntityE().put(e1.getName(), e1);
        f2.getEntityE().put(e2.getName(), e2);
        em.getTransaction().commit();
    }

    public void validateGraph(M2MEntityE e1, M2MEntityE e2, M2MEntityF f1, M2MEntityF f2) {
        assertNotNull(e1);
        assertNotNull(e2);
        assertNotNull(f1);
        assertNotNull(f2);

        assertEquals(f1, e1.getEntityF().get(f1.getId()));
        assertEquals(f2, e1.getEntityF().get(f2.getId()));
        assertEquals(f1, e2.getEntityF().get(f1.getId()));
        assertEquals(f2, e2.getEntityF().get(f2.getId()));

        assertEquals(e1, f1.getEntityE().get(e1.getName()));
        assertEquals(e2, f1.getEntityE().get(e2.getName()));
        assertEquals(e1, f2.getEntityE().get(e1.getName()));
        assertEquals(e2, f2.getEntityE().get(e2.getName()));
    }

    /**
     * Test if child list is in order after new child list is added in setup().
     * 
     */
    public void testM2MDataCache() {
        EntityManager em = emf.createEntityManager();
        populate(em);
        em.close();

        em = emf.createEntityManager();
        M2MEntityE e1a = em.find(M2MEntityE.class, 1);
        assertNotNull(e1a.getEntityF());
        M2MEntityE e2a = em.find(M2MEntityE.class, 2);
        assertNotNull(e2a.getEntityF());
        M2MEntityF f1a = em.find(M2MEntityF.class, 10);
        assertNotNull(f1a.getEntityE());
        M2MEntityF f2a = em.find(M2MEntityF.class, 20);
        assertNotNull(f2a.getEntityE());
        em.close();
    }

    public void testEagerFetch() {
        EntityManager em = emf.createEntityManager();
        populate(em);
        em.close();

        em = emf.createEntityManager();

        OpenJPAEntityManager ojEm = OpenJPAPersistence.cast(em);
        ojEm.getFetchPlan().addField(M2MEntityE.class, "entityf");
        ojEm.getFetchPlan().addField(M2MEntityF.class, "entitye");

        M2MEntityE e1, e2;
        M2MEntityF f1, f2;

        e1 = em.find(M2MEntityE.class, 1);
        e2 = em.find(M2MEntityE.class, 2);
        f1 = em.find(M2MEntityF.class, 10);
        f2 = em.find(M2MEntityF.class, 20);

        validateGraph(e1, e2, f1, f2);
    }
}

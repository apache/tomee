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
package org.apache.openjpa.persistence.querycache;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;

import org.apache.openjpa.datacache.ConcurrentQueryCache;
import org.apache.openjpa.persistence.querycache.common.apps.Entity1;
import org.apache.openjpa.persistence.querycache.common.apps.Entity2;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

@NamedQuery(name = "setParam1",
    query = "SELECT o FROM Entity1 o WHERE o.pk = :pk")
public class TestQueryCache extends SQLListenerTestCase {

    EntityManager em;
    private static final String CACHE_NAME = "QueryCacheName";
    public void setUp() {
        super.setUp(
            DROP_TABLES,
            "openjpa.QueryCache", "true(name="+CACHE_NAME+")", 
            "openjpa.RemoteCommitProvider","sjvm",
            Entity1.class,Entity2.class
        // ,"openjpa.Log","SQL=trace"
            );
        em = emf.createEntityManager();
        
        em.getTransaction().begin();
        //create and persist multiple entity1 instances
        for (int i = 0; i < 10; i++) {
            Entity1 ent = new Entity1(i, "string" + i, i + 2);
            Entity2 ent2 = new Entity2(i * 2, "ent2" + i, i);
            ent.setEntity2Field(ent2);
            em.persist(ent);
        }
        em.getTransaction().commit();
    }

    public void testCachedQuery(){
        em.createQuery("Select object(o) from Entity1 o").getResultList().get(0);        
        resetSQL();
        em.createQuery("Select object(o) from Entity1 o").getResultList().get(0);
        em.createQuery("Select object(o) from Entity1 o").getResultList().get(0);
        
        assertEquals(0, getSQLCount());
        
    }
    public void testResultList() {
        List list = em.createQuery("Select object(o) from Entity1 o")
            .getResultList();

        assertEquals(10, list.size());

    }

    public void testGetSingleList() {

        String curr = 2 + "";

        Entity1 ret = (Entity1) em
            .createQuery("SELECT o FROM Entity1 o WHERE o.pk = :pk")
            .setParameter("pk", Long.valueOf(curr))
            .getSingleResult();

        assertNotNull(ret);
        assertEquals("string2", ret.getStringField());
        assertEquals(4, ret.getIntField());


    }

    public void testExecuteUpdate() {
        String curr = 2 + "";
        String curr2 = 22 + "";


        startTx(em);

        Entity1 entity1 = (Entity1) em
            .createQuery("SELECT o FROM Entity1 o WHERE o.pk = :pk")
            .setParameter("pk", Long.valueOf(curr))
            .getSingleResult();

        int ret = em.createQuery("Delete FROM Entity1 o WHERE o.pk = :pk")
            .setParameter("pk", Long.valueOf(curr))
            .executeUpdate();
        assertEquals(ret, 1);

        // cascade remove doesn't remove the entity2
        int retTmp = em.createQuery("Delete FROM Entity2 o WHERE o.pk = :pk")
            .setParameter("pk", entity1.getEntity2Field().getPk())
            .executeUpdate();

        int ret2 = em.createQuery("Delete FROM Entity1 o WHERE o.pk = :pk")
            .setParameter("pk", Long.valueOf(curr2))
            .executeUpdate();

        assertEquals(ret2, 0);

        endTx(em);

    }

    public void testSetMaxResults() {


        List l = em.createQuery("Select object(o) from Entity1 o")
            .setMaxResults(5)
            .getResultList();

        assertNotNull(l);
        assertEquals(5, l.size());


    }

    public void testSetFirstResults() {


        List l = em.createQuery("Select object(o) from Entity1 o")
            .setFirstResult(3)
            .getResultList();

        Entity1 ent = (Entity1) l.get(0);

        assertNotNull(ent);
        assertEquals("string3", ent.getStringField());
        assertEquals(5, ent.getIntField());


    }

    public void testName() {
        ConcurrentQueryCache qCache =
            (ConcurrentQueryCache) emf.getConfiguration().getDataCacheManagerInstance().getSystemQueryCache();
        assertNotNull(qCache);
        assertEquals(CACHE_NAME, qCache.getName());
    }

    protected void startTx(EntityManager em) {
        em.getTransaction().begin();
    }

    protected boolean isActiveTx(EntityManager em) {
        return em.getTransaction().isActive();
    }

    protected void endTx(EntityManager em) {
        if (em.getTransaction().isActive()) {
            if (em.getTransaction().getRollbackOnly())
                em.getTransaction().rollback();
            else
                em.getTransaction().commit();
        }
    }
}

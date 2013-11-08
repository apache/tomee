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
package org.apache.openjpa.persistence.external;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.openjpa.persistence.RollbackException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestExternalValues extends SingleEMFTestCase {

    public void setUp() {
        super.setUp(DROP_TABLES, EntityA.class);
    }

    public void testExternalValues() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        EntityA entity = new EntityA();

        entity.setS1("SMALL");
        entity.setS2("MEDIUM");
        entity.setUseStreaming(true);

        em.persist(entity);
        
        em.getTransaction().commit();
        
        // Validate
        
        Query q = em.createQuery("SELECT a from EntityA a");
        EntityA aPrime = (EntityA) q.getSingleResult();
        Assert.assertEquals("SMALL", aPrime.getS1());
        Assert.assertEquals("MEDIUM", aPrime.getS2());
        Assert.assertEquals(true, aPrime.getUseStreaming());

        em.getTransaction().begin();

        entity = new EntityA();
        entity.setS1("LARGE");
        entity.setS2("LARGE");
        entity.setUseStreaming(false);
        em.persist(entity);

        em.getTransaction().commit();

        q = em.createQuery("SELECT t0.id, t0.s1, t0._useStreaming, t0.s2 FROM EntityA t0 ORDER BY t0.s1 DESC");
        List<Object[]> res = q.getResultList();

        Iterator<Object[]> itr = res.iterator();
        Object[] values = itr.next();
        Assert.assertEquals("SMALL", values[1]);
        Assert.assertEquals(Boolean.TRUE, values[2]);
        Assert.assertEquals("MEDIUM", values[3]);

        values = itr.next();
        Assert.assertEquals("LARGE", values[1]);
        Assert.assertEquals(Boolean.FALSE, values[2]);
        Assert.assertEquals("LARGE", values[3]);
        
        em.close();
    }

    public void testUnrecognizedExternalValue() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        EntityA entity = new EntityA();

        entity.setS1("ABDEF");
        entity.setS2("NOT_VALID");

        em.persist(entity);

        try {
            em.getTransaction().commit();
            fail("Expected an exception at commit time");
        } catch (RollbackException e) {
            Throwable t = e;
            while (t.getCause() != null) {
                t = t.getCause();
            }
            assertTrue(t.getMessage().contains(
                    "was not found in the list of ExternalValues"));
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
        em.close();
    }
}

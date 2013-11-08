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
package org.apache.openjpa.persistence.jdbc.annotations;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.strats.NoneVersionStrategy;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for opt-lock
 *
 * @author Steve Kim
 */
public class TestVersion extends SingleEMFTestCase {

    public void setUp() {
        setUp(AnnoTest1.class, AnnoTest2.class, AnnoTest3.class, Flat1.class,
            EmbedOwner.class, EmbedValue.class, CLEAR_TABLES 
//            ,"openjpa.Log","SQL=trace"
            ,"openjpa.ConnectionFactoryProperties","printParameters=true"
        );

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 test1 = new AnnoTest1();
        test1.setPk(new Long(5));
        test1.setBasic(50);
        test1.setTransient(500);
        em.persist(test1);

        AnnoTest2 test2 = new AnnoTest2();
        test2.setPk1(5);
        test2.setPk2("bar");
        test2.setBasic("50");
        em.persist(test2);

        AnnoTest3 test3 = new AnnoTest3();
        test3.setPk(new Long(3));
        test3.setBasic2(50);
        em.persist(test3);
        em.getTransaction().commit();
        em.close();
    }

    public void testVersionNumeric() {
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();

        AnnoTest1 pc1 = em1.find(AnnoTest1.class, new Long(5));
        AnnoTest1 pc2 = em2.find(AnnoTest1.class, new Long(5));
        assertEquals(1, pc1.getVersion());
        assertEquals(1, pc2.getVersion());
        assertEquals(0, pc1.getTransient());
        pc1.setBasic(75);
        pc2.setBasic(75);
        em1.getTransaction().commit();
        em1.close();

        em1 = emf.createEntityManager();
        pc1 = em1.find(AnnoTest1.class, new Long(5));
        assertEquals(2, pc1.getVersion());
        em1.close();
        try {
            em2.getTransaction().commit();
            fail("Optimistic fail");
        } catch (Exception e) {
        } finally {
            em2.close();
        }
    }

    public void testVersionTimestamp() {
        // ensure that some time has passed
        // since the records were created
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            // do nothing
        }
        
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();

        AnnoTest2 pc1 = em1.find(AnnoTest2.class,
            new AnnoTest2.Oid(5, "bar"));
        AnnoTest2 pc2 = em2.find(AnnoTest2.class,
            new AnnoTest2.Oid(5, "bar"));
        assertNotNull(pc1.getVersion());
        assertEquals(pc1.getVersion(), pc2.getVersion());
        pc1.setBasic("75");
        pc2.setBasic("75");
        em1.getTransaction().commit();
        em1.close();

        em1 = emf.createEntityManager();
        pc1 = em1.find(AnnoTest2.class,
            new AnnoTest2.Oid(5, "bar"));
        java.util.Date pc1Version = pc1.getVersion();
        java.util.Date pc2Version = pc2.getVersion();
        assertTrue(pc1Version.compareTo(pc2Version) > 0);
        em1.close();
        try {
            em2.getTransaction().commit();
            fail("Optimistic fail");
        } catch (Exception e) {
        } finally {
            em2.close();
        }
    }

    public void testVersionSubclass() {
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();

        AnnoTest3 pc1 = em1.find(AnnoTest3.class, new Long(3));
        AnnoTest3 pc2 = em2.find(AnnoTest3.class, new Long(3));
        assertEquals(1, pc1.getVersion());
        assertEquals(1, pc2.getVersion());
        pc1.setBasic2(75);
        pc2.setBasic2(75);
        em1.getTransaction().commit();
        em1.close();

        em1 = emf.createEntityManager();
        pc1 = em1.find(AnnoTest3.class, new Long(3));
        assertEquals(2, pc1.getVersion());
        em1.close();
        try {
            em2.getTransaction().commit();
            fail("Optimistic fail");
        } catch (Exception e) {
        } finally {
            em2.close();
        }
    }

    public void testVersionNoChange() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AnnoTest1 pc = em.find(AnnoTest1.class, new Long(5));
        assertEquals(1, pc.getVersion());
        assertEquals(0, pc.getTransient());
        pc.setTransient(750);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(5));
        assertEquals(1, pc.getVersion());
        assertEquals(0, pc.getTransient());
        em.close();
    }

    public void testNoDefaultVersionWithoutFieldOrColumn() {
        ClassMapping cls = ((JDBCConfiguration) emf.getConfiguration()).
            getMappingRepositoryInstance().getMapping(EmbedOwner.class, 
            null, true);
        assertEquals(NoneVersionStrategy.getInstance(),
            cls.getVersion().getStrategy());
        assertEquals(0, cls.getVersion().getColumns().length);
    }

    public void testVersionWithField() {
        ClassMapping cls = ((JDBCConfiguration) emf.getConfiguration()).
            getMappingRepositoryInstance().getMapping(AnnoTest1.class, 
            null, true);
        assertTrue(NoneVersionStrategy.getInstance() !=
            cls.getVersion().getStrategy());
        assertEquals(1, cls.getVersion().getColumns().length);
    }

    public void testNullInitialVersion() {
        EntityManager em = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        try {
            AnnoTest1 e = new AnnoTest1(System.currentTimeMillis());
            em.getTransaction().begin();
            em.persist(e);
            em.createQuery("UPDATE AnnoTest1 a SET a.version=null where a.pk=:pk").setParameter("pk", e.getPk())
                .executeUpdate();
            em.getTransaction().commit();
            em.close();
             em = emf.createEntityManager();
            
            em.getTransaction().begin();
            em2.getTransaction().begin();
            
            AnnoTest1 e2 = em2.find(AnnoTest1.class, e.getPk());
            e = em.find(AnnoTest1.class, e.getPk());
            e.setBasic(1);
            em.getTransaction().commit();
            
            e2 = em2.find(AnnoTest1.class, e.getPk());
            em2.refresh(e2);
            System.out.println(e2.getBasic());
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

}

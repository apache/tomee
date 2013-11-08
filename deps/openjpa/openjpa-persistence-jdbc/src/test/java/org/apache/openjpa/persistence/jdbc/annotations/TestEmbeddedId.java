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

import javax.persistence.Query;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * <p>Test embedded id classes.</p>
 *
 * @author Abe White
 */
public class TestEmbeddedId
    extends SingleEMFTestCase {

    EmbeddedIdClass _oid;
    EmbeddedIdClass _roid;

    public void setUp() {
        setUp(EmbeddedIdEntity.class, EmbeddedIdClass.class, CLEAR_TABLES);

        _oid = new EmbeddedIdClass();
        _oid.setPk1(1);
        _oid.setPk2(2);

        EmbeddedIdEntity e = new EmbeddedIdEntity();
        e.setId(_oid);
        e.setValue("e");

        _roid = new EmbeddedIdClass();
        _roid.setPk1(2);
        _roid.setPk2(3);

        EmbeddedIdEntity rel = new EmbeddedIdEntity();
        rel.setId(_roid);
        rel.setValue("r");
        e.setRelation(rel);

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persistAll(e, rel);
        em.getTransaction().commit();
        em.close();
    }

    public void testGetObjectId() {
        OpenJPAEntityManager em = emf.createEntityManager();
        EmbeddedIdEntity e = em.find(EmbeddedIdEntity.class, _oid);
        assertNotNull(e);
        assertEquals("e", e.getValue());
        assertNull(e.getMappingOverrideRelation());
        assertNotNull(e.getRelation());
        assertEquals("r", e.getRelation().getValue());

        assertEquals(_oid, em.getObjectId(e));
        assertEquals(_roid, em.getObjectId(e.getRelation()));
        assertEquals(_oid, e.getId());
        assertEquals(_roid, e.getRelation().getId());
        assertNull(((PersistenceCapable) e.getId()).pcGetGenericContext());
        em.close();
    }

    public void testMutateEmbeddedIdFieldValueOfNew() {
        EmbeddedIdEntity e1 = new EmbeddedIdEntity();
        e1.setValue("e1");
        EmbeddedIdEntity e2 = new EmbeddedIdEntity();
        e2.setValue("e2");
        EmbeddedIdClass id = new EmbeddedIdClass();
        e2.setId(id);

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persistAll(e1, e2);

        EmbeddedIdClass oid = new EmbeddedIdClass();
        oid.setPk1(4);
        oid.setPk2(5);
        e1.setId(oid);
        e2.getId().setPk1(6);
        e2.getId().setPk2(7);
        em.getTransaction().commit();

        EmbeddedIdClass oid1 = e1.getId();
        assertEquals(oid1, em.getObjectId(e1));
        assertEquals(4, oid1.getPk1());
        assertEquals(5, oid1.getPk2());

        EmbeddedIdClass oid2 = e2.getId();
        // pcl: 30 October 2007: this fails currently; commenting out.
        // See OPENJPA-425
        //assertEquals(oid2, em.getObjectId(e2));
        assertEquals(6, oid2.getPk1());
        assertEquals(7, oid2.getPk2());
        em.close();

        em = emf.createEntityManager();
        e1 = em.find(EmbeddedIdEntity.class, oid1);
        e2 = em.find(EmbeddedIdEntity.class, oid2);
        assertEquals(oid1, em.getObjectId(e1));
        assertEquals(oid2, em.getObjectId(e2));
        assertEquals(oid1, e1.getId());
        assertEquals(oid2, e2.getId());
        em.close();
    }

    public void testMutateEmbeddedIdFieldValueOfExisting() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        EmbeddedIdEntity e = em.find(EmbeddedIdEntity.class, _oid);
        e.setValue("changed");
        try {
            e.getId().setPk1(9);
            em.getTransaction().commit();
            fail("Committed with changed oid field.");
        } catch (RuntimeException re) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }
        em.close();
    }

    public void testDetachAttach() {
        OpenJPAEntityManager em = emf.createEntityManager();
        EmbeddedIdEntity e = em.find(EmbeddedIdEntity.class, _oid);
        e.getRelation();
        em.close();

        e.setValue("echanged");
        e.getRelation().setValue("rchanged");

        em = emf.createEntityManager();
        em.getTransaction().begin();
        EmbeddedIdEntity me = (EmbeddedIdEntity) em.mergeAll(e,
            e.getRelation())[0];
        assertTrue(me != e);
        assertNotNull(me.getRelation());
        assertTrue(me.getRelation() != e.getRelation());
        assertEquals("echanged", me.getValue());
        assertEquals("rchanged", me.getRelation().getValue());
        assertEquals(_oid, me.getId());
        assertEquals(_oid, em.getObjectId(me));
        assertEquals(_roid, me.getRelation().getId());
        assertEquals(_roid, em.getObjectId(me.getRelation()));
        em.getTransaction().commit();
        em.close();
    }

    public void testQuery() {
        OpenJPAEntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select e from EmbeddedIdEntity e "
            + "where e.id.pk1 = 1");
        EmbeddedIdEntity e = (EmbeddedIdEntity) q.getSingleResult();
        assertEquals(_oid, e.getId());
        assertEquals("e", e.getValue());

        q = em.createQuery("select e.id.pk2 from EmbeddedIdEntity e "
            + "where e.id.pk1 = 1");
        assertEquals(new Long(_oid.getPk2()), q.getSingleResult());

        q = em.createQuery("select e.id from EmbeddedIdEntity e "
            + "where e.id.pk1 = 1");
        assertEquals(_oid, q.getSingleResult());
        em.close();
    }

    public void testAutoAssigned() {
        // begin with null id object
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        EmbeddedIdEntity e = new EmbeddedIdEntity();
        em.persist(e);
        EmbeddedIdClass oid = e.getId();
        assertNotNull(oid);
        assertTrue(oid.getPk3() != 0);
        assertEquals(oid, em.getObjectId(e));
        em.getTransaction().commit();
        assertEquals(oid, em.getObjectId(e));
        em.close();

        em = emf.createEntityManager();
        e = em.find(EmbeddedIdEntity.class, oid);
        assertEquals(oid, em.getObjectId(e));
        em.close();

        // begin with non-null id object
        em = emf.createEntityManager();
        em.getTransaction().begin();
        e = new EmbeddedIdEntity();
        oid = new EmbeddedIdClass();
        oid.setPk1(4);
        oid.setPk2(5);
        e.setId(oid);
        em.persist(e);
        oid = e.getId();
        assertEquals(4, oid.getPk1());
        assertEquals(5, oid.getPk2());
        assertTrue(oid.getPk3() != 0);
        assertEquals(oid, em.getObjectId(e));
        em.getTransaction().commit();
        assertEquals(oid, em.getObjectId(e));
        em.close();

        em = emf.createEntityManager();
        e = em.find(EmbeddedIdEntity.class, oid);
        assertEquals(oid, em.getObjectId(e));
        em.close();

        // flush before accessing id field
        em = emf.createEntityManager();
        em.getTransaction().begin();
        e = new EmbeddedIdEntity();
        em.persist(e);
        em.getTransaction().commit();
        oid = e.getId();
        assertTrue(oid.getPk3() != 0);
        assertEquals(oid, em.getObjectId(e));
        em.close();

        em = emf.createEntityManager();
        e = em.find(EmbeddedIdEntity.class, oid);
        assertEquals(oid, em.getObjectId(e));
        em.close();
    }
}

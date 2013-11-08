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
package org.apache.openjpa.persistence.annotations;

import javax.persistence.*;

import org.apache.openjpa.enhance.*;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

/**
 * <p>Test embedded id classes.</p>
 *
 * @author Abe White
 */
public class TestEmbeddedId extends AnnotationTestCase
{

	public TestEmbeddedId(String name)
	{
		super(name, "annotationcactusapp");
	}

    EmbeddedIdClass _oid;
    EmbeddedIdClass _roid;

    public void setUp() {
        deleteAll(EmbeddedIdEntity.class);

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

        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        em.persistAll(e, rel);
        endTx(em);
        endEm(em);
    }

    public void testGetObjectId() {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
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
        endEm(em);
    }

    public void testMutateEmbeddedIdFieldValueOfNew() {
        EmbeddedIdEntity e1 = new EmbeddedIdEntity();
        e1.setValue("e1");
        EmbeddedIdEntity e2 = new EmbeddedIdEntity();
        e2.setValue("e2");
        
        e2.setId(new EmbeddedIdClass());
        //Comment this since pk3 is auto-generated and assigning
        //it explicitly causes a rollback
        //e2.getId().setPk3(99); // prevent assign on access

        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        //em.persistAll(e1, e2);

         EmbeddedIdClass oid = new EmbeddedIdClass();
        oid.setPk1(4);
        oid.setPk2(5);
        //Comment this since pk3 is auto-generated and assigning
        //it explicitly causes a rollback
        //oid.setPk3(6);
        e1.setId(oid);
        e2.getId().setPk1(6);
        e2.getId().setPk2(7);
        em.persistAll(e1, e2);

        endTx(em);

        EmbeddedIdClass oid1 = e1.getId();
        EmbeddedIdClass oid2 = e2.getId();
        assertEquals(oid1, em.getObjectId(e1));
        assertEquals(oid2, em.getObjectId(e2));
        assertEquals(4, oid1.getPk1());
        assertEquals(5, oid1.getPk2());
        assertEquals(6, oid2.getPk1());
        assertEquals(7, oid2.getPk2());
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        e1 = em.find(EmbeddedIdEntity.class, oid1);
        e2 = em.find(EmbeddedIdEntity.class, oid2);
        assertEquals(oid1, em.getObjectId(e1));
        assertEquals(oid2, em.getObjectId(e2));
        assertEquals(oid1, e1.getId());
        assertEquals(oid2, e2.getId());
        endEm(em);
    }

    public void testMutateEmbeddedIdFieldValueOfExisting() 
    {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        EmbeddedIdEntity e = em.find(EmbeddedIdEntity.class, _oid);
        e.setValue("changed");
        try {
            e.getId().setPk1(9);
            endTx(em);
            fail("Committed with changed oid field.");
        } catch (RuntimeException re) {
            if (isActiveTx(em))
            	rollbackTx(em);
        }
        catch (Exception exc)
        {
            if (isActiveTx(em))
            	rollbackTx(em);
        }
        endEm(em);
    }

    public void testDetachAttach() {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        EmbeddedIdEntity e = em.find(EmbeddedIdEntity.class, _oid);
        e.getRelation();
        endEm(em);

        e.setValue("echanged");
        e.getRelation().setValue("rchanged");

        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
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
        endTx(em);
        endEm(em);
    }

    public void testQuery() {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
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
        endEm(em);
    }

    public void testAutoAssigned() {
        // begin with null id object
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        EmbeddedIdEntity e = new EmbeddedIdEntity();
        em.persist(e);
        EmbeddedIdClass oid = e.getId();
        assertNotNull(oid);
        assertTrue(oid.getPk3() != 0);
        assertEquals(oid, em.getObjectId(e));
        endTx(em);
        assertEquals(oid, em.getObjectId(e));
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        e = em.find(EmbeddedIdEntity.class, oid);
        assertEquals(oid, em.getObjectId(e));
        endEm(em);

        // begin with non-null id object
        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
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
        endTx(em);
        assertEquals(oid, em.getObjectId(e));
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        e = em.find(EmbeddedIdEntity.class, oid);
        assertEquals(oid, em.getObjectId(e));
        endEm(em);

        // flush before accessing id field
        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        e = new EmbeddedIdEntity();
        em.persist(e);
        endTx(em);
        oid = e.getId();
        assertTrue(oid.getPk3() != 0);
        assertEquals(oid, em.getObjectId(e));
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        e = em.find(EmbeddedIdEntity.class, oid);
        assertEquals(oid, em.getObjectId(e));
        endEm(em);
    }
}

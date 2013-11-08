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
package org.apache.openjpa.persistence.managedinterface;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;

import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.query.SimpleEntity;
import org.apache.openjpa.meta.ClassMetaData;

@AllowFailure(message=
    "On JDK6 Upgrade these tests are failing with wrong mapping. " +
    "Column PC_ID in ManagedInterfaceSup Table is not created. getPC() is " +
    "defined as property in ManageIFace sub-interface")
public class TestManagedInterfaces extends SingleEMFTestCase {

    @Override
    public void setUp() {
        super.setUp(SimpleEntity.class, ManagedInterfaceEmbed.class,
            ManagedInterfaceSup.class, ManagedIface.class,
            ManagedInterfaceOwner.class, MixedInterface.class,
            MixedInterfaceImpl.class, NonMappedInterfaceImpl.class,
            CLEAR_TABLES);
    }

    public void testEmbeddedMetaData() {
        emf.createEntityManager().close();
        ClassMetaData ownerMeta = JPAFacadeHelper.getMetaData(emf,
            ManagedIface.class);
        ClassMetaData embeddedMeta = ownerMeta.getField("embed")
            .getDefiningMetaData();
        assertTrue(embeddedMeta.isManagedInterface());
        assertTrue(embeddedMeta.isIntercepting());

        ClassMetaData embeddableMeta = JPAFacadeHelper.getMetaData(emf,
            ManagedInterfaceEmbed.class);
        assertTrue(embeddableMeta.isManagedInterface());
        assertTrue(embeddableMeta.isIntercepting());
    }

    public void testManagedInterface() throws Exception {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ManagedIface pc = em.createInstance(ManagedIface.class);
        pc.setIntFieldSup(3);
        pc.setIntField(4);
        pc.setEmbed(em.createInstance(ManagedInterfaceEmbed.class));

        pc.getEmbed().setEmbedIntField(5);
        assertEquals(5, pc.getEmbed().getEmbedIntField());
        em.persist(pc);
        Object oid = em.getObjectId(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        assertEquals(3, pc.getIntFieldSup());
        assertEquals(4, pc.getIntField());
        assertEquals(5, pc.getEmbed().getEmbedIntField());
        em.getTransaction().begin();
        pc.setIntField(14);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager ();
        em.getTransaction().begin();
        Query query = em.createQuery("select o from ManagedIface o " +
            "where o.intField = 14");
        pc = (ManagedIface) query.getSingleResult();
        assertEquals(14, pc.getIntField());
        em.remove(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        try {
            assertNull(em.find(ManagedIface.class, oid));
        } catch (EntityNotFoundException onfe) {}

        em.close();
    }

    public void testInterfaceOwner() {
        OpenJPAEntityManager em = emf.createEntityManager();
        ManagedInterfaceOwner pc = new ManagedInterfaceOwner();
        pc.setIFace(em.createInstance(ManagedInterfaceSup.class));
        pc.setEmbed(em.createInstance(ManagedInterfaceEmbed.class));
        pc.getIFace().setIntFieldSup(3);
        pc.getEmbed().setEmbedIntField(5);

        em.getTransaction().begin();
        em.persist(pc);
        Object oid = em.getObjectId(pc);
        em.getTransaction().commit();
        pc = em.find(ManagedInterfaceOwner.class, oid);
        assertEquals(3, pc.getIFace().getIntFieldSup());
        assertEquals(5, pc.getEmbed().getEmbedIntField());
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedInterfaceOwner.class, oid);
        assertEquals(3, pc.getIFace().getIntFieldSup());
        assertEquals(5, pc.getEmbed().getEmbedIntField());
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("select o from ManagedInterfaceOwner o " +
            "where o.iface.intFieldSup = 3 and o.embed.embedIntField = 5");
        pc = (ManagedInterfaceOwner) q.getSingleResult();
        assertEquals(3, pc.getIFace().getIntFieldSup());
        assertEquals(5, pc.getEmbed().getEmbedIntField());

        pc.getIFace().setIntFieldSup(13);
        pc.getEmbed().setEmbedIntField(15);
        assertEquals(13, pc.getIFace().getIntFieldSup());
        assertEquals(15, pc.getEmbed().getEmbedIntField());
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedInterfaceOwner.class, oid);
        assertEquals(13, pc.getIFace().getIntFieldSup());
        assertEquals(15, pc.getEmbed().getEmbedIntField());
        em.close();
    }

    public void testCollection() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ManagedIface pc = em.createInstance(ManagedIface.class);
        Set set = new HashSet();
        set.add(new Integer(3));
        set.add(new Integer(4));
        set.add(new Integer(5));
        pc.setSetInteger(set);
        em.persist(pc);
        Object oid = em.getObjectId(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetInteger();
        assertEquals(3, set.size());
        assertTrue(set.contains(new Integer(3)));
        assertTrue(set.contains(new Integer(4)));
        assertTrue(set.contains(new Integer(5)));
        em.getTransaction().begin();
        set.remove(new Integer(4));
        set.add(new Integer(14));
        set.add(new Integer(15));
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetInteger();
        assertEquals(4, set.size());
        assertTrue(set.contains(new Integer(3)));
        assertTrue(set.contains(new Integer(5)));
        assertTrue(set.contains(new Integer(14)));
        assertTrue(set.contains(new Integer(15)));
        em.getTransaction().begin();
        pc.setSetInteger(null);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetInteger();
        assertTrue (set == null || set.size() == 0);
        em.close();
    }

    public void testCollectionPC() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ManagedIface pc = em.createInstance(ManagedIface.class);
        Set set = new HashSet();
        set.add(new SimpleEntity("a", "3"));
        set.add(new SimpleEntity("b", "4"));
        set.add(new SimpleEntity("c", "5"));
        pc.setSetPC(set);
        em.persist(pc);
        Object oid = em.getObjectId(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetPC();
        assertEquals(3, set.size());
        Collection seen = new ArrayList();
        SimpleEntity rel;
        SimpleEntity toRem = null;
        for (Iterator it = set.iterator(); it.hasNext();) {
            rel = (SimpleEntity) it.next();
            seen.add(rel.getName());
            if (rel.getValue().equals("4"))
                toRem = rel;
        }
        assertEquals(3, seen.size());
        assertTrue(seen.contains("a"));
        assertTrue(seen.contains("b"));
        assertTrue(seen.contains("c"));
        em.getTransaction().begin();
        assertNotNull(toRem);
        set.remove(toRem);
        set.add(new SimpleEntity("x", "14"));
        set.add(new SimpleEntity("y", "15"));
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetPC();
        assertEquals(4, set.size());
        seen.clear();
        for (Iterator it = set.iterator(); it.hasNext();) {
            rel = (SimpleEntity) it.next();
            seen.add(rel.getName());
        }
        assertEquals(4, seen.size());
        assertTrue(seen.contains("a"));
        assertTrue(seen.contains("c"));
        assertTrue(seen.contains("x"));
        assertTrue(seen.contains("y"));
        em.getTransaction().begin();
        pc.setSetPC(null);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetPC();
        assertTrue (set == null || set.size() == 0);
        em.close();
    }

    public void testCollectionInterfaces() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ManagedIface pc = em.createInstance(ManagedIface.class);
        Set set = new HashSet();
        set.add(createInstance(em, 3));
        set.add(createInstance(em, 4));
        set.add(createInstance(em, 5));
        pc.setSetI(set);
        em.persist(pc);
        Object oid = em.getObjectId(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetI();
        assertEquals(3, set.size());
        Collection seen = new ArrayList();
        ManagedIface rel = null;
        ManagedIface toRem = null;
        for (Iterator it = set.iterator(); it.hasNext();) {
            rel = (ManagedIface) it.next();
            seen.add(new Integer(rel.getIntField()));
            if (rel.getIntField() == 4)
                toRem = rel;
        }
        assertEquals(3, seen.size());
        assertTrue(seen.contains(new Integer(3)));
        assertTrue(seen.contains(new Integer(4)));
        assertTrue(seen.contains(new Integer(5)));
        em.getTransaction().begin();
        assertNotNull(toRem);
        set.remove(toRem);
        set.add(createInstance(em, 14));
        set.add(createInstance(em, 15));
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetI();
        assertEquals(4, set.size());
        seen.clear();
        for (Iterator it = set.iterator(); it.hasNext();) {
            rel = (ManagedIface) it.next();
            seen.add(new Integer(rel.getIntField()));
        }
        assertEquals(4, seen.size());
        assertTrue(seen.contains(new Integer(3)));
        assertTrue(seen.contains(new Integer(5)));
        assertTrue(seen.contains(new Integer(14)));
        assertTrue(seen.contains(new Integer(15)));
        em.getTransaction().begin();
        pc.setSetPC(null);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        set = pc.getSetPC();
        assertTrue (set == null || set.size() == 0);
        em.close();
    }

    public void testMixedQuery() {
        createMixed();

        OpenJPAEntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select o from MixedInterface o " +
            "where o.intField = 4");
        Collection c = q.getResultList();
        Set seen = new HashSet();
        assertEquals(2, c.size());
        MixedInterface pc;
        for (Iterator it = c.iterator(); it.hasNext();) {
            pc = (MixedInterface) it.next();
            assertEquals(4, pc.getIntField());
            seen.add(pc.getClass());
        }
        assertEquals(2, seen.size());
        
        // Changes of OPENJPA-485 had the positive (but unintended) consequence
        // of making this case pass, which was failing before as reported in
        // OPENJPA-481
    }

    public void testQueryForMixedInterfaceImpls() {
        createMixed();

        OpenJPAEntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select o from MixedInterfaceImpl o " +
            "where o.intField = 4");
        MixedInterface pc = (MixedInterface) q.getSingleResult();
        assertEquals(4, pc.getIntField());
        assertTrue(pc instanceof MixedInterfaceImpl);
        em.close();
    }

    public void testMixedExtent() {
        createMixed();

        OpenJPAEntityManager em = emf.createEntityManager();
        Extent e = em.createExtent(MixedInterface.class, true);
        Set seen = new HashSet();
        int size = 0;
        for (Iterator it = e.iterator(); it.hasNext();) {
            seen.add(it.next().getClass());
            size++;
        }
        assertEquals(3, size);
        assertEquals(2, seen.size());

        e = em.createExtent(MixedInterface.class, false);
        seen = new HashSet();
        size = 0;
        for (Iterator it = e.iterator(); it.hasNext();) {
            seen.add(it.next().getClass());
            size++;
        }
        assertEquals(1, size);
        assertNotEquals(MixedInterfaceImpl.class, seen.iterator().next());
        em.close();
    }

    private void createMixed() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        MixedInterface pc = em.createInstance(MixedInterface.class);
        pc.setIntField(4);
        em.persist(pc);
        pc = new MixedInterfaceImpl();
        pc.setIntField(4);
        em.persist(pc);
        pc = new MixedInterfaceImpl();
        pc.setIntField(8);
        em.persist(pc);
        em.getTransaction().commit();
        em.close();
    }

    public void testUnimplementedThrowsException() {
        OpenJPAEntityManager em = emf.createEntityManager();
        ManagedIface pc = createInstance(em, 1);
        try {
            pc.unimplemented();
            fail("Exception expected.");
        } catch (UnsupportedOperationException uoe) {} // good
        em.close();
    }

    public void testNonMappedCreateInstanceException() {
        // OpenJPA's support of non-mapped interfaces differs from JDO support;
        // there is no special query or relation support for non-mapped
        // interfaces in OpenJPA at this time.
        OpenJPAEntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.createInstance(NonMappedInterface.class);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {} // good
        if (em != null)
            em.close();
    }

    public void testDetach() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ManagedIface pc = createInstance(em, 4);
        em.persist(pc);
        Object oid = em.getObjectId(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        ManagedIface pcx = em.find(ManagedIface.class, oid);
        pc = em.detachCopy(pcx);
        em.close();

        assertTrue(em.isDetached(pc));
        pc.setIntField(7);

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(ManagedIface.class, oid);
        assertEquals(7, pc.getIntField());
        em.close();
    }

    private ManagedIface createInstance(OpenJPAEntityManager em, int i) {
        ManagedIface pc = em.createInstance(ManagedIface.class);
        pc.setIntField(i);
        return pc;
    }
}

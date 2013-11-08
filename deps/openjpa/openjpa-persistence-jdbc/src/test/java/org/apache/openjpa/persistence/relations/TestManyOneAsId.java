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
package org.apache.openjpa.persistence.relations;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Perform basic operations on an entity with a many-one relation as its id
 * field.
 *
 * @author Abe White
 */
public class TestManyOneAsId
    extends SingleEMFTestCase {

    private long id;
    private long dsid;
    private long cid;

    public void setUp() {
        setUp(BasicEntity.class, DataStoreBasicEntity.class,
            ManyOneIdOwner.class, DataStoreManyOneIdOwner.class,
            ManyOneCompoundIdOwner.class);

        BasicEntity id1 = new BasicEntity();
        id1.setName("id1");
        BasicEntity id2 = new BasicEntity();
        id2.setName("id2");
        id1.setRel(id2);
        id2.setRel(id1);
        DataStoreBasicEntity dsid1 = new DataStoreBasicEntity();
        dsid1.setName("dsid1");
        dsid1.setRel(id1);
        DataStoreBasicEntity dsid2 = new DataStoreBasicEntity();
        dsid2.setName("dsid2");
        dsid2.setRel(id2);

        ManyOneIdOwner parent = new ManyOneIdOwner();
        parent.setId(id1);
        parent.setName("parent");
        ManyOneIdOwner child = new ManyOneIdOwner();
        child.setId(id2);
        child.setName("child");
        parent.setSelfRel(child);
        DataStoreManyOneIdOwner dsparent = new DataStoreManyOneIdOwner();
        dsparent.setId(dsid1);
        dsparent.setName("dsparent");
        DataStoreManyOneIdOwner dschild = new DataStoreManyOneIdOwner();
        dschild.setId(dsid2);
        dschild.setName("dschild");
        dsparent.setSelfRel(dschild);
        ManyOneCompoundIdOwner cparent = new ManyOneCompoundIdOwner();
        cparent.setEntityId(id1);
        cparent.setName("cparent");
        ManyOneCompoundIdOwner cchild = new ManyOneCompoundIdOwner();
        cchild.setEntityId(id2);
        cchild.setName("cchild");
        cparent.setSelfRel(cchild);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(id1);
        em.persist(id2);
        em.persist(dsid1);
        em.persist(dsid2);
        em.persist(parent);
        em.persist(child);
        em.persist(dsparent);
        em.persist(dschild);
        em.persist(cparent);
        em.persist(cchild);
        em.getTransaction().commit();
        id = id1.getId();
        assertRelations(em, parent);
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em;
        dsid = (Long) oem.getObjectId(dsid1);
        assertDataStoreRelations(oem, dsparent);
        cid = cparent.getLongId();
        assertCompoundRelations(oem, cparent);
        em.close();
    }

    private void assertRelations(EntityManager em, ManyOneIdOwner parent) {
        assertEquals("parent", parent.getName());
        BasicEntity id1 = parent.getId();
        assertNotNull(id1);
        assertEquals(id, id1.getId());
        assertEquals("id1", id1.getName());
        assertTrue(id1 == em.find(BasicEntity.class, id));
        ManyOneIdOwner child = parent.getSelfRel();
        assertNotNull(child);
        assertEquals("child", child.getName());
        BasicEntity id2 = child.getId();
        assertNotNull(id2);
        assertEquals("id2", id2.getName());
        assertTrue(id2 == em.find(BasicEntity.class, id2.getId()));
        assertTrue(id2 == id1.getRel());
        assertTrue(id1 == id2.getRel());
        assertNull(child.getSelfRel());
    }

    private void assertDataStoreRelations(OpenJPAEntityManager em, 
        DataStoreManyOneIdOwner dsparent) {
        assertEquals("dsparent", dsparent.getName());
        DataStoreBasicEntity dsid1 = dsparent.getId();
        assertNotNull(dsid1);
        assertEquals(dsid, ((Long) em.getObjectId(dsid1)).longValue());
        assertEquals("dsid1", dsid1.getName());
        assertTrue(dsid1 == em.find(DataStoreBasicEntity.class, dsid));
        DataStoreManyOneIdOwner dschild = dsparent.getSelfRel();
        assertNotNull(dschild);
        assertEquals("dschild", dschild.getName());
        DataStoreBasicEntity dsid2 = dschild.getId();
        assertNotNull(dsid2);
        assertEquals("dsid2", dsid2.getName());
        assertTrue(dsid2 == em.find(DataStoreBasicEntity.class, 
            em.getObjectId(dsid2)));
        assertNull(dschild.getSelfRel());
    }

    private void assertCompoundRelations(OpenJPAEntityManager em, 
        ManyOneCompoundIdOwner cparent) {
        assertEquals("cparent", cparent.getName());
        BasicEntity id1 = cparent.getEntityId();
        assertNotNull(id1);
        assertEquals(id, id1.getId());
        assertEquals("id1", id1.getName());
        assertTrue(id1 == em.find(BasicEntity.class, id));
        ManyOneCompoundIdOwner cchild = cparent.getSelfRel();
        assertNotNull(cchild);
        assertEquals("cchild", cchild.getName());
        BasicEntity id2 = cchild.getEntityId();
        assertNotNull(id2);
        assertEquals("id2", id2.getName());
        assertTrue(id2 == em.find(BasicEntity.class, id2.getId()));
        assertNull(cchild.getSelfRel());
        ManyOneCompoundIdOwnerId oid = (ManyOneCompoundIdOwnerId) 
            em.getObjectId(cparent);
        assertEquals(id, oid.entityId);
    }

    public void testRetrieveWithManyOneId() {
        EntityManager em = emf.createEntityManager();
        ManyOneIdOwner parent = em.find(ManyOneIdOwner.class, id);
        assertNotNull(parent);
        assertRelations(em, parent);
        em.close();
    }

    public void testRetrieveWithDataStoreManyOneId() {
        EntityManager em = emf.createEntityManager();
        DataStoreManyOneIdOwner dsparent = 
            em.find(DataStoreManyOneIdOwner.class, dsid);
        assertNotNull(dsparent);
        assertDataStoreRelations((OpenJPAEntityManager) em, dsparent);
        em.close();
    }

    public void testRetrieveWithCompoundManyOneId() {
        EntityManager em = emf.createEntityManager();
        ManyOneCompoundIdOwnerId oid = new ManyOneCompoundIdOwnerId();
        oid.entityId = id;
        oid.longId = cid;
        ManyOneCompoundIdOwner cparent = 
            em.find(ManyOneCompoundIdOwner.class, oid);
        assertNotNull(cparent);
        assertCompoundRelations((OpenJPAEntityManager) em, cparent);
        em.close();
    }

    public void testAttemptToChangeManyOne() {
        EntityManager em = emf.createEntityManager();
        ManyOneIdOwner parent = em.find(ManyOneIdOwner.class, id);
        assertNotNull(parent);
        assertNotNull(parent.getSelfRel());
        em.getTransaction().begin();
        try {
            parent.setId(parent.getSelfRel().getId()); 
            em.getTransaction().commit();
            fail("Successfully changed id relation.");
        } catch (Exception e) {
            // expected
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }
        em.close();
    }

    public void testChangeRelationToManyOneOwner() {
        BasicEntity id3 = new BasicEntity();
        id3.setName("id3");
        ManyOneIdOwner child2 = new ManyOneIdOwner();
        child2.setName("child2");
        child2.setId(id3);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(id3);
        em.persist(child2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        ManyOneIdOwner parent = em.find(ManyOneIdOwner.class, id);
        assertNotNull(parent);
        em.getTransaction().begin();
        parent.setSelfRel(child2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        parent = em.find(ManyOneIdOwner.class, id);
        child2 = parent.getSelfRel();
        assertEquals("child2", child2.getName());
        assertEquals(id3.getId(), child2.getId().getId());
        em.close();
    }

    public void testChangeRelationToDataStoreManyOneOwner() {
        DataStoreBasicEntity dsid3 = new DataStoreBasicEntity();
        dsid3.setName("dsid3");
        DataStoreManyOneIdOwner dschild2 = new DataStoreManyOneIdOwner();
        dschild2.setName("dschild2");
        dschild2.setId(dsid3);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(dsid3);
        em.persist(dschild2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        DataStoreManyOneIdOwner dsparent = 
            em.find(DataStoreManyOneIdOwner.class, dsid);
        assertNotNull(dsparent);
        em.getTransaction().begin();
        dsparent.setSelfRel(dschild2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        dsparent = em.find(DataStoreManyOneIdOwner.class, dsid);
        dschild2 = dsparent.getSelfRel();
        assertEquals("dschild2", dschild2.getName());
        OpenJPAEntityManager oem = (OpenJPAEntityManager) em;
        assertEquals(oem.getObjectId(dsid3), oem.getObjectId(dschild2.getId()));
        em.close();
    }

    public void testChangeRelationToCompoundManyOneOwner() {
        BasicEntity id3 = new BasicEntity();
        id3.setName("id3");
        ManyOneCompoundIdOwner cchild2 = new ManyOneCompoundIdOwner();
        cchild2.setName("cchild2");
        cchild2.setEntityId(id3);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(id3);
        em.persist(cchild2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        ManyOneCompoundIdOwnerId oid = new ManyOneCompoundIdOwnerId();
        oid.entityId = id;
        oid.longId = cid; 
        ManyOneCompoundIdOwner cparent = em.find(ManyOneCompoundIdOwner.class, 
            oid);
        assertNotNull(cparent);
        em.getTransaction().begin();
        cparent.setSelfRel(cchild2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        cparent = em.find(ManyOneCompoundIdOwner.class, oid);
        cchild2 = cparent.getSelfRel();
        assertEquals("cchild2", cchild2.getName());
        assertEquals(id3.getId(), cchild2.getEntityId().getId());
        em.close();
    }

    public void testQuery() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select e from ManyOneIdOwner e "
            + "where e.id.id = :id");
        q.setParameter("id", id);
        ManyOneIdOwner pc = (ManyOneIdOwner) q.getSingleResult();
        assertNotNull(pc);
        assertEquals("parent", pc.getName());
        em.close();

        em = emf.createEntityManager();
        BasicEntity id1 = em.find(BasicEntity.class, id);
        assertNotNull(id1);
        assertEquals("id1", id1.getName());
        q = em.createQuery("select e from ManyOneIdOwner e where e.id = :id");
        q.setParameter("id", id1);
        pc = (ManyOneIdOwner) q.getSingleResult();
        assertNotNull(pc);
        assertEquals("parent", pc.getName());
        em.close();
    }

    public void testDataStoreQuery() {
        EntityManager em = emf.createEntityManager();
        DataStoreBasicEntity dsid1 = em.find(DataStoreBasicEntity.class, dsid);
        assertNotNull(dsid1);
        assertEquals("dsid1", dsid1.getName());
        Query q = em.createQuery("select e from DataStoreManyOneIdOwner e "
            + "where e.id = :id");
        q.setParameter("id", dsid1);
        DataStoreManyOneIdOwner dspc = (DataStoreManyOneIdOwner) 
            q.getSingleResult();
        assertNotNull(dspc);
        assertEquals("dsparent", dspc.getName());
        em.close();
    }

    public void testCompoundQuery() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select e from ManyOneCompoundIdOwner e "
            + "where e.longId = :cid and e.entityId.id = :id");
        q.setParameter("cid", cid);
        q.setParameter("id", id);
        ManyOneCompoundIdOwner pc = (ManyOneCompoundIdOwner)
            q.getSingleResult();
        assertNotNull(pc);
        assertEquals("cparent", pc.getName());
        em.close();

        em = emf.createEntityManager();
        BasicEntity id1 = em.find(BasicEntity.class, id);
        assertNotNull(id1);
        assertEquals("id1", id1.getName());
        q = em.createQuery("select e from ManyOneCompoundIdOwner e "
            + "where e.longId = :cid and e.entityId = :id");
        q.setParameter("cid", cid);
        q.setParameter("id", id1);
        pc = (ManyOneCompoundIdOwner) q.getSingleResult();
        assertNotNull(pc);
        assertEquals("cparent", pc.getName());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestManyOneAsId.class);
    }
}


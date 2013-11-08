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
 package org.apache.openjpa.persistence.blob;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.apache.openjpa.persistence.query.QueryBuilder;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestBlobs extends SingleEMFTestCase {

    private int _populatedId = 1;
    private int _nullLobId = 2;
    private int _nullBlobId = 3;
    private int _nullBothId = 4;

    QueryBuilder qb = null;

    public void setUp() {
        super.setUp(BlobEntity.class, RETAIN_DATA);
        cleanup();
        populate();
        qb = emf.getDynamicQueryBuilder();
    }

    protected void cleanup() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE from BlobEntity").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    protected void populate() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();

        BlobEntity be = new BlobEntity();
        be.setId(_populatedId);
        be.setLobField("abcdef");
        be.setBlobField("abcdef".getBytes());
        em.persist(be);

        be = new BlobEntity();
        be.setId(_nullLobId);
        be.setLobField(null);
        be.setBlobField("abcdef".getBytes());
        em.persist(be);

        be = new BlobEntity();
        be.setId(_nullBlobId);
        be.setLobField("abcdef");
        be.setBlobField(null);
        em.persist(be);

        be = new BlobEntity();
        be.setId(_nullBothId);
        be.setLobField(null);
        be.setBlobField(null);
        em.persist(be);

        tran.commit();
        em.close();
    }

    public void testNotNullBlobQuery() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Integer> q =
            em.createQuery("SELECT e.id FROM BlobEntity e where e.blobField IS NOT NULL", Integer.class);

        List<Integer> ids = q.getResultList();
        assertTrue(ids.contains(_populatedId));
        assertTrue(ids.contains(_nullLobId));
        assertFalse(ids.contains(_nullBlobId));
        assertFalse(ids.contains(_nullBothId));
        
        em.close();
    }

    public void testNullBlobQuery() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Integer> q =
            em.createQuery("SELECT e.id FROM BlobEntity e where e.blobField IS NULL", Integer.class);

        List<Integer> ids = q.getResultList();
        assertFalse(ids.contains(_populatedId));
        assertFalse(ids.contains(_nullLobId));
        assertTrue(ids.contains(_nullBlobId));
        assertTrue(ids.contains(_nullBothId));
        
        em.close();
    }

    public void testNotNullLobQuery() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Integer> q =
            em.createQuery("SELECT e.id FROM BlobEntity e where e.lobField IS NOT NULL", Integer.class);

        List<Integer> ids = q.getResultList();
        assertTrue(ids.contains(_populatedId));
        assertFalse(ids.contains(_nullLobId));
        assertTrue(ids.contains(_nullBlobId));
        assertFalse(ids.contains(_nullBothId));
        
        em.close();
    }

    public void testNullLobQuery() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Integer> q =
            em.createQuery("SELECT e.id FROM BlobEntity e where e.lobField IS NULL", Integer.class);

        List<Integer> ids = q.getResultList();
        assertFalse(ids.contains(_populatedId));
        assertTrue(ids.contains(_nullLobId));
        assertFalse(ids.contains(_nullBlobId));
        assertTrue(ids.contains(_nullBothId));
        
        em.close();
    }
}

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

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.simple.AllFieldTypes;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestBulkJPQLAndDataCache
    extends SingleEMFTestCase {

    private Object oid;

    public void setUp() throws Exception {
        setUp("openjpa.DataCache", "true",
            "openjpa.QueryCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm",
            CLEAR_TABLES,
            AllFieldTypes.class, CascadeParent.class, CascadeChild.class);

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AllFieldTypes pc = new AllFieldTypes();
        pc.setStringField("DeleteMe");
        em.persist(pc);
        oid = em.getObjectId(pc);
        em.getTransaction().commit();
        em.close();
    }

    public void testBulkDelete() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        List result = em.createQuery("SELECT o FROM AllFieldTypes o")
            .getResultList();
        assertEquals(1, result.size());
        em.createQuery("DELETE FROM AllFieldTypes o").executeUpdate();
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();

        // this assumes that we invalidate the cache, rather than update it
        // according to the bulk rule.
        assertFalse(OpenJPAPersistence.cast(emf).getStoreCache()
            .contains(AllFieldTypes.class, oid));

        assertNull(em.find(AllFieldTypes.class, oid));
        em.close();
    }

    public void testBulkUpdate() {
        OpenJPAEntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        List result = em.createQuery("SELECT o FROM AllFieldTypes o "
            + "WHERE o.intField = 0").getResultList();
        assertEquals(1, result.size());
        em.createQuery("UPDATE AllFieldTypes o SET o.intField = 10")
            .executeUpdate();
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();

        // this assumes that we invalidate the cache, rather than update it
        // according to the bulk rule.
        assertFalse(OpenJPAPersistence.cast(emf).getStoreCache()
            .contains(AllFieldTypes.class, oid));

        em.close();
    }

    public void testBulkDeleteOfCascadingEntity() {
        CascadeParent parent = new CascadeParent();
        parent.setName("parent");
        CascadeChild child = new CascadeChild();
        child.setName("child");
        parent.setChild(child);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(parent);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        assertEquals(1, em.createQuery("SELECT o FROM CascadeParent o").
            getResultList().size());
        assertEquals(1, em.createQuery("SELECT o FROM CascadeChild o").
            getResultList().size());
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM CascadeParent o").executeUpdate();
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        assertEquals(0, em.createQuery("SELECT o FROM CascadeParent o").
            getResultList().size());
        assertEquals(0, em.createQuery("SELECT o FROM CascadeChild o").
            getResultList().size());
        em.close();
    }
}

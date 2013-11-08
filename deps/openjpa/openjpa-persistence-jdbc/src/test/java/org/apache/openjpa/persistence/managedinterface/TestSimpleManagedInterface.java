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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.query.SimpleEntity;
import org.apache.openjpa.kernel.AbstractBrokerFactory;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;

public class TestSimpleManagedInterface
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(SimpleManagedInterface.class, SimpleEntity.class, CLEAR_TABLES);
    }

    public void testMetaDataRepository() {
        AbstractBrokerFactory bf =
            (AbstractBrokerFactory) JPAFacadeHelper.toBrokerFactory(emf);
        bf.makeReadOnly();
        MetaDataRepository repos = bf.getConfiguration()
            .getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getMetaData(SimpleManagedInterface.class,
            null, false);
        assertNotNull(meta);
        assertTrue(meta.isManagedInterface());
        assertEquals(SimpleManagedInterface.class, meta.getDescribedType());
    }

    public void testInterfaceImplGeneration() {
        ((AbstractBrokerFactory) JPAFacadeHelper.toBrokerFactory(emf))
            .makeReadOnly();
        // load metadata to trigger instance creation
        ClassMetaData meta = JPAFacadeHelper.getMetaData(emf,
            SimpleManagedInterface.class);
        assertEquals(SimpleManagedInterface.class, meta.getDescribedType());
    }

    public void testBasicOperations() {
        OpenJPAEntityManager em = emf.createEntityManager();
        SimpleManagedInterface pc =
            em.createInstance(SimpleManagedInterface.class);
        pc.setId(17);
        pc.setString("hello!");
        em.getTransaction().begin();
        em.persist(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(SimpleManagedInterface.class, 17);
        assertNotNull(pc);
        em.getTransaction().begin();
        pc.setString("updated");
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.getReference(SimpleManagedInterface.class, 17));
        em.getTransaction().commit();
        em.close();
    }

    public void testJPQL() {
        EntityManager em = emf.createEntityManager();
        assertEquals(0, em.createQuery("select o from SimpleManagedInterface o")
            .getResultList().size());
        em.close();
    }
}

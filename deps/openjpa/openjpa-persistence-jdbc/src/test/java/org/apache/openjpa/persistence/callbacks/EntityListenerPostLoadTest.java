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
package org.apache.openjpa.persistence.callbacks;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class EntityListenerPostLoadTest extends SingleEMFTestCase {

    public void setUp() {
        setUp(CLEAR_TABLES);
    }

    @Override
    protected String getPersistenceUnitName() {
        return "listener-pu";
    }

    /**
     * If an entity gets merged it is first read from the database prior to the update. In this read step, the
     * &#064;PostLoad get's executed. After I save my entity to the database, the &#064;PostLoad following merge should
     * return exactly the value stored to the database. Even if the value got changed locally in the meantime.
     */
    public void testPostLoadValues() {
        OpenJPAEntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PostLoadListenerEntity entity = null;

            entity = new PostLoadListenerEntity();
            entity.setValue("val1");

            em.persist(entity);
            em.getTransaction().commit();

            // close the EntityManager so our entity is now detached
            em.close();

            // reopen a new EntityManager
            em = emf.createEntityManager();
            assertTrue(em.isDetached(entity));

            em.getTransaction().begin();
            // entity = em.find(PostLoadListenerEntity.class, entity.getId());
            entity = em.find(PostLoadListenerEntity.class, entity.getId());
            assertNotNull(entity);

            // the merge invoked a PostLoad, so this should now be 'val1'
            assertEquals("val1", PostLoadListenerImpl.postLoadValue);

            em.getTransaction().commit();

            // close the EntityManager so our entity is now detached again
            em.close();

            // reopen a new EntityManager
            em = emf.createEntityManager();
            em.getTransaction().begin();
            
            assertTrue(em.isDetached(entity));
            
            entity.setValue("val2");
            //X entity.setValue2("val2");

            entity = em.merge(entity);

            // the merge invoked a PostLoad, and this should now STILL be 'val1'
            assertEquals("val1", PostLoadListenerImpl.postLoadValue);
            em.getTransaction().commit();

            // close the EntityManager so our entity is now detached again
            em.close();

            // reopen a new EntityManager
            em = emf.createEntityManager();
            em.getTransaction().begin();
            entity.setValue("val3");

            entity = em.merge(entity);

            // the merge invoked a PostLoad, and this should now STILL be 'val1'
            assertEquals("val2", PostLoadListenerImpl.postLoadValue);
            em.getTransaction().commit();

        } finally {
            if (em != null && em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (em != null && em.isOpen())
                em.close();
        }

    }
}

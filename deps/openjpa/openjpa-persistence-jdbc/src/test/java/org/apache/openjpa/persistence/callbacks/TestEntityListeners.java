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

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestEntityListeners extends SingleEMFTestCase {

    private static final int ENTITY_LISTENER_ENTITY = 1;
    private static final int GLOBAL_LISTENER_ENTITY = 2;
    private static final int DUPLICATE_LISTENER_ENTITY = 3;
    
    public void setUp() {
        setUp(CLEAR_TABLES);
        ListenerImpl.prePersistCount = 0;
        ListenerImpl.postPersistCount = 0;
        ListenerImpl.preUpdateCount = 0;
        ListenerImpl.postUpdateCount = 0;
        ListenerImpl.preRemoveCount = 0;
        ListenerImpl.postRemoveCount = 0;
        ListenerImpl.postLoadCount = 0;
    }

    @Override
    protected String getPersistenceUnitName() {
        return "listener-pu";
    }

    public void testEntityListeners() {
        helper(ENTITY_LISTENER_ENTITY);
    }

    public void testGlobalListeners() {
        helper(GLOBAL_LISTENER_ENTITY);
    }
    
    public void testDuplicateListeners() {
        super.setUp(CLEAR_TABLES, DuplicateListenerEntity.class, 
            "openjpa.Callbacks", "AllowsDuplicateListener=false");
        assertFalse(emf.getConfiguration().getCallbackOptionsInstance()
            .getAllowsDuplicateListener());
        helper(DUPLICATE_LISTENER_ENTITY);
    }

    public void helper(int entityListeners) {
        OpenJPAEntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ListenerTestEntity o = null;
            switch (entityListeners) {
            case ENTITY_LISTENER_ENTITY:
                o = new EntityListenerEntity();
                break;
            case GLOBAL_LISTENER_ENTITY:
                o = new GlobalListenerEntity();
                break;
            case DUPLICATE_LISTENER_ENTITY:
                o = new DuplicateListenerEntity();
                break;
                
            }
            em.persist(o);

            assertStatus(1, 0, 0, 0, 0, 0, 0);

            em.getTransaction().commit();
            long id = o.getId();
            em.close();

            assertStatus(1, 1, 0, 0, 0, 0, 0);

            em = emf.createEntityManager();
            em.getTransaction().begin();
            switch (entityListeners) {
            case ENTITY_LISTENER_ENTITY:
                o = em.find(EntityListenerEntity.class, id);
                break;
            case GLOBAL_LISTENER_ENTITY:
                o = em.find(GlobalListenerEntity.class, id);
                break;
            case DUPLICATE_LISTENER_ENTITY:
                o = em.find(DuplicateListenerEntity.class, id);
                break;
                
            }
            assertNotNull(o);
            assertStatus(1, 1, 0, 0, 0, 0, 1);

            o.setValue(o.getValue() + 1);

            em.flush();
            assertStatus(1, 1, 1, 1, 0, 0, 1);

            em.remove(o);
            assertStatus(1, 1, 1, 1, 1, 0, 1);

            em.getTransaction().commit();

            assertStatus(1, 1, 1, 1, 1, 1, 1);

            em.close();
        } finally {
            if (em != null && em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (em != null && em.isOpen())
                em.close();
        }
    }

    private void assertStatus(
        int prePersist, int postPersist,
        int preUpdate, int postUpdate,
        int preRemove, int postRemove,
        int postLoad) {
        assertEquals(prePersist, ListenerImpl.prePersistCount);
        assertEquals(postPersist, ListenerImpl.postPersistCount);
        assertEquals(preUpdate, ListenerImpl.preUpdateCount);
        assertEquals(postUpdate, ListenerImpl.postUpdateCount);
        assertEquals(preRemove, ListenerImpl.preRemoveCount);
        assertEquals(postRemove, ListenerImpl.postRemoveCount);
        assertEquals(postLoad, ListenerImpl.postLoadCount);
    }

}

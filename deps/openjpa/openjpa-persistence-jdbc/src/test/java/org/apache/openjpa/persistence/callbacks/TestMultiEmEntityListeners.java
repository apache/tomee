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
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestMultiEmEntityListeners extends SingleEMFTestCase {

    public void setUp() {
        setUp(CLEAR_TABLES, ListenerInEntity.class, AddListenerEntity.class
//                , "openjpa.Compatibility", "SingletonLifecycleEventManager=true"
            );
    }

    public void testListenerInEntity1() {
        OpenJPAEntityManager em1 = null;
        OpenJPAEntityManager em2 = null;
        try {
            em1 = emf.createEntityManager();
            em2 = emf.createEntityManager();

            ListenerInEntity o1 = new ListenerInEntity();
            ListenerInEntity o2 = new ListenerInEntity();

            em1.getTransaction().begin();
            em1.persist(o1);
            assertListenerInEntityStatus(o1, 1, 0, 0, 0, 0, 0, 0);
            assertListenerInEntityStatus(o2, 0, 0, 0, 0, 0, 0, 0);

            em2.getTransaction().begin();
            em2.persist(o2);
            assertListenerInEntityStatus(o1, 1, 0, 0, 0, 0, 0, 0);
            assertListenerInEntityStatus(o2, 1, 0, 0, 0, 0, 0, 0);

            em2.getTransaction().commit();
            long id2 = o2.getId();
            assertListenerInEntityStatus(o1, 1, 0, 0, 0, 0, 0, 0);
            assertListenerInEntityStatus(o2, 1, 1, 0, 0, 0, 0, 0);

            em1.getTransaction().commit();
            long id1 = o1.getId();
            assertListenerInEntityStatus(o1, 1, 1, 0, 0, 0, 0, 0);
            assertListenerInEntityStatus(o2, 1, 1, 0, 0, 0, 0, 0);

            em1.clear();
            ListenerInEntity fo1 = em1.find(ListenerInEntity.class, id1);
            assertNotNull(fo1);
            assertListenerInEntityStatus(fo1, 0, 0, 0, 0, 0, 0, 1);

            em2.clear();
            ListenerInEntity fo2 = em2.find(ListenerInEntity.class, id2);
            assertNotNull(fo2);
            assertListenerInEntityStatus(fo1, 0, 0, 0, 0, 0, 0, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 0, 0, 0, 0, 1);

            em1.getTransaction().begin();
            fo1.setValue(fo1.getValue() + 1);

            em1.flush();
            assertListenerInEntityStatus(fo1, 0, 0, 1, 1, 0, 0, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 0, 0, 0, 0, 1);

            em2.getTransaction().begin();
            fo2.setValue(fo2.getValue() + 1);

            em2.flush();
            assertListenerInEntityStatus(fo1, 0, 0, 1, 1, 0, 0, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 1, 1, 0, 0, 1);

            em1.remove(fo1);
            assertListenerInEntityStatus(fo1, 0, 0, 1, 1, 1, 0, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 1, 1, 0, 0, 1);

            em2.remove(fo2);
            assertListenerInEntityStatus(fo1, 0, 0, 1, 1, 1, 0, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 1, 1, 1, 0, 1);

            em1.getTransaction().commit();

            assertListenerInEntityStatus(fo1, 0, 0, 1, 1, 1, 1, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 1, 1, 1, 0, 1);

            em2.getTransaction().commit();

            assertListenerInEntityStatus(fo1, 0, 0, 1, 1, 1, 1, 1);
            assertListenerInEntityStatus(fo2, 0, 0, 1, 1, 1, 1, 1);

            em1.close();
            em2.close();
        } finally {
            if (em1 != null && em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em1 != null && em1.isOpen())
                em1.close();
            if (em2 != null && em2.getTransaction().isActive())
                em2.getTransaction().rollback();
            if (em2 != null && em2.isOpen())
                em2.close();
        }
    }

    private void assertListenerInEntityStatus(ListenerInEntity l,
        int prePersist, int postPersist,
        int preUpdate, int postUpdate,
        int preRemove, int postRemove,
        int postLoad) {
        assertEquals(prePersist, l.prePersistCount);
        assertEquals(postPersist, l.postPersistCount);
        assertEquals(preUpdate, l.preUpdateCount);
        assertEquals(postUpdate, l.postUpdateCount);
        assertEquals(preRemove, l.preRemoveCount);
        assertEquals(postRemove, l.postRemoveCount);
        assertEquals(postLoad, l.postLoadCount);
    }

    public void testAddListenerEntity1() {
        OpenJPAEntityManager em1 = null;
        OpenJPAEntityManager em2 = null;
        try {
            em1 = emf.createEntityManager();
            PerInstanceListener l1 = new PerInstanceListener();
            ((OpenJPAEntityManagerSPI) em1).addLifecycleListener(l1, (Class<?>[])null);

            em2 = emf.createEntityManager();
            PerInstanceListener l2 = new PerInstanceListener();
            ((OpenJPAEntityManagerSPI) em2).addLifecycleListener(l2, (Class<?>[])null);

            AddListenerEntity o1 = new AddListenerEntity();
            AddListenerEntity o2 = new AddListenerEntity();

            em1.getTransaction().begin();
            em1.persist(o1);
            assertAddListenerEntityStatus(l1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);
            assertAddListenerEntityStatus(l2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

            em2.getTransaction().begin();
            em2.persist(o2);
            assertAddListenerEntityStatus(l1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);

            em2.getTransaction().commit();
            assertAddListenerEntityStatus(l1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0);
            long id2 = o2.getId();

            em1.getTransaction().commit();
            assertAddListenerEntityStatus(l1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0);
            long id1 = o1.getId();

            em1.clear();
            AddListenerEntity fo1 = em1.find(AddListenerEntity.class, id1);
            assertAddListenerEntityStatus(l1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0);
            assertNotNull(fo1);

            em2.clear();
            AddListenerEntity fo2 = em2.find(AddListenerEntity.class, id2);
            assertAddListenerEntityStatus(l1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            assertNotNull(fo2);

            em1.getTransaction().begin();
            fo1.setValue(fo1.getValue() + 1);
            assertAddListenerEntityStatus(l1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0);

            em1.flush();
            assertAddListenerEntityStatus(l1, 1, 1, 2, 2, 1, 0, 1, 1, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0);

            em2.getTransaction().begin();
            fo2.setValue(fo2.getValue() + 1);
            assertAddListenerEntityStatus(l1, 1, 1, 2, 2, 1, 0, 1, 1, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 1, 1, 1, 0, 1, 1, 0, 0);

            em2.flush();
            assertAddListenerEntityStatus(l1, 1, 1, 2, 2, 1, 0, 1, 1, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 2, 2, 1, 0, 1, 1, 0, 0);

            em2.remove(fo2);
            assertAddListenerEntityStatus(l1, 1, 1, 2, 2, 1, 0, 1, 1, 0, 0);
            assertAddListenerEntityStatus(l2, 1, 1, 2, 2, 1, 0, 1, 1, 1, 1);

            em1.remove(fo1);
            assertAddListenerEntityStatus(l1, 1, 1, 2, 2, 1, 0, 1, 1, 1, 1);
            assertAddListenerEntityStatus(l2, 1, 1, 2, 2, 1, 0, 1, 1, 1, 1);

            em1.getTransaction().commit();
            em2.getTransaction().commit();

            em1.close();
            em2.close();
        } finally {
            if (em1 != null && em1.getTransaction().isActive())
                em1.getTransaction().rollback();
            if (em1 != null && em1.isOpen())
                em1.close();
            if (em2 != null && em2.getTransaction().isActive())
                em2.getTransaction().rollback();
            if (em2 != null && em2.isOpen())
                em2.close();
        }
    }

    private void assertAddListenerEntityStatus(PerInstanceListener l
            , int beforePersist, int afterPersist
            , int beforeStore, int afterStore
            , int afterLoad, int afterRefresh
            , int beforeDirty, int afterDirty
            , int beforeDelete, int afterDelete) {
        assertEquals(beforePersist, l.beforePersist);
        assertEquals(afterPersist, l.afterPersist);
        assertEquals(beforeStore, l.beforeStore);
        assertEquals(afterStore, l.afterStore);
        assertEquals(afterLoad, l.afterLoad);
        assertEquals(afterRefresh, l.afterRefresh);
        assertEquals(beforeDirty, l.beforeDirty);
        assertEquals(afterDirty, l.afterDirty);
        assertEquals(beforeDelete, l.beforeDelete);
        assertEquals(afterDelete, l.afterDelete);
    }
}

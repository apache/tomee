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
 * Unless required by applicable law or agEmployee_Last_Name to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.compat;

import javax.persistence.Query;

import org.apache.openjpa.persistence.callbacks.Message;
import org.apache.openjpa.persistence.callbacks.MessageListenerImpl;
import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * <b>TestCompatibile</b> is used to test various backwards compatibility scenarios between JPA 2.0 and JPA 1.2
 * 
 * <p>The following scenarios are tested:
 * <ol>
 * <li>preUpdate and postUpdate behavior in JPA 2.0 is different than in 1.2
 * <li>TBD
 * </ol>
 * <p> 
 * <b>Note(s):</b>
 * <ul>
 * <li>The proper openjpa.Compatibility value(s) must be provided in order for the testcases to succeed
 * </ul>
 */
public class TestMessageListener extends SingleEMFTestCase {

    public void setUp() {
        setUp(Message.class, 
              "openjpa.Compatibility", "default",
              DROP_TABLES);
    }

    public void testUpdateInPreUpdate() {
        // Create a new EntityManager from the EntityManagerFactory. The
        // EntityManager is the main object in the persistence API, and is
        // used to create, delete, and query objects, as well as access
        // the current transaction
        OpenJPAEntityManager em = emf.createEntityManager();
        try {
            // Begin a new local transaction so that we can persist a new entity
            em.getTransaction().begin();

            MessageListenerImpl.resetCounters();

            // Create and persist a new Message entity
            Message message = new Message("Hello Persistence!");
            assertNull("Test message's created field to be null.", message.getCreated());
            assertNull("Test message's updated field to be null.", message.getUpdated());

            em.persist(message);

            // Pre-persist invoked, created and updated fields set
            assertStatus(1, 0, 0, 0, 0, 0, 0);
            assertNotNull("Test message's created field being set.", message.getCreated());
            assertNotNull("Test message's updated field being set.", message.getUpdated());

            // Perform a simple query to get the Message
            Query q = em.createQuery("select m from Message m where m.id=" + message.getId());
            Message m = (Message) q.getSingleResult();
            assertEquals("Test first expected message.", "Hello Persistence!",m.getMessage());
            assertNotNull("Test message's created field being set.", m.getCreated());
            assertNotNull("Test message's updated field being set.", m.getUpdated());

            // Query cause flush to occur, hence fire the postPersist event
            assertStatus(1, 1, 0, 0, 0, 0, 0);

            // Create and persist another new Message entity
            message = new Message("Hello Persistence 2!");
            assertNull("Test message's created field to be null.", message.getCreated());
            assertNull("Test message's updated field to be null.", message.getUpdated());

            em.persist(message);

            // Pre-persist invoked, created and updated fields set
            assertStatus(2, 1, 0, 0, 0, 0, 0);
            assertNotNull("Test message's created field being set.", message.getCreated());
            assertNotNull("Test message's updated field being set.", message.getUpdated());

            em.getTransaction().commit();

            // Complete the 2nd @postPersist
            // preUpdate and postUpdate are called in 1.2.x but not in 1.3 or later
            // See JPA2 Spec 3.5.2 Note about this being implementation dependent
            if ((OpenJPAVersion.MAJOR_RELEASE >= 2) ||
                ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3))) {
                assertStatus(2, 2, 0, 0, 0, 0, 0);
            } else {
                // prior to 2.0, pre/postUpdate was called
                assertStatus(2, 2, 1, 1, 0, 0, 0);
            }            

            // Make an update to trigger the pre/postUpdater callbacks
            em.getTransaction().begin();
            message = em.find(Message.class,message.getId());
            message.setMessage("Update field and trigger pre/postUpdate");
            em.getTransaction().commit();

            // Complete the 2nd @postPersist
            if ((OpenJPAVersion.MAJOR_RELEASE >= 2) ||
                ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                    (OpenJPAVersion.MINOR_RELEASE >= 3))) {
                assertStatus(2, 2, 1, 1, 0, 0, 0);
            } else {
                assertStatus(2, 2, 2, 2, 0, 0, 0);
            }            
        }
        finally {
            if (em != null && em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (em != null && em.isOpen())
                em.close();
        }
    } 

    public void testUpdateInPreUpdate2() {
        // Create a new EntityManager from the EntityManagerFactory. The
        // EntityManager is the main object in the persistence API, and is
        // used to create, delete, and query objects, as well as access
        // the current transaction
        OpenJPAEntityManager em = emf.createEntityManager();
        try {
            // Begin a new local transaction so that we can persist a new entity
            em.getTransaction().begin();

            MessageListenerImpl.resetCounters();

            // Create and persist a new Message entity
            Message message = new Message("Hello Persistence!");
            assertNull("Test message's created field to be null.", message.getCreated());
            assertNull("Test message's updated field to be null.", message.getUpdated());

            em.persist(message);

            // Pre-persist invoked, created and updated fields set
            assertStatus(1, 0, 0, 0, 0, 0, 0);
            assertNotNull("Test message's created field being set.", message.getCreated());
            assertNotNull("Test message's updated field being set.", message.getUpdated());

            // Perform a simple query to get the Message
            Query q = em.createQuery("select m from Message m where m.id=" + message.getId());
            Message m = (Message) q.getSingleResult();
            assertEquals("Test first expected message.", "Hello Persistence!",m.getMessage());
            assertNotNull("Test message's created field being set.", m.getCreated());
            assertNotNull("Test message's updated field being set.", m.getUpdated());

            // Query cause flush to occur, hence fire the postPersist event
            assertStatus(1, 1, 0, 0, 0, 0, 0);

            // Create and persist another new Message entity
            message = new Message("Hello Persistence 2!");
            assertNull("Test message's created field to be null.", message.getCreated());
            assertNull("Test message's updated field to be null.", message.getUpdated());

            em.persist(message);

            // Pre-persist invoked, created and updated fields set
            assertStatus(2, 1, 0, 0, 0, 0, 0);
            assertNotNull("Test message's created field being set.", message.getCreated());
            assertNotNull("Test message's updated field being set.", message.getUpdated());

            // Update the entity before committing
            message.setMessage("Combined Create and Update triggers");
            // preUpdate and postUpdate are called in 1.2.x but not in 1.3 or later
            // See JPA2 Spec 3.5.2 Note about this being implementation dependent
            if ((OpenJPAVersion.MAJOR_RELEASE >= 2) ||
                ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3))) {
                assertStatus(2, 1, 0, 0, 0, 0, 0);
            } else {
                // prior to 2.0, pre/postUpdate was called
                assertStatus(2, 1, 1, 1, 0, 0, 0);
            }            

            em.getTransaction().commit();

            // Complete the 2nd @postPersist
            // preUpdate and postUpdate are called in 1.2.x but not in 1.3 or later
            if ((OpenJPAVersion.MAJOR_RELEASE >= 2) ||
                ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3))) {
                assertStatus(2, 2, 0, 0, 0, 0, 0);
            } else {
                assertStatus(2, 2, 1, 1, 0, 0, 0);
            }            

            // Make an update to trigger the pre/postUpdater callbacks
            em.getTransaction().begin();
            message = em.find(Message.class,message.getId());
            message.setMessage("Update field and trigger pre/postUpdate");
            em.getTransaction().commit();

            // Complete the 2nd @postPersist
            if ((OpenJPAVersion.MAJOR_RELEASE >= 2) ||
                ((OpenJPAVersion.MAJOR_RELEASE == 1) &&
                 (OpenJPAVersion.MINOR_RELEASE >= 3))) {
                assertStatus(2, 2, 1, 1, 0, 0, 0);
            } else {
                assertStatus(2, 2, 2, 2, 0, 0, 0);
            }            
        }
        finally {
            if (em != null && em.getTransaction().isActive())
                em.getTransaction().rollback();
            if (em != null && em.isOpen())
                em.close();
        }
    } 

    private void assertStatus(int prePersist, int postPersist, int preUpdate, 
                              int postUpdate, int preRemove, int postRemove, int postLoad) {
        assertEquals("prePersist", prePersist, MessageListenerImpl.prePersistCount);
        assertEquals("postPersist", postPersist, MessageListenerImpl.postPersistCount);
        assertEquals("preUpdate", preUpdate, MessageListenerImpl.preUpdateCount);
        assertEquals("postUpdate", postUpdate, MessageListenerImpl.postUpdateCount);
        assertEquals("preRemove", preRemove, MessageListenerImpl.preRemoveCount);
        assertEquals("postRemove", postRemove, MessageListenerImpl.postRemoveCount);
        assertEquals("postLoad", postLoad, MessageListenerImpl.postLoadCount);
    }
}


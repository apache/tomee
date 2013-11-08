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
package org.apache.openjpa.persistence.detachment;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestGetReferenceAndImplicitDetachment
    extends SingleEMFTestCase {

    public void setUp() {
        setUp("openjpa.DetachState", "fetch-groups",
            DetachmentOneManyParent.class, DetachmentOneManyChild.class);
    }

    public void testNonexistentGetReferenceDetachmentInTxWithCommit() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DetachmentOneManyParent o = 
            em.getReference(DetachmentOneManyParent.class, 0);
        em.getTransaction().commit();
        em.close();

        // the close detachment should leave these invalid objects in a 
        // transient state
        assertFalse(((PersistenceCapable) o).pcIsTransactional());
        assertFalse(((PersistenceCapable) o).pcIsPersistent());
        // pcIsDetached() will give a false positive in this configuration
        // assertFalse(((PersistenceCapable) o).pcIsDetached());
    }

    public void testNonexistentGetReferenceDetachmentOutsideTx() {
        EntityManager em = emf.createEntityManager();
        DetachmentOneManyParent o = 
            em.getReference(DetachmentOneManyParent.class, 0);
        em.close();

        // the close detachment should leave these invalid objects in a 
        // transient state
        assertFalse(((PersistenceCapable) o).pcIsTransactional());
        assertFalse(((PersistenceCapable) o).pcIsPersistent());
        // pcIsDetached() will give a false positive in this configuration
        // assertFalse(((PersistenceCapable) o).pcIsDetached());
    }

    public void testNonexistentGetReferenceDetachmentInTxWithRollback() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DetachmentOneManyParent o = 
            em.getReference(DetachmentOneManyParent.class, 0);
        em.getTransaction().rollback();

        // the rollback should cause a detachment
        assertFalse(OpenJPAPersistence.cast(em).isTransactional(o));
        assertFalse(OpenJPAPersistence.cast(em).isPersistent(o));
        // pcIsDetached() will give a false positive in this configuration
        // assertFalse(OpenJPAPersistence.cast(em).isDetached(o));

        em.close();
    }

    public void testNonexistentGetReferenceDetachmentInTxWithFailedCommit() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        DetachmentOneManyParent o = 
            em.getReference(DetachmentOneManyParent.class, 0);
        em.getTransaction().setRollbackOnly();
        try {
            em.getTransaction().commit();
        } catch (RollbackException re) {
            // expected
        }

        // the failed commit should cause a detachment
        assertFalse(OpenJPAPersistence.cast(em).isTransactional(o));
        assertFalse(OpenJPAPersistence.cast(em).isPersistent(o));
        // pcIsDetached() will give a false positive in this configuration
        // assertFalse(OpenJPAPersistence.cast(em).isDetached(o));

        em.close();
    }
}

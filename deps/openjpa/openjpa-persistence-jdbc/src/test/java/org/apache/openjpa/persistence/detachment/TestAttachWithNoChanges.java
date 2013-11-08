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
import javax.persistence.OptimisticLockException;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that attaching an instance without having changed it still overwrites
 * any changes to the managed copy.
 *
 * @author Abe White
 */
public class TestAttachWithNoChanges
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(DetachmentOneManyParent.class, DetachmentOneManyChild.class);
    }

    public void testAttachWithNoChangesChecksVersion() {
        DetachmentOneManyChild e = new DetachmentOneManyChild();
        DetachmentOneManyParent p = new DetachmentOneManyParent();
        e.setName("orig");
        p.addChild(e);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(p);
        em.persist(e);
        em.flush();
        em.clear();
        
        DetachmentOneManyChild changed = em.find(DetachmentOneManyChild.class,
            e.getId()); 
        changed.setName("newname");
        em.flush();

        em.merge(e);
        try {
            em.flush();
            fail("Should not be able to flush old version over new.");
        } catch (OptimisticLockException ole) {
            // expected
        } finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
    }

    public static void main(String[] args) {
        TestRunner.run(TestAttachWithNoChanges.class);
    }
}


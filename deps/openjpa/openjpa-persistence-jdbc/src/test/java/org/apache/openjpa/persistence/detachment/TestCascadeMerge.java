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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.kernel.EntityF;
import org.apache.openjpa.jdbc.kernel.EntityG;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCascadeMerge extends SingleEMFTestCase {

    private EntityG enG1;
    private EntityG enG2;

    public void setUp() {
        setUp(CLEAR_TABLES,EntityF.class, EntityG.class);

        createTestData();
    }

    private void createTestData() {
        enG1 = new EntityG();
        enG1.setId(1);

        enG2 = new EntityG();
        enG2.setId(2);
    }

    public void testCascadeMerge() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityF enF = new EntityF();
            enF.setId(1);
            em.persist(enF);
            em.getTransaction().commit();
            em.close();

            em = emf.createEntityManager();
            em.getTransaction().begin();

            EntityF enF1 = em.find(EntityF.class, 1);
            enG1.setEntityF(enF1);
            enG2.setEntityF(enF1);
            List<EntityG> l = new ArrayList<EntityG>();
            l.add(enG1);
            l.add(enG2);
            enF1.setListG(l);
            em.merge(enG1);
            em.getTransaction().commit();
        }
        finally {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            em.close();
        }
    }
}

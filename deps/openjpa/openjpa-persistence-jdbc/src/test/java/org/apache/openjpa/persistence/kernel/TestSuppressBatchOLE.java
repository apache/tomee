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
package org.apache.openjpa.persistence.kernel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.RollbackException;

import org.apache.openjpa.jdbc.kernel.EntityA;
import org.apache.openjpa.jdbc.kernel.EntityB;
import org.apache.openjpa.jdbc.kernel.EntityC;
import org.apache.openjpa.jdbc.kernel.EntityD;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestSuppressBatchOLE extends SingleEMFTestCase {
    @Override
    public void setUp() throws Exception {
        setUp(CLEAR_TABLES, org.apache.openjpa.jdbc.kernel.EntityA.class, org.apache.openjpa.jdbc.kernel.EntityB.class,
            org.apache.openjpa.jdbc.kernel.EntityC.class, org.apache.openjpa.jdbc.kernel.EntityD.class,
            "openjpa.BrokerImpl", "SuppressBatchOLELogging=true", "openjpa.jdbc.DBDictionary", "batchLimit=-1",
            "openjpa.DataCache", "false");
    }

    public void test() throws Exception {
        OpenJPAEntityManagerSPI em1 = emf.createEntityManager();
        OpenJPAEntityManagerSPI em2 = emf.createEntityManager();

        em1.getTransaction().begin();
        List<EntityA> entities = new ArrayList<EntityA>();
        for (int i = 0; i < 25; i++) {
            EntityA a = createEntity();
            entities.add(a);
            em1.persist(a);
        }
        em1.getTransaction().commit();
        em1.clear();

        em1.getTransaction().begin();
        em2.getTransaction().begin();
        for (EntityA a : entities) {
            EntityA a1 = em1.find(EntityA.class, a.getId());
            EntityA a2 = em2.find(EntityA.class, a.getId());
            a1.setName("asdf");
            a2.setName("asdf2");
        }
        em1.getTransaction().commit();
        try {
            em2.getTransaction().commit();
        } catch (RollbackException e) {
            assertTrue(e.getMessage().contains("Suppressing"));
        }

    }

    EntityA createEntity() {
        EntityA res = new EntityA();
        EntityB b = new EntityB();
        EntityC c = new EntityC();
        EntityD d = new EntityD();

        res.setEntityB(b);
        b.setEntityC(c);
        c.setEntityD(d);

        return res;
    }

}

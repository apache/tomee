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

import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.AImplB;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBGetObjectByIdValue extends AbstractTestCase {

    public TestEJBGetObjectByIdValue(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class, AImplB.class);

        EntityManager em = currentEntityManager();
        startTx(em);
        em.flush();
        endTx(em);
        endEm(em);
    }

    public void testDatastore() {
        EntityManager em = currentEntityManager();
        startTx(em);

        RuntimeTest1 pc = new RuntimeTest1(1);
        pc.setStringField("foo");
        em.persist(pc);

        endTx(em);
        endEm(em);

        em = currentEntityManager();
        pc = em.find(RuntimeTest1.class, 1);
        assertEquals("foo", pc.getStringField());
        em.close();

        em = currentEntityManager();
        pc = em.find(RuntimeTest1.class, pc.getIntField());
        assertEquals("foo", pc.getStringField());
        endEm(em);
    }

    public void testAppId() {
        EntityManager em = currentEntityManager();
        startTx(em);
        AImplB pc = new AImplB("foo", 1, "pk");
        em.persist(pc);
        AImplB.Idkey oid = new AImplB.Idkey();
        oid.pk1 = 1;
        oid.pk2 = "pk";
        endTx(em);
        endEm(em);

        em = currentEntityManager();
        pc = em.find(AImplB.class, oid);
        assertEquals("foo", pc.getName());
        endEm(em);

        em = currentEntityManager();
        pc = em.find(AImplB.class, oid.toString());
        assertEquals("foo", pc.getName());
        endEm(em);
    }
}

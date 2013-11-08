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
package org.apache.openjpa.event;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.simple.AllFieldTypes;

public class TestLifecycleListener
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class, CLEAR_TABLES);
    }

    public void testMutationsInLifecycleListener() {
        emf.addLifecycleListener(new AbstractLifecycleListener() {
            @Override
            public void beforePersist(LifecycleEvent event) {
                ((AllFieldTypes) event.getSource()).setLongField(17);
            }
        }, (Class[]) null);

        AllFieldTypes aft = new AllFieldTypes();
        aft.setStringField("foo");
        aft.setIntField(5);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(aft);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        aft = (AllFieldTypes) em.createQuery("select o from AllFieldTypes o")
            .getSingleResult();
        // is changed in the listener impl
        assertEquals(17, aft.getLongField());
        em.close();
    }
}

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

import javax.persistence.Query;

import org.apache.openjpa.persistence.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestMultipleInsertDeleteSameId
    extends SingleEMTestCase {

    public void setUp() {
        setUp(RuntimeTest1.class, CLEAR_TABLES);
    }

    public void testMultipleInsertDelete() {
        em.getTransaction().begin();

        RuntimeTest1 o = new RuntimeTest1("one", 99);
        em.persist(o);
        Query q = em.createQuery("select o from RuntimeTest1 o "
          + " where o.stringField = 'one'"); 
        assertEquals(o, q.getSingleResult());

        em.remove(o);
        assertEquals(0, q.getResultList().size());
      
        RuntimeTest1 o2 = new RuntimeTest1("two", 99);
        em.persist(o2);
        q = em.createQuery("select o from RuntimeTest1 o "
          + " where o.stringField = 'two'"); 
        assertEquals(o2, q.getSingleResult());

        em.remove(o2);
        assertEquals(0, q.getResultList().size());

        em.getTransaction().commit();
        assertNull(em.find(RuntimeTest1.class, 99));
        em.close();
    }
}

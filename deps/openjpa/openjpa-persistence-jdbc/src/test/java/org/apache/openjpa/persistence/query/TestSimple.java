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
package org.apache.openjpa.persistence.query;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;


import org.apache.openjpa.persistence.query.common.apps.Entity1;

public class TestSimple extends BaseQueryTest {

    private Query q = null;

    public TestSimple(String test) {
        super(test);
    }

    public void setUp() {
        deleteAll(Entity1.class);
    }

    public void testSimple() throws java.io.IOException {
        // test create
        {
            EntityManager em = currentEntityManager();
            startTx(em);
            em.persist(new Entity1(0, "testSimple", 12));
            endTx(em);
            endEm(em);
        }

        // test Query
        {
            EntityManager em = currentEntityManager();
            startTx(em);
            em.persist(new Entity1(1, "testSimple", 12));
            List<Entity1> l =
                em.createQuery("SELECT o FROM Entity1 o " + "WHERE o.stringField = 'testSimple'", Entity1.class)
                    .getResultList();
            assertSize(2, l);
            rollbackTx(em);
            em.clear();
            endEm(em);
        }
        

        // test Update
        {
            EntityManager em = currentEntityManager();
            startTx(em);
            ((Entity1) em.createQuery("SELECT o FROM Entity1 o "
                + "WHERE o.stringField = 'testSimple'").getSingleResult())
                .setStringField("testSimple2");
            endTx(em);
            endEm(em);

            em = currentEntityManager();
            startTx(em);
            q = em.createQuery("SELECT o FROM Entity1 o "
                + "WHERE o.stringField = 'testSimple'");
            assertSize(0, q);
            q = em.createQuery("SELECT o FROM Entity1 o "
                + "WHERE o.stringField = 'testSimple2'");
            assertSize(1, q);
            endTx(em);
            endEm(em);
        }

        // test delete
        {
            EntityManager em = currentEntityManager();
            startTx(em);
            em.remove(em.createQuery("SELECT o FROM Entity1 o "
                + "WHERE o.stringField = 'testSimple2'").getSingleResult());
            endTx(em);
            endEm(em);

            em = currentEntityManager();
            startTx(em);

            q = em.createQuery("SELECT o FROM Entity1 o "
                + "WHERE o.stringField = 'testSimple2'");
            assertSize(0, q);
            endTx(em);
            endEm(em);
        }
    }
}

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

package org.apache.openjpa.persistence.relations;

import java.util.Collection;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.PersistenceException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestJoinedMultiInheritanceHierarchy
    extends SingleEMFTestCase {

    public void setUp() {
        super.setUp( CLEAR_TABLES, ChildChildClass.class, ChildClass.class,
                     GrandChildClass.class, ParentClass.class,
                     "openjpa.BrokerImpl", "EvictFromDataCache=true" );
    }

    public void testCacheSqlGeneration() throws PersistenceException {
        OpenJPAEntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        final GrandChildClass notEmpty = new GrandChildClass();
        notEmpty.setName("Not empty object");
        Collection<String> itemSet = notEmpty.getItems();
        for (int i = 0; i < 5; i++) {
            itemSet.add(notEmpty.getName() + " : item n." + i);
        }

        notEmpty.setItems(itemSet);

        final GrandChildClass empty = new GrandChildClass();
        empty.setName("empty object");

        em.persist(notEmpty);
        em.persist(empty);
        em.getTransaction().commit();

        em.evictAll();
        assertTrue(empty.getItems().isEmpty());
    }

}



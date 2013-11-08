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

import org.apache.openjpa.persistence.relations.entity.IncompleteRelationshipChildEntity;
import org.apache.openjpa.persistence.relations.entity.IncompleteRelationshipParentEntity;
import org.apache.openjpa.persistence.relations.entity.IncompleteRelationshipSubclass;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import javax.persistence.EntityManager;

/**
 * A test case that tests for incomplete foreign-key relationships and ensures
 * that proper database mechanics are enforced.
 */
public class TestIncompleteRelationship extends SingleEMFTestCase {
    private static final String CLIENT_ID = "00000-00000-00000-00000-00000-00000";

    private static final String DISCOUNTS[] = {
        "Five-Finger Discount",
        "Staff Discount"
    };

    @Override
    public void setUp() {
        super.setUp(CLEAR_TABLES,
                    IncompleteRelationshipParentEntity.class,
                    IncompleteRelationshipParentEntity.IncompleteRelationshipParentEntityPk.class,
                    IncompleteRelationshipChildEntity.class,
                    IncompleteRelationshipChildEntity.IncompleteRelationshipChildEntityPk.class,
                    IncompleteRelationshipSubclass.class,
                    "openjpa.jdbc.QuerySQLCache", "true");
    }

    public void testIncompleteRelationship() {
        final EntityManager em = emf.createEntityManager();
        IncompleteRelationshipSubclass parent = null;
        IncompleteRelationshipChildEntity child = null;

        em.getTransaction().begin();
        for (String s : DISCOUNTS) {
            child = new IncompleteRelationshipChildEntity(s, CLIENT_ID);
            em.persist(child);
        }
        em.getTransaction().commit();

        for (int i = 1; i < 100; i++) {
            em.getTransaction().begin();

            parent = new IncompleteRelationshipSubclass(i, CLIENT_ID);
//            parent.setChild(child);

            em.persist(parent);
            em.getTransaction().commit();

            parent = (IncompleteRelationshipSubclass)
                    em.createQuery("SELECT i "+
                                   "FROM IncompleteRelationshipSubclass i "+
                                   "WHERE   i.pk.id = :id "+
                                   "AND     i.pk.clientId = :clientId ")
              .setParameter("id", i)
              .setParameter("clientId", CLIENT_ID)
              .getSingleResult();

            assertEquals(parent.getPk().getId(), i);
            assertNull(parent.getChild());
        }
    }
}

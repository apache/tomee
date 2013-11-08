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
package org.apache.openjpa.persistence.jpql;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.util.EagerEmbed;
import org.apache.openjpa.persistence.util.EagerEmbedRel;
import org.apache.openjpa.persistence.util.EagerEntity;

public class TestOneToManySubQuery extends SingleEMFTestCase {

    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, EagerEntity.class, EagerEmbed.class, EagerEmbedRel.class);
    }

    public void test() {
        EntityManager em = emf.createEntityManager();
        try {
            assertEquals(0, em.createQuery(
                "SELECT e FROM EagerEntity e WHERE EXISTS (SELECT e1 FROM e.eagerSelf e1 WHERE e1.id = 0)"
                    + " OR EXISTS (SELECT e1 FROM e.eagerSelf e1 WHERE e1.id = 1)", EagerEntity.class).getResultList()
                .size());

        } finally {
            em.close();

        }
    }
}

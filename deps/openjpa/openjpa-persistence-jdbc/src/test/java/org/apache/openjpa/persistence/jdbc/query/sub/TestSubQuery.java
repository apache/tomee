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
package org.apache.openjpa.persistence.jdbc.query.sub;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestSubQuery extends SingleEMFTestCase {
    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, MaxQueryEntity.class, MaxQueryMapEntity.class);
        populate();
    }

    public void populate() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();

        MaxQueryEntity mqe = new MaxQueryEntity(1, 1, 1);
        MaxQueryMapEntity mqme = new MaxQueryMapEntity(1, 1, "A1");
        MaxQueryMapEntity mqme2 = new MaxQueryMapEntity(2, 2, "A2");
        mqme.setRefEntity(mqe);
        mqme2.setRefEntity(mqe);
        em.persist(mqe);
        em.persist(mqme);
        em.persist(mqme2);

        mqe = new MaxQueryEntity(2, 2, 1);
        mqme = new MaxQueryMapEntity(3, 1, "B1");
        mqme.setRefEntity(mqe);
        em.persist(mqe);
        em.persist(mqme);
        mqme = new MaxQueryMapEntity(4, 2, "B2");
        mqme.setRefEntity(mqe);
        em.persist(mqme);
        mqme = new MaxQueryMapEntity(5, 3, "B3");
        mqme.setRefEntity(mqe);
        em.persist(mqme);

        mqe = new MaxQueryEntity(3, 3, 1);
        mqme = new MaxQueryMapEntity(6, 4, "C1");
        mqme.setRefEntity(mqe);
        em.persist(mqe);
        em.persist(mqme);

        tran.commit();
        em.close();
    }

    public void test() {
        EntityManager em = emf.createEntityManager();

        Query query =
            em
                .createQuery("SELECT e FROM MaxQueryEntity e, MaxQueryMapEntity map "
                    + "WHERE "
//                    + " map.selectCriteria = 'B3' "
//                    + "  AND map.refEntity = e "
//                    + "  AND e.revision = ( SELECT MAX(e_.revision)"
//                    + "                     FROM MaxQueryEntity e_"
//                    + "                     WHERE e_.domainId = e.domainId )"
//                    + "  AND "
                    + " map.revision = ( SELECT MAX(map_.revision)"
                    + "                       FROM MaxQueryMapEntity map_"
                    + "                       WHERE map_.refEntity = map.refEntity )"
                    );

//        assertEquals(1, query.getResultList().size());
        assertEquals(9, query.getResultList().size());

        em.close();
    }
}

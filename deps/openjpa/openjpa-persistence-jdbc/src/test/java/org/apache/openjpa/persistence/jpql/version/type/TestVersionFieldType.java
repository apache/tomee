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
package org.apache.openjpa.persistence.jpql.version.type;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Verifies that the version field is of the proper java type 
 * when returned from a query. See OPENJPA-2435.
 */
public class TestVersionFieldType extends SingleEMFTestCase {

    public void setUp() {
        setUp(CLEAR_TABLES, LongVersionEntity.class, 
            ShortVersionEntity.class, PrimativeLongVersionEntity.class,
            PrimativeShortVersionEntity.class, TimestampVersionEntity.class, 
            BaseEntity.class, ChildVersionEntity.class);
        createTestData();
    }

    public void testProjectionVersionReturnType() {
        verifyType(LongVersionEntity.class, Long.class);
        verifyType(ShortVersionEntity.class, Short.class);
        verifyType(PrimativeShortVersionEntity.class, Short.class);
        verifyType(PrimativeLongVersionEntity.class, Long.class);
        verifyType(ChildVersionEntity.class, Long.class);
        verifyType(TimestampVersionEntity.class, Timestamp.class);
    }

    public void verifyType(Class<?> cls, Class<?> expectedClsType) {

        EntityManager em = emf.createEntityManager();
        String str = "SELECT o.id, o.version FROM " + cls.getName() + " o";
        Query query = em.createQuery(str);
        List<Object[]> objectList = query.getResultList();

        for (Object[] objects : objectList) {
            assertNotNull("Version should not be null.", objects[1]);
            assertTrue("Type should be " + expectedClsType.getName() + 
                ".  But it is " + objects[1].getClass(),
                objects[1].getClass() == expectedClsType);
        }
        
        em.close();
    }

    public void createTestData() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        LongVersionEntity lve = new LongVersionEntity();
        lve.setId(9);
        em.persist(lve);

        ShortVersionEntity sve = new ShortVersionEntity();
        sve.setId(9);
        em.persist(sve);

        PrimativeShortVersionEntity psve = new PrimativeShortVersionEntity();
        psve.setId(9);
        em.persist(psve);

        PrimativeLongVersionEntity plve = new PrimativeLongVersionEntity();
        plve.setId(9);
        em.persist(plve);

        TimestampVersionEntity tve = new TimestampVersionEntity();
        tve.setId(9);
        em.persist(tve);

        ChildVersionEntity ave = new ChildVersionEntity();
        ave.setId(9);
        em.persist(ave);

        em.getTransaction().commit();
        em.close();
    }
}

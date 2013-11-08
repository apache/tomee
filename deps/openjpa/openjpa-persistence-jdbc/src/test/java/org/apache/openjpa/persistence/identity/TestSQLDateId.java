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
package org.apache.openjpa.persistence.identity;

import java.sql.Date;
import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that entities can use SQL dates as identity values.
 *
 * @author Abe White
 */
public class TestSQLDateId
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(SQLDateIdEntity.class);
    }

    public void testPersist() {
        long time = ((long) (System.currentTimeMillis() / 1000)) * 1000;

        SQLDateIdEntity e = new SQLDateIdEntity();
        e.setId(new Date(time));
        e.setData(1);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertEquals(time, e.getId().getTime());
        em.close();

        em = emf.createEntityManager();
        e = em.find(SQLDateIdEntity.class, new Date(time));
        assertEquals(1, e.getData());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestSQLBigIntegerId.class);
    }
}


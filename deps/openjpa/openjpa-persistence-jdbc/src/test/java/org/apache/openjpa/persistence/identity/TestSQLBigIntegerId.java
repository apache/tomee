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

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * @author <a href="mailto:mnachev@gmail.com">Miroslav Nachev</a>
 */
public class TestSQLBigIntegerId
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(SQLBigIntegerIdEntity.class, CLEAR_TABLES);
    }

    public void testPersist() {
        long time = ((long) (System.currentTimeMillis() / 1000)) * 1000;
        BigInteger integer = new BigDecimal(time).toBigInteger();

        SQLBigIntegerIdEntity e = new SQLBigIntegerIdEntity();
        e.setId(integer);
        e.setData(1);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertEquals(time, e.getId().longValue());
        em.close();

        em = emf.createEntityManager();
        e = em.find(SQLBigIntegerIdEntity.class, integer);
        assertEquals(1, e.getData());
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestSQLBigIntegerId.class);
    }
}

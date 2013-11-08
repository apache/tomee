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

import javax.persistence.EntityManager;

import junit.textui.TestRunner;

import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * @author <a href="mailto:mnachev@gmail.com">Miroslav Nachev</a>
 */
public class TestSQLBigDecimalId
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(SQLBigDecimalIdEntity.class, DROP_TABLES);
    }

    public void testPersist() {
        long time = ((long) (System.currentTimeMillis() / 1000)) * 1000;
        BigDecimal decimal = new BigDecimal(time);

        SQLBigDecimalIdEntity e = new SQLBigDecimalIdEntity();
        e.setId(decimal);
        e.setData(1);
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();
        assertEquals(time, e.getId().longValue());
        em.close();

        em = emf.createEntityManager();
        e = em.find(SQLBigDecimalIdEntity.class, decimal);
        assertEquals(1, e.getData());
        em.close();
        
    }
    
    @AllowFailure
    // This test currently allows failure because DB2 and Derby don't handle BigDecimals properly quite yet. 
    public void testQuery() {
        int data = 156;
        BigDecimal decimal = new BigDecimal(1234);
        SQLBigDecimalIdEntity e = new SQLBigDecimalIdEntity();
        e.setId(decimal);
        e.setData(data);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(e);
        em.getTransaction().commit();

        SQLBigDecimalIdEntity e2 =
            (SQLBigDecimalIdEntity) em.createQuery("SELECT a FROM SQLBigDecimalIdEntity a WHERE a.data=" + data)
                .getSingleResult();
        
        assertEquals(e, e2);
        em.close();

    }

    public static void main(String[] args) {
        TestRunner.run(TestSQLBigDecimalId.class);
    }
}

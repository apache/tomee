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
package org.apache.openjpa.persistence.xs;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestMergeComplexKey extends SingleEMFTestCase {
    Object[] props = new Object[] { AccountingHierarchy.class, AccountingHierarchyRate.class, CLEAR_TABLES };

    @Override
    public void setUp() throws Exception {
        setUp(props);
    }

    public void test() throws Exception {
        createDate();
        EntityManager em = emf.createEntityManager();
        AccountingHierarchy accountingHierarchy = (AccountingHierarchy) em.find(AccountingHierarchy.class, "TESTING");
        accountingHierarchy.setShortDesc("NAME:" + System.currentTimeMillis());
        accountingHierarchy = roundtrip(accountingHierarchy);
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            em.merge(accountingHierarchy);
        } catch (RuntimeException e) {
            em.getTransaction().setRollbackOnly();
            throw e;
        } finally {
            if (em.getTransaction().getRollbackOnly()) {
                em.getTransaction().rollback();
            } else {
                em.getTransaction().commit();
            }
        }

    }

    void createDate() {
        EntityManager em = emf.createEntityManager();
        System.out.println(em.createQuery("select o from AccountingHierarchy o").getResultList().size());

        String code = "TESTING";
        AccountingHierarchy accountingHierarchy = em.find(AccountingHierarchy.class, code);
        if (accountingHierarchy == null) {
            accountingHierarchy = new AccountingHierarchy();
            accountingHierarchy.setCode(code);
            accountingHierarchy.setShortDesc("TESTING");

            AccountingHierarchyRate accountingHierarchyRate =
                new AccountingHierarchyRate("1", accountingHierarchy, BigDecimal.ONE, BigDecimal.TEN);

            accountingHierarchy.getAccRateList().add(accountingHierarchyRate);

            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                em.persist(accountingHierarchy);
            } catch (RuntimeException e) {
                tx.setRollbackOnly();
                throw e;
            } finally {
                if (tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }
            }
        }

    }
}

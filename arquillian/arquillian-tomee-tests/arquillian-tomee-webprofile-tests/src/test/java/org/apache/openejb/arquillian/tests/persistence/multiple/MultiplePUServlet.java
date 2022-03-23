/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.arquillian.tests.persistence.multiple;

import org.apache.openejb.arquillian.tests.Runner;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class MultiplePUServlet extends HttpServlet {
    @Resource
    private UserTransaction transaction;

    @Resource
    private TransactionManager txMgr;

    @PersistenceContext(name = "pu1")
    private EntityManager entityManager1;

    @PersistenceContext(name = "pu2")
    private EntityManager entityManager2;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Runner.run(req, resp, this);
    }

    public void testEntityManager1() throws Exception {
        testEm(entityManager1, new Person1("p1"));
    }

    public void testEntityManager2() throws Exception {
        testEm(entityManager2, new Person2("p2"));
    }

    private void testEm(final EntityManager em, final Object person) throws Exception {
        assertNotNull(em);
        // start debug
        System.out.flush();
        System.out.println("[DEBUG] Tx status = " + transaction.getStatus() + ", " + txMgr.getTransaction());
        System.out.flush();
        // end debug

        final boolean start = txMgr.getTransaction() == null; // should be false
        if (start) {
            transaction.begin();
        }
        em.persist(person);
        if (start) {
            transaction.commit();
        }
    }
}
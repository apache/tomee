/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.servlet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.Query;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JpaServlet extends HttpServlet {

    @PersistenceUnit(name = "jpa-example")
    private EntityManagerFactory emf;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();

        out.println("@PersistenceUnit=" + emf);

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        JpaBean jpaBean = new JpaBean();
        jpaBean.setName("JpaBean");
        em.persist(jpaBean);

        transaction.commit();
        transaction.begin();

        Query query = em.createQuery("SELECT j FROM JpaBean j WHERE j.name='JpaBean'");
        jpaBean = (JpaBean) query.getSingleResult();
        out.println("Loaded " + jpaBean);

        em.remove(jpaBean);

        transaction.commit();
        transaction.begin();

        query = em.createQuery("SELECT count(j) FROM JpaBean j WHERE j.name='JpaBean'");
        int count = ((Number) query.getSingleResult()).intValue();
        if (count == 0) {
            out.println("Removed " + jpaBean);
        } else {
            out.println("ERROR: unable to remove" + jpaBean);
        }

        transaction.commit();
    }
}

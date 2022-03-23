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

package org.apache.openejb.arquillian.tests.listenerpersistence;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.transaction.UserTransaction;


public class PersistenceServletContextListener implements ServletContextListener {

    @Resource
    private UserTransaction transaction;

    @PersistenceUnit
    private EntityManagerFactory entityMgrFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public void contextInitialized(ServletContextEvent event) {
        final ServletContext context = event.getServletContext();

        if (transaction != null) {
            try {
                transaction.begin();
                transaction.commit();
                context.setAttribute(ContextAttributeName.KEY_Transaction.name(), "Transaction injection successful");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (entityManager != null) {
            Address a = new Address();
            try {
                entityManager.contains(a);
                context.setAttribute(ContextAttributeName.KEY_EntityManager.name(), "Transaction manager injection successful");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (entityMgrFactory != null) {
            Address a = new Address();
            try {
                EntityManager em = entityMgrFactory.createEntityManager();
                em.contains(a);
                context.setAttribute(ContextAttributeName.KEY_EntityManagerFactory.name(), "Transaction manager factory injection successful");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    public void contextDestroyed(ServletContextEvent event) {
    }

}
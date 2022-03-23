/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.arquillian.tests.persistence.ejb;

import org.apache.openejb.arquillian.tests.Runner;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import java.io.IOException;


public class SessionSynchronizationCallbackServlet extends HttpServlet {

    @Resource
    UserTransaction ut;

    @PersistenceContext
    EntityManager em;

    @EJB
    StatefulBean statefulBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Runner.run(req, resp, this);
    }

    public void testStatefulBeanSessionSynchronizationCallback() throws ServletException {
        cleanUpInOwnTransaction();
        statefulBean.doPersist();
        verifyExecutionResult();
    }

    private void cleanUpInOwnTransaction() {
        try {
            ut.begin();
            deleteIfExists(TestEntity.class, StatefulBean.ENTITY_ID_AFTER_BEGIN);
            deleteIfExists(TestEntity.class, StatefulBean.ENTITY_ID_BEFORE_COMPLETION);
            deleteIfExists(TestEntity.class, StatefulBean.ENTITY_ID_BUSINESS_METHOD);
            ut.commit();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to clean up before the test: " + ex.getMessage(), ex);
        }
    }

    private void deleteIfExists(Class entityClass, int id) {
        final Object entity = em.find(entityClass, Integer.valueOf(id));
        if (null != entity) {
            em.remove(entity);
        }
    }

    private void verifyExecutionResult() {
        verifyEntityExists(TestEntity.class, StatefulBean.ENTITY_ID_AFTER_BEGIN);
        verifyEntityExists(TestEntity.class, StatefulBean.ENTITY_ID_BEFORE_COMPLETION);
        verifyEntityExists(TestEntity.class, StatefulBean.ENTITY_ID_BUSINESS_METHOD);
    }

    private void verifyEntityExists(Class entityClass, int id) {
        final Object entity = em.find(entityClass, Integer.valueOf(id));
        if (null == entity) {
            throw new IllegalStateException("Expecting entity of type=" + entityClass
                    + " with id=" + id + " instead of NULL"
            );
        }
    }
}

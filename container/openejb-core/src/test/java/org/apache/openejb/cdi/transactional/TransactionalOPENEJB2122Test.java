/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi.transactional;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

@SimpleLog
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@PersistenceUnitDefinition
public class TransactionalOPENEJB2122Test {
    @Inject
    private MyTestRessource bean;

    @Test
    public void run() {
        assertEquals("execution finished", bean.simpleTestMethod());
    }

    @Stateless
    public static class MyTestRessource {
        @Inject
        private MyTestBean myTestBean;

        @PersistenceContext
        private EntityManager em;

        @Transactional(Transactional.TxType.NOT_SUPPORTED)
        public String simpleTestMethod() {
            myTestBean.persistAnEntity();
            assertEquals(0, em.createQuery("select count(e) from TransactionalOPENEJB2122Test$MyEntity e", Number.class).getSingleResult().intValue());
            myTestBean.doOnFail();
            assertEquals(1, em.createQuery("select count(e) from TransactionalOPENEJB2122Test$MyEntity e", Number.class).getSingleResult().intValue());
            return "execution finished";
        }
    }

    @Stateless
    public static class MyTestBean {
        @PersistenceContext
        private EntityManager em;

        @Resource
        private SessionContext sessionContext;

        @Transactional(Transactional.TxType.REQUIRED)
        public void persistAnEntity() {
            final MyEntity e = new MyEntity();
            em.persist(e);
            sessionContext.setRollbackOnly();
        }

        @Transactional(Transactional.TxType.REQUIRES_NEW)
        public void doOnFail() {
            final MyEntity e = new MyEntity();
            em.persist(e);
        }
    }

    @Entity
    public static class MyEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }
    }
}

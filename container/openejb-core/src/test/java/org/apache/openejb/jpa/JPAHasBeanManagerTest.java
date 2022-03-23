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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jpa;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.transaction.Transactional;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class JPAHasBeanManagerTest {
    @Inject
    private Dao dao;

    @Test
    public void run() {
        dao.doAsserts();
    }

    @Transactional
    @ApplicationScoped
    @PersistenceUnitDefinition(
            entitiesPackage = "org.apache.openejb.jpa",
            properties = {"openjpa.RuntimeUnenhancedClasses=supported", "tomee.jpa.factory.lazy=true"},
            provider = "org.apache.openejb.jpa.JPAHasBeanManagerTest$TheTestProvider")
    public static class Dao {
        @PersistenceContext
        private EntityManager em;
        private TheTestEntity persisted;

        public void start(@Observes @Initialized(ApplicationScoped.class) final Object boot) {
            TheTestEntity entity = new TheTestEntity();
            em.persist(entity); // ensure it works
            persisted = entity;
        }

        public void doAsserts() {
            assertNotNull(TheTestProvider.MAP);
            final Object bm = TheTestProvider.MAP.get("jakarta.persistence.bean.manager");
            assertNotNull(bm);
            assertTrue(BeanManager.class.isInstance(bm));
            assertNotNull(em.find(TheTestEntity.class, persisted.getId()));
        }
    }

    @Startup
    @Singleton
    public static class EJBDao {
        @PersistenceContext
        private EntityManager em;

        @PostConstruct
        public void start() {
            TheTestEntity entity = new TheTestEntity();
            em.persist(entity); // ensure it works
        }
    }

    public static class TheTestProvider extends PersistenceProviderImpl {
        private static Map MAP;

        @Override
        public OpenJPAEntityManagerFactory createContainerEntityManagerFactory(final PersistenceUnitInfo pui, final Map m) {
            MAP = m;
            // only works cause of lazy property
            final BeanManager beanManager = BeanManager.class.cast(m.get("jakarta.persistence.bean.manager"));
            assertNotNull(beanManager.getReference(beanManager.resolve(beanManager.getBeans(Dao.class)), Dao.class, null));
            // just delegate to openjpa since we don't aim at reimplementing JPA in a test ;)
            return super.createContainerEntityManagerFactory(pui, m);
        }
    }

    @Entity
    public static class TheTestEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }
    }
}

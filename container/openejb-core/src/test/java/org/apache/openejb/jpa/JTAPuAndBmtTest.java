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

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class JTAPuAndBmtTest {
    @EJB
    private BmtManager bmtManager;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("JTAPuAndBmtTest", "new://Resource?type=DataSource");
        p.put("JTAPuAndBmtTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("JTAPuAndBmtTest.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module
    public StatelessBean app() throws Exception {
        final StatelessBean bean = new StatelessBean(BmtManager.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(TheEntity.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @LocalBean
    @Stateless
    @TransactionManagement(TransactionManagementType.BEAN)
    public static class BmtManager {
        @PersistenceContext
        private EntityManager em;

        @Resource
        private EJBContext ctx;

        public TheEntity persist() {
            try {
                ctx.getUserTransaction().begin();
                final TheEntity entity = new TheEntity();
                entity.setName("name");
                em.persist(entity);
                ctx.getUserTransaction().commit();
                return entity;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        public TheEntity findWithJpQl() {
            final TypedQuery<TheEntity> query = em.createQuery("select e from JTAPuAndBmtTest$TheEntity e", TheEntity.class);
            query.getResultList(); // to ensure we don't break OPENEJB-1443
            return query.getResultList().iterator().next();
        }

        public void update(final TheEntity entity) {
            entity.setName("new");
            try {
                ctx.getUserTransaction().begin();
                em.merge(entity);
                ctx.getUserTransaction().commit();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Entity
    public static class TheEntity {
        @Id
        @GeneratedValue
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(final long i) {
            id = i;
        }

        public String getName() {
            return name;
        }

        public void setName(final String n) {
            name = n;
        }
    }

    @Test
    public void valid() {
        assertNotNull(bmtManager.persist());

        final TheEntity entity = bmtManager.findWithJpQl();
        assertNotNull(entity);

        bmtManager.update(entity); // will throw an exception if any error
    }
}

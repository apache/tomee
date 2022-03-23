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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.stateful;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import java.util.Properties;

/**
 * http://openejb.979440.n4.nabble.com/openEJB-fail-on-second-test-tp4401889p4406177.html
 */
@RunWith(ApplicationComposer.class)
public class StatefulJPATest {
    @Test
    public void testRemoveOk() throws NamingException {
        final Context ctx = (Context) System.getProperties().get(ApplicationComposers.OPENEJB_APPLICATION_COMPOSER_CONTEXT);
        final AStateful stateful = (AStateful) ctx.lookup("global/StatefulJPATest/app/AStateful");
        for (int i = 0; i < 3; i++) {
            stateful.saveSomething();
        }
        stateful.remove();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.setProperty("StatefulJPATest", "new://Resource?type=DataSource");
        p.setProperty("StatefulJPATest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.setProperty("StatefulJPATest.JdbcUrl", "jdbc:hsqldb:mem:stateful-jpa");
        return p;
    }

    @Module
    public StatefulBean app() throws Exception {
        final StatefulBean bean = new StatefulBean(AStateful.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(AnEntity.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @LocalBean
    @Stateful
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public static class AStateful {
        @PersistenceContext
        private EntityManager em;

        public void saveSomething() {
            final AnEntity entity = new AnEntity();
            entity.setName("name");
            em.persist(entity);
        }

        @Remove
        public void remove() {
            saveSomething();
        }
    }

    @Entity
    public static class AnEntity {
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
}

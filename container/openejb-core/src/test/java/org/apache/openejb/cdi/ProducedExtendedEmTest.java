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
package org.apache.openejb.cdi;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJBException;
import jakarta.ejb.Stateful;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import java.io.Serializable;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class ProducedExtendedEmTest {
    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{EntityManagerProducer.class, A.class};
    }

    @Module
    public Persistence persistence() throws Exception {
        final PersistenceUnit unit = new PersistenceUnit("cdi-em-extended");
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.getProperties().setProperty("openjpa.DatCache", "false");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new org.apache.openejb.jee.jpa.unit.Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("ProducedExtendedEmTest", "new://Resource?type=DataSource");
        p.put("ProducedExtendedEmTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("ProducedExtendedEmTest.JdbcUrl", "jdbc:hsqldb:mem:produce-em-cdi");
        return p;
    }

    @SessionScoped
    @Stateful
    public static class EntityManagerProducer implements Serializable {
        @PersistenceContext(type = PersistenceContextType.EXTENDED)
        private EntityManager em;

        @Produces
        public EntityManager produceEm() {
            return em;
        }
    }

    @SessionScoped
    @Stateful
    public static class A implements Serializable {
        @Inject
        private EntityManager em;

        public String getDelegateClassName() {
            return em.getDelegate().getClass().getCanonicalName();
        }
    }

    @Inject
    private A a;

    @Test
    public void checkEm() {
        try {
            a.getDelegateClassName();
        } catch (final EJBException ee) {
            // an entity manager should already be registered for this extended persistence unit
            // valid since we dont call it in the context of the stateful
            // note: we could enhance it later if needed
            assertNotNull(ee);
            assertThat(ee.getCause(), instanceOf(IllegalStateException.class));
        }
    }
}

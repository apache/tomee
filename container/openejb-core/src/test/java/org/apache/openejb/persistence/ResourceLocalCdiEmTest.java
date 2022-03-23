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
package org.apache.openejb.persistence;

import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.webbeans.logger.JULLoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class ResourceLocalCdiEmTest {
    @Inject
    private PersistManager persistManager;

    @Test
    public void injection() {
        assertNotNull(persistManager);
        assertTrue(!persistManager.isEmNull());
    }

    @Configuration
    public Properties config() {
        //avoid linkage error on mac, only used for tests so don't need to add it in Core
        JULLoggerFactory.class.getName();

        final Properties p = new Properties();
        p.put("ResourceLocalCdiEmTest", "new://Resource?type=DataSource");
        p.put("ResourceLocalCdiEmTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("ResourceLocalCdiEmTest.JdbcUrl", "jdbc:hsqldb:mem:ResourceLocalCdiEmTest");
        p.put("ResourceLocalCdiEmTest.JtaManaged", "false");
        return p;
    }

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{EMFProducer.class, PersistManager.class};
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("rl-unit");
        unit.setTransactionType(TransactionType.RESOURCE_LOCAL);
        unit.setNonJtaDataSource("ResourceLocalCdiEmTest");
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    public static class PersistManager {
        @Inject
        private EntityManager em;

        public boolean isEmNull() {
            return em == null;
        }
    }

    public static class EMFProducer {
        @Produces
        @jakarta.persistence.PersistenceUnit
        private EntityManagerFactory emf;

        @Produces
        private EntityManager em() {
            return emf.createEntityManager();
        }

        private void close(@Disposes final EntityManager em) {
            em.close();
        }
    }
}

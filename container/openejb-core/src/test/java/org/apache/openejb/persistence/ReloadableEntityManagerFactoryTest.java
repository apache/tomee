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

package org.apache.openejb.persistence;

import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

@RunWith(ApplicationComposer.class)
public class ReloadableEntityManagerFactoryTest {
    @jakarta.persistence.PersistenceUnit
    private EntityManagerFactory emf;

    @Module
    public Persistence persistence() throws Exception {
        final PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(MyEntity.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.getProperties().setProperty("openjpa.DataCache", "false");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new org.apache.openejb.jee.jpa.unit.Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("ReloadableEntityManagerFactoryTest", "new://Resource?type=DataSource");
        p.put("ReloadableEntityManagerFactoryTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("ReloadableEntityManagerFactoryTest.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Test
    public void reload() {
        final ReloadableEntityManagerFactory remft = (ReloadableEntityManagerFactory) emf;
        final EntityManagerFactory originalEmf = remft.getDelegate();

        /*
         * XXX Remove / update this call if OPENJPA-2844 is resolved
         * Workaround: Initialize the underlying Broker by calling createEntityManager() first
         * before calling getProperties()
         */
        remft.createEntityManager();

        assertEquals("false", emf.getProperties().get("openjpa.DataCache"));
        select();

        remft.setProperty("openjpa.DataCache", "true(Types=" + MyEntity.class.getName() + ")");
        remft.reload();
        select();
        assertEquals("true(Types=" + MyEntity.class.getName() + ")", emf.getProperties().get("openjpa.DataCache"));

        final EntityManagerFactory reloadedEmf = remft.getDelegate();
        assertNotSame(originalEmf, reloadedEmf);
    }

    private void select() {
        emf.createEntityManager()
            .createQuery("select m from ReloadableEntityManagerFactoryTest$MyEntity m")
            .getResultList();
    }

    @Entity
    public static class MyEntity {
        @Id
        @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }

        public void setId(final long id) {
            this.id = id;
        }
    }
}

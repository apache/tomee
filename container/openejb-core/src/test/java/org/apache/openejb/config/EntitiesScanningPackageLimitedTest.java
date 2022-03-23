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
package org.apache.openejb.config;

import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Stateless;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceUnit;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class EntitiesScanningPackageLimitedTest {

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[]{SomeSimpleEJb.class, SomeSimpleEntity.class};
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("cczczczedc2", "new://Resource?type=DataSource");
        p.put("cczczczedc2.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("cczczczedc2.JdbcUrl", "jdbc:hsqldb:mem:cczccdzcz2");
        return p;
    }

    @Module
    public Persistence persistence() {
        final org.apache.openejb.jee.jpa.unit.PersistenceUnit unit = new org.apache.openejb.jee.jpa.unit.PersistenceUnit("jpa-global-dsdef-unit");
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.getProperties().setProperty("openejb.jpa.auto-scan", "true");
        unit.getProperties().setProperty("openejb.jpa.auto-scan.package", "com.doesnt.exist");

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @Stateless
    public static class SomeSimpleEJb {
    }

    @Entity
    public static class SomeSimpleEntity {
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

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Test
    public void check() throws Exception {
        final ReloadableEntityManagerFactory remf = ((ReloadableEntityManagerFactory) emf);
        assertEquals(0, remf.getManagedClasses().size());
    }
}

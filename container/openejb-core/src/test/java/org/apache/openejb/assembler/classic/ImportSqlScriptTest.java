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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class ImportSqlScriptTest {
    @EJB
    private Persister persister;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("ImportSqlScriptTestDb", "new://Resource?type=DataSource");
        p.put("ImportSqlScriptTestDb.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("ImportSqlScriptTestDb.JdbcUrl", "jdbc:hsqldb:mem:import-sql");
        //p.put("ImportSqlScriptTest.initConnectionSqls", "insert into \"ImportSqlScriptTest$Something\" (id) values(1);");
        return p;
    }

    @Module
    public SingletonBean app() throws Exception {
        final SingletonBean bean = new SingletonBean(Persister.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("ImportSqlScriptTest");
        unit.addClass(Something.class);
        unit.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.setProperty("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @LocalBean
    @Stateless
    public static class Persister {
        @PersistenceContext
        private EntityManager em;

        public int count() {
            return ((Number) em.createQuery("select count(s) from ImportSqlScriptTest$Something s").getSingleResult()).intValue();
        }
    }

    @Entity
    public static class Something {
        @Id
        private long id;
    }

    @Test
    public void checkImportData() {
        assertEquals(3, persister.count());
    }
}

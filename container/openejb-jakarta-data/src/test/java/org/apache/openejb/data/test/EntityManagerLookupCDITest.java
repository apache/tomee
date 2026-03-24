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
package org.apache.openejb.data.test;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import org.apache.openejb.data.test.entity.Person;
import org.apache.openejb.data.test.repo.PersonRepository;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that EntityManagerLookup correctly resolves persistence units
 * and that CDI-to-JNDI fallback works when no CDI-produced EntityManagerFactory exists.
 */
@RunWith(ApplicationComposer.class)
public class EntityManagerLookupCDITest {

    @Inject
    private PersonRepository personRepository;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {PersonRepository.class})
    public EjbJar beans() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        final PersistenceUnit unit = new PersistenceUnit("person-unit");
        unit.setJtaDataSource("personDatabase");
        unit.setNonJtaDataSource("personDatabaseUnmanaged");
        unit.getClazz().add(Person.class.getName());
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        return unit;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("personDatabase", "new://Resource?type=DataSource");
        p.put("personDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-cdi-lookup");
        return p;
    }

    @Test
    public void testLookupFallsBackToJNDIWhenNoCDIBean() throws Exception {
        // In this test, no CDI-produced EntityManagerFactory exists,
        // so EntityManagerLookup should fall back to JNDI and still work
        assertNotNull("Repository should be injected", personRepository);

        utx.begin();
        try {
            final Person person = new Person("CDILookupTest", 42, "cdi@test.com");
            final Person saved = personRepository.insert(person);
            assertNotNull("Saved person should have an id", saved.getId());

            final Optional<Person> found = personRepository.findById(saved.getId());
            assertTrue("Should find the saved person", found.isPresent());
            assertEquals("CDILookupTest", found.get().getName());
        } finally {
            utx.rollback();
        }
    }
}

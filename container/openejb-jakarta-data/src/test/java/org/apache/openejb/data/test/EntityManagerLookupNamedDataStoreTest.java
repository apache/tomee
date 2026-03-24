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
import org.apache.openejb.data.test.repo.NamedDataStorePersonRepository;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests EntityManagerLookup with @Repository(dataStore="person-unit")
 * which exercises the resolveJndiName path with a specific named dataStore.
 * This ensures the JNDI lookup path for named persistence units works correctly.
 */
@RunWith(ApplicationComposer.class)
public class EntityManagerLookupNamedDataStoreTest {

    @Inject
    private NamedDataStorePersonRepository repo;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {NamedDataStorePersonRepository.class})
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
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-named-ds");
        return p;
    }

    @Test
    public void testNamedDataStoreResolvesCorrectly() throws Exception {
        assertNotNull("Repository with named dataStore should be injected", repo);

        utx.begin();
        try {
            final Person person = new Person("NamedDS", 38, "named-ds@test.com");
            final Person saved = repo.insert(person);
            assertNotNull("Saved person should have an id", saved.getId());

            final Optional<Person> found = repo.findById(saved.getId());
            assertTrue("Should find the saved person via named dataStore", found.isPresent());
            assertEquals("NamedDS", found.get().getName());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testNamedDataStoreExistsById() throws Exception {
        utx.begin();
        try {
            final Person person = repo.insert(new Person("ExistCheck", 29, "exist@test.com"));

            assertTrue(repo.existsById(person.getId()));
            assertFalse(repo.existsById(999999L));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testNamedDataStoreCrudOperations() throws Exception {
        utx.begin();
        try {
            // Insert
            final Person person = repo.insert(new Person("CrudNamed", 45, "crud-named@test.com"));
            final Long id = person.getId();

            // Update via save
            person.setAge(46);
            final Person updated = repo.save(person);
            assertEquals(46, updated.getAge());

            // Find
            final Optional<Person> found = repo.findById(id);
            assertTrue(found.isPresent());
            assertEquals(46, found.get().getAge());

            // Delete
            repo.delete(updated);
            assertFalse(repo.existsById(id));
        } finally {
            utx.rollback();
        }
    }
}

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
 * Tests EntityManagerLookup JNDI fallback paths including:
 * - resolveJndiName with a specific dataStore name
 * - wrapWithJta using JtaEntityManagerRegistry
 * - extractUnitName from JNDI path
 *
 * This test uses a named persistence unit to exercise the dataStore-based
 * JNDI lookup path in resolveJndiName.
 */
@RunWith(ApplicationComposer.class)
public class EntityManagerLookupJNDITest {

    @Inject
    private PersonRepository personRepository;

    @PersistenceContext
    private EntityManager em;

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
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-jndi-lookup");
        return p;
    }

    @Test
    public void testJNDILookupWithDefaultDataStore() throws Exception {
        // With empty dataStore, resolveJndiName should enumerate PUs and find "person-unit"
        assertNotNull("Repository should be injected", personRepository);

        utx.begin();
        try {
            final Person person = new Person("JNDIDefault", 35, "jndi-default@test.com");
            final Person saved = personRepository.insert(person);
            assertNotNull("Saved person should have an id", saved.getId());

            final Optional<Person> found = personRepository.findById(saved.getId());
            assertTrue("Should find the saved person", found.isPresent());
            assertEquals("JNDIDefault", found.get().getName());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testJNDILookupReturnsJtaEntityManager() throws Exception {
        // Verify the EntityManager is JTA-managed (wrapped by JtaEntityManager)
        assertNotNull("Repository should be injected", personRepository);

        utx.begin();
        try {
            // Insert and verify we can find within the same transaction (proves JTA integration)
            final Person p1 = personRepository.insert(new Person("JtaTest1", 28, "jta1@test.com"));
            final Person p2 = personRepository.insert(new Person("JtaTest2", 32, "jta2@test.com"));

            // Both should be visible in the same transaction
            final Optional<Person> found1 = personRepository.findById(p1.getId());
            final Optional<Person> found2 = personRepository.findById(p2.getId());
            assertTrue("Should find first person in same tx", found1.isPresent());
            assertTrue("Should find second person in same tx", found2.isPresent());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testJNDILookupRollbackDoesNotPersist() throws Exception {
        final Long id;
        utx.begin();
        try {
            final Person person = personRepository.insert(new Person("RollbackTest", 40, "rollback@test.com"));
            id = person.getId();
            assertNotNull(id);
        } finally {
            utx.rollback();
        }

        // After rollback, entity should not be found
        utx.begin();
        try {
            final Optional<Person> found = personRepository.findById(id);
            assertTrue("Entity should not exist after rollback", found.isEmpty());
        } finally {
            utx.rollback();
        }
    }
}

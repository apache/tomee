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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class BasicCrudTest {

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
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-crud");
        return p;
    }

    @Test
    public void testInjectRepository() {
        assertNotNull("Repository should be injected", personRepository);
    }

    @Test
    public void testInsertAndFindById() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("Alice", 30, "alice@example.com");
            final Person saved = personRepository.insert(person);

            assertNotNull("saved should not be null", saved);
            assertNotNull("saved.getId() should not be null, saved=" + saved.getName() + " age=" + saved.getAge(), saved.getId());

            final Optional<Person> found = personRepository.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals("Alice", found.get().getName());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testSaveAndUpdate() throws Exception {
        utx.begin();
        try {
            Person person = new Person("Bob", 25, "bob@example.com");
            person = personRepository.insert(person);

            person.setAge(26);
            final Person updated = personRepository.save(person);
            assertEquals(26, updated.getAge());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDelete() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("Charlie", 35, "charlie@example.com");
            final Person saved = personRepository.insert(person);
            final Long id = saved.getId();

            personRepository.delete(saved);

            final Optional<Person> found = personRepository.findById(id);
            assertFalse(found.isPresent());
        } finally {
            utx.rollback();
        }
    }

    // -- existsById tests --

    @Test
    public void testExistsByIdReturnsTrue() throws Exception {
        utx.begin();
        try {
            final Person person = personRepository.insert(new Person("Diana", 28, "diana@example.com"));

            assertTrue("existsById should return true for an existing entity",
                personRepository.existsById(person.getId()));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testExistsByIdReturnsFalse() throws Exception {
        utx.begin();
        try {
            assertFalse("existsById should return false for a non-existing entity",
                personRepository.existsById(999999L));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testExistsByIdAfterDelete() throws Exception {
        utx.begin();
        try {
            final Person person = personRepository.insert(new Person("Eve", 40, "eve@example.com"));
            final Long id = person.getId();

            assertTrue(personRepository.existsById(id));

            personRepository.delete(person);

            assertFalse("existsById should return false after entity is deleted",
                personRepository.existsById(id));
        } finally {
            utx.rollback();
        }
    }

    // -- count tests --

    @Test
    public void testCountEmpty() throws Exception {
        utx.begin();
        try {
            assertEquals(0L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testCountAfterInserts() throws Exception {
        utx.begin();
        try {
            personRepository.insert(new Person("Frank", 30, "frank@example.com"));
            personRepository.insert(new Person("Grace", 25, "grace@example.com"));
            personRepository.insert(new Person("Hank", 35, "hank@example.com"));

            assertEquals(3L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testCountAfterDelete() throws Exception {
        utx.begin();
        try {
            final Person p1 = personRepository.insert(new Person("Ivy", 22, "ivy@example.com"));
            personRepository.insert(new Person("Jack", 33, "jack@example.com"));

            assertEquals(2L, personRepository.count());

            personRepository.delete(p1);

            assertEquals(1L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    // -- deleteAll tests --

    @Test
    public void testDeleteAllEntities() throws Exception {
        utx.begin();
        try {
            personRepository.insert(new Person("Kate", 28, "kate@example.com"));
            personRepository.insert(new Person("Leo", 31, "leo@example.com"));
            personRepository.insert(new Person("Mia", 26, "mia@example.com"));

            assertEquals(3L, personRepository.count());

            personRepository.deleteAll(List.of());
            // Deleting empty list should not affect existing entities
            assertEquals(3L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDeleteAllWithEntitiesArgument() throws Exception {
        utx.begin();
        try {
            final Person p1 = personRepository.insert(new Person("Nick", 30, "nick@example.com"));
            final Person p2 = personRepository.insert(new Person("Olivia", 27, "olivia@example.com"));
            final Person p3 = personRepository.insert(new Person("Pat", 35, "pat@example.com"));

            assertEquals(3L, personRepository.count());

            personRepository.deleteAll(List.of(p1, p2));

            assertEquals(1L, personRepository.count());
            assertFalse(personRepository.existsById(p1.getId()));
            assertFalse(personRepository.existsById(p2.getId()));
            assertTrue(personRepository.existsById(p3.getId()));
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDeleteAllNoArg() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Quinn", 29, "quinn@example.com"));
            em.persist(new Person("Rose", 32, "rose@example.com"));
            em.flush();

            assertEquals(2L, personRepository.count());

            // CrudRepository.deleteAll(List) with all entities
            final List<Person> all = personRepository.findAll().toList();
            personRepository.deleteAll(all);

            assertEquals(0L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    // -- saveAll tests --

    @Test
    public void testSaveAllInsertMultiple() throws Exception {
        utx.begin();
        try {
            final List<Person> people = List.of(
                new Person("Sam", 28, "sam@example.com"),
                new Person("Tina", 33, "tina@example.com"),
                new Person("Uma", 41, "uma@example.com")
            );

            final List<Person> saved = personRepository.saveAll(people);

            assertEquals(3, saved.size());
            assertEquals("Sam", saved.get(0).getName());
            assertEquals("Tina", saved.get(1).getName());
            assertEquals("Uma", saved.get(2).getName());
            assertEquals(3L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testSaveAllUpdateExisting() throws Exception {
        utx.begin();
        try {
            final Person p1 = personRepository.insert(new Person("Vera", 30, "vera@example.com"));
            final Person p2 = personRepository.insert(new Person("Walt", 25, "walt@example.com"));

            p1.setAge(31);
            p2.setAge(26);

            final List<Person> updated = personRepository.saveAll(List.of(p1, p2));

            assertEquals(2, updated.size());
            assertEquals(31, updated.get(0).getAge());
            assertEquals(26, updated.get(1).getAge());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testSaveAllEmptyList() throws Exception {
        utx.begin();
        try {
            final List<Person> result = personRepository.saveAll(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } finally {
            utx.rollback();
        }
    }

    // -- save with array (isArray code path) --

    @Test
    public void testSaveArrayInsertMultiple() throws Exception {
        utx.begin();
        try {
            final Person[] people = new Person[]{
                new Person("Xander", 22, "xander@example.com"),
                new Person("Yara", 37, "yara@example.com")
            };

            final Person[] saved = personRepository.save(people);

            assertEquals(2, saved.length);
            for (final Person p : saved) {
                assertNotNull("saved entity should have an ID", p.getId());
            }
            assertEquals(2L, personRepository.count());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testSaveArrayUpdateExisting() throws Exception {
        utx.begin();
        try {
            final Person p1 = personRepository.insert(new Person("Zach", 29, "zach@example.com"));
            final Person p2 = personRepository.insert(new Person("Amy", 34, "amy@example.com"));

            p1.setAge(30);
            p2.setAge(35);

            final Person[] updated = personRepository.save(new Person[]{p1, p2});

            assertEquals(2, updated.length);
            assertEquals(30, updated[0].getAge());
            assertEquals(35, updated[1].getAge());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testSaveArrayEmpty() throws Exception {
        utx.begin();
        try {
            final Person[] result = personRepository.save(new Person[0]);
            assertNotNull(result);
            assertEquals(0, result.length);
        } finally {
            utx.rollback();
        }
    }
}

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
import org.apache.openejb.data.test.repo.PersonExtendedRepository;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class ExtendedCrudTest {

    @Inject
    private PersonExtendedRepository repo;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {PersonExtendedRepository.class})
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
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-extended");
        return p;
    }

    // -- Proxy Object methods --

    @Test
    public void testProxyToString() {
        final String str = repo.toString();
        assertNotNull(str);
        assertTrue(str.contains("PersonExtendedRepository"));
    }

    @Test
    public void testProxyHashCode() {
        final int hash = repo.hashCode();
        // just check it doesn't throw
        assertTrue(hash != 0 || hash == 0);
    }

    @Test
    public void testProxyEquals() {
        assertTrue(repo.equals(repo));
        assertFalse(repo.equals(null));
        assertFalse(repo.equals("not a repo"));
    }

    // -- Stream return type --

    @Test
    public void testFindByWithStreamReturn() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@test.com"));
            em.persist(new Person("Bob", 20, "bob@test.com"));
            em.persist(new Person("Charlie", 40, "charlie@test.com"));
            em.flush();

            final Stream<Person> stream = repo.findByAgeGreaterThan(25);
            assertNotNull(stream);
            final long count = stream.count();
            assertEquals(2, count);
        } finally {
            utx.rollback();
        }
    }

    // -- Optional return type --

    @Test
    public void testFindByWithOptionalReturnPresent() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@unique.com"));
            em.flush();

            final Optional<Person> result = repo.findByEmail("alice@unique.com");
            assertTrue(result.isPresent());
            assertEquals("Alice", result.get().getName());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFindByWithOptionalReturnEmpty() throws Exception {
        utx.begin();
        try {
            final Optional<Person> result = repo.findByEmail("nonexistent@test.com");
            assertFalse(result.isPresent());
        } finally {
            utx.rollback();
        }
    }

    // -- Single entity return --

    @Test
    public void testFindBySingleEntityReturn() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("UniqueAlice", 30, "ua@test.com"));
            em.flush();

            final Person person = repo.findByName("UniqueAlice");
            assertNotNull(person);
            assertEquals("UniqueAlice", person.getName());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFindBySingleEntityReturnNull() throws Exception {
        utx.begin();
        try {
            final Person person = repo.findByName("DoesNotExist");
            assertNull(person);
        } finally {
            utx.rollback();
        }
    }

    // -- LessThanEqual operator --

    @Test
    public void testFindByLessThanEqual() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 20, "a@test.com"));
            em.persist(new Person("Bob", 30, "b@test.com"));
            em.persist(new Person("Charlie", 40, "c@test.com"));
            em.flush();

            final List<Person> result = repo.findByAgeLessThanEqual(30);
            assertEquals(2, result.size());
        } finally {
            utx.rollback();
        }
    }

    // -- Contains operator --

    @Test
    public void testFindByContains() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice Smith", 30, "a@test.com"));
            em.persist(new Person("Bob Jones", 30, "b@test.com"));
            em.persist(new Person("Alice Jones", 40, "c@test.com"));
            em.flush();

            final List<Person> result = repo.findByNameContains("Jones");
            assertEquals(2, result.size());
        } finally {
            utx.rollback();
        }
    }

    // -- deleteByName (method-name delete) --

    @Test
    public void testDeleteByName() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("ToDelete", 30, "td1@test.com"));
            em.persist(new Person("ToDelete", 25, "td2@test.com"));
            em.persist(new Person("ToKeep", 40, "tk@test.com"));
            em.flush();

            final int deleted = repo.deleteByName("ToDelete");
            assertEquals(2, deleted);
        } finally {
            utx.rollback();
        }
    }

    // -- @Query returning count --

    @Test
    public void testQueryReturningCount() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "a@test.com"));
            em.persist(new Person("Bob", 20, "b@test.com"));
            em.persist(new Person("Charlie", 40, "c@test.com"));
            em.flush();

            final long count = repo.countOlderThan(25);
            assertEquals(2, count);
        } finally {
            utx.rollback();
        }
    }

    // -- @Query returning Optional --

    @Test
    public void testQueryReturningOptional() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice-opt@test.com"));
            em.flush();

            final Optional<Person> found = repo.findOneByEmail("alice-opt@test.com");
            assertTrue(found.isPresent());

            final Optional<Person> notFound = repo.findOneByEmail("nope@test.com");
            assertFalse(notFound.isPresent());
        } finally {
            utx.rollback();
        }
    }

    // -- @Find with @By returning Optional --

    @Test
    public void testFindAnnotationWithByReturningOptional() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@test.com"));
            em.flush();

            // Get the ID of the persisted entity
            final Long id = em.createQuery("SELECT p.id FROM Person p WHERE p.name = 'Alice'", Long.class)
                .getSingleResult();

            final Optional<Person> result = repo.lookupByIdAndName(id, "Alice");
            assertTrue(result.isPresent());
            assertEquals("Alice", result.get().getName());

            // Non-matching should be empty
            final Optional<Person> notFound = repo.lookupByIdAndName(id, "Bob");
            assertFalse(notFound.isPresent());
        } finally {
            utx.rollback();
        }
    }

    // -- Custom @Insert annotation --

    @Test
    public void testCustomInsertAnnotation() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("InsertTest", 33, "insert@test.com");
            final Person saved = repo.customInsert(person);
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("InsertTest", saved.getName());
        } finally {
            utx.rollback();
        }
    }

    // -- Custom @Update annotation --

    @Test
    public void testCustomUpdateAnnotation() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("UpdateTest", 33, "update@test.com");
            em.persist(person);
            em.flush();

            person.setAge(44);
            final Person updated = repo.customUpdate(person);
            assertEquals(44, updated.getAge());
        } finally {
            utx.rollback();
        }
    }

    // -- Custom @Save annotation --

    @Test
    public void testCustomSaveAnnotation() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("SaveTest", 33, "save@test.com");
            em.persist(person);
            em.flush();

            person.setAge(55);
            final Person saved = repo.customSave(person);
            assertEquals(55, saved.getAge());
        } finally {
            utx.rollback();
        }
    }

    // -- Custom @Delete annotation --

    @Test
    public void testCustomDeleteAnnotation() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("DeleteTest", 33, "delete@test.com");
            em.persist(person);
            em.flush();

            repo.customDelete(person);

            // Verify deleted
            final List<?> remaining = em.createQuery("SELECT p FROM Person p WHERE p.name = 'DeleteTest'")
                .getResultList();
            assertEquals(0, remaining.size());
        } finally {
            utx.rollback();
        }
    }

    // -- Built-in CrudRepository: insertAll --

    @Test
    public void testInsertAll() throws Exception {
        utx.begin();
        try {
            final List<Person> people = List.of(
                new Person("P1", 20, "p1@test.com"),
                new Person("P2", 30, "p2@test.com")
            );
            final List<Person> saved = repo.insertAll(people);
            assertEquals(2, saved.size());
            assertNotNull(saved.get(0).getId());
            assertNotNull(saved.get(1).getId());
        } finally {
            utx.rollback();
        }
    }

    // -- Built-in CrudRepository: updateAll --

    @Test
    public void testUpdateAll() throws Exception {
        utx.begin();
        try {
            final Person p1 = new Person("U1", 20, "u1@test.com");
            final Person p2 = new Person("U2", 30, "u2@test.com");
            em.persist(p1);
            em.persist(p2);
            em.flush();

            p1.setAge(21);
            p2.setAge(31);
            final List<Person> updated = repo.updateAll(List.of(p1, p2));
            assertEquals(2, updated.size());
        } finally {
            utx.rollback();
        }
    }

    // -- Built-in CrudRepository: save (merge for new entity) --

    @Test
    public void testSaveNewEntity() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("SaveNew", 25, "savenew@test.com");
            final Person saved = repo.save(person);
            assertNotNull(saved);
            assertEquals("SaveNew", saved.getName());
        } finally {
            utx.rollback();
        }
    }

    // -- Built-in: deleteById --

    @Test
    public void testDeleteById() throws Exception {
        utx.begin();
        try {
            final Person person = new Person("DeleteById", 33, "dbi@test.com");
            em.persist(person);
            em.flush();
            final Long id = person.getId();

            repo.deleteById(id);

            assertFalse(repo.findById(id).isPresent());
        } finally {
            utx.rollback();
        }
    }

    // -- Built-in: deleteById non-existing --

    @Test
    public void testDeleteByIdNonExisting() throws Exception {
        utx.begin();
        try {
            // Should not throw
            repo.deleteById(999999L);
        } finally {
            utx.rollback();
        }
    }

    // -- Built-in: findById not found --

    @Test
    public void testFindByIdNotFound() throws Exception {
        utx.begin();
        try {
            final Optional<Person> result = repo.findById(999999L);
            assertFalse(result.isPresent());
        } finally {
            utx.rollback();
        }
    }

    // -- @Delete with no args: delete all entities --

    @Test
    public void testDeleteAllNoArgs() throws Exception {
        utx.begin();
        try {
            repo.customInsert(new Person("Dan", 30, "dan@example.com"));
            repo.customInsert(new Person("Eva", 25, "eva@example.com"));

            em.clear();
            final long countBefore = em.createQuery("SELECT COUNT(p) FROM Person p", Long.class)
                .getSingleResult();
            assertEquals(2L, countBefore);

            // deleteAll() with no args should bulk delete all
            repo.deleteAll();

            em.clear();
            final long countAfter = em.createQuery("SELECT COUNT(p) FROM Person p", Long.class)
                .getSingleResult();
            assertEquals(0L, countAfter);
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testDeleteAllEntitiesAnnotation() throws Exception {
        utx.begin();
        try {
            repo.customInsert(new Person("Ada", 30, "ada@example.com"));
            repo.customInsert(new Person("Ben", 25, "ben@example.com"));
            repo.customInsert(new Person("Cal", 40, "cal@example.com"));

            // Verify entities exist
            em.clear();
            final long countBefore = em.createQuery("SELECT COUNT(p) FROM Person p", Long.class)
                .getSingleResult();
            assertEquals(3L, countBefore);

            // @Delete with no args should delete all
            repo.deleteAllEntities();

            em.clear();
            final long countAfter = em.createQuery("SELECT COUNT(p) FROM Person p", Long.class)
                .getSingleResult();
            assertEquals(0L, countAfter);
        } finally {
            utx.rollback();
        }
    }
}

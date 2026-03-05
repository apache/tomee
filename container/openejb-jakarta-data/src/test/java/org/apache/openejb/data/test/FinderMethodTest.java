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
import org.apache.openejb.data.test.repo.PersonCustomRepository;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class FinderMethodTest {

    @Inject
    private PersonCustomRepository personRepo;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {PersonCustomRepository.class})
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
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-finder");
        return p;
    }

    @Test
    public void testFindByName() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@example.com"));
            em.persist(new Person("Alice", 25, "alice2@example.com"));
            em.persist(new Person("Bob", 40, "bob@example.com"));
            em.flush();

            final List<Person> result = personRepo.findByName("Alice");
            assertEquals(2, result.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFindByNameAndAge() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@test.com"));
            em.persist(new Person("Alice", 25, "alice2@test.com"));
            em.flush();

            final List<Person> result = personRepo.findByNameAndAge("Alice", 30);
            assertEquals(1, result.size());
            assertEquals(30, result.get(0).getAge());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testFindByAgeGreaterThanOrderByName() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Charlie", 50, "charlie@test.com"));
            em.persist(new Person("Alice", 40, "alice@test.com"));
            em.persist(new Person("Bob", 20, "bob@test.com"));
            em.flush();

            final List<Person> result = personRepo.findByAgeGreaterThanOrderByNameAsc(30);
            assertEquals(2, result.size());
            assertEquals("Alice", result.get(0).getName());
            assertEquals("Charlie", result.get(1).getName());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testCountByName() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "a1@test.com"));
            em.persist(new Person("Alice", 25, "a2@test.com"));
            em.persist(new Person("Bob", 40, "b@test.com"));
            em.flush();

            final long count = personRepo.countByName("Alice");
            assertEquals(2, count);
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testExistsByEmail() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@test.com"));
            em.flush();

            assertTrue(personRepo.existsByEmail("alice@test.com"));
            assertFalse(personRepo.existsByEmail("nonexistent@test.com"));
        } finally {
            utx.rollback();
        }
    }
}

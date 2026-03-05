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
import jakarta.validation.ConstraintViolationException;
import org.apache.openejb.data.test.entity.Person;
import org.apache.openejb.data.test.repo.ValidatedPersonRepository;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class BeanValidationTest {

    @Inject
    private ValidatedPersonRepository repo;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {ValidatedPersonRepository.class})
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
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-bval");
        return p;
    }

    @Test
    public void testValidParametersPasses() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Alice", 30, "alice@test.com"));
            em.flush();

            final List<Person> result = repo.findByName("Alice");
            assertEquals(1, result.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testNullNameThrowsConstraintViolation() throws Exception {
        utx.begin();
        try {
            repo.findByName(null);
            fail("Should throw ConstraintViolationException for null name");
        } catch (final ConstraintViolationException e) {
            assertNotNull(e.getConstraintViolations());
            assertFalse(e.getConstraintViolations().isEmpty());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testEmptyNameThrowsConstraintViolation() throws Exception {
        utx.begin();
        try {
            repo.findByName("");
            fail("Should throw ConstraintViolationException for empty name");
        } catch (final ConstraintViolationException e) {
            assertNotNull(e.getConstraintViolations());
            assertFalse(e.getConstraintViolations().isEmpty());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testNegativeAgeThrowsConstraintViolation() throws Exception {
        utx.begin();
        try {
            repo.findByAge(-1);
            fail("Should throw ConstraintViolationException for negative age");
        } catch (final ConstraintViolationException e) {
            assertNotNull(e.getConstraintViolations());
            assertFalse(e.getConstraintViolations().isEmpty());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testZeroAgeThrowsConstraintViolation() throws Exception {
        utx.begin();
        try {
            repo.findByAge(0);
            fail("Should throw ConstraintViolationException for zero age (@Positive)");
        } catch (final ConstraintViolationException e) {
            assertNotNull(e.getConstraintViolations());
            assertFalse(e.getConstraintViolations().isEmpty());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testPositiveAgeIsValid() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Bob", 25, "bob@test.com"));
            em.flush();

            final List<Person> result = repo.findByAge(25);
            assertEquals(1, result.size());
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testMultipleConstraintViolations() throws Exception {
        utx.begin();
        try {
            repo.findByNameAndAge(null, -5);
            fail("Should throw ConstraintViolationException for null name AND negative age");
        } catch (final ConstraintViolationException e) {
            // At least 2 violations: @NotNull on name and @Positive on age
            assertTrue(e.getConstraintViolations().size() >= 2);
        } finally {
            utx.rollback();
        }
    }

    @Test
    public void testValidMultipleParameters() throws Exception {
        utx.begin();
        try {
            em.persist(new Person("Charlie", 35, "charlie@test.com"));
            em.flush();

            final List<Person> result = repo.findByNameAndAge("Charlie", 35);
            assertEquals(1, result.size());
        } finally {
            utx.rollback();
        }
    }
}

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
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that EntityManagerLookup correctly throws an error when multiple
 * persistence units exist and no dataStore is specified on the @Repository.
 * This exercises the ambiguity detection in resolveJndiName.
 */
@RunWith(ApplicationComposer.class)
public class EntityManagerLookupAmbiguousTest {

    @Inject
    private PersonRepository repo;

    @Resource
    private UserTransaction utx;

    @Module
    @Classes(cdi = true, value = {PersonRepository.class})
    public EjbJar beans() {
        return new EjbJar();
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit1 = new PersistenceUnit("unit-one");
        unit1.setJtaDataSource("personDatabase");
        unit1.setNonJtaDataSource("personDatabaseUnmanaged");
        unit1.getClazz().add(Person.class.getName());
        unit1.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");

        final PersistenceUnit unit2 = new PersistenceUnit("unit-two");
        unit2.setJtaDataSource("personDatabase");
        unit2.setNonJtaDataSource("personDatabaseUnmanaged");
        unit2.getClazz().add(Person.class.getName());
        unit2.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");

        final Persistence persistence = new Persistence();
        persistence.getPersistenceUnit().add(unit1);
        persistence.getPersistenceUnit().add(unit2);
        return persistence;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("personDatabase", "new://Resource?type=DataSource");
        p.put("personDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("personDatabase.JdbcUrl", "jdbc:hsqldb:mem:persondb-ambiguous");
        return p;
    }

    @Test
    public void testMultiplePersistenceUnitsThrowsAmbiguityError() throws Exception {
        assertNotNull("Repository should be injected", repo);

        utx.begin();
        try {
            repo.insert(new Person("Ambiguous", 30, "ambiguous@test.com"));
            fail("Should throw IllegalStateException for ambiguous persistence units");
        } catch (final IllegalStateException e) {
            assertTrue("Error message should mention multiple persistence units",
                e.getMessage().contains("Multiple persistence units"));
        } finally {
            utx.rollback();
        }
    }
}

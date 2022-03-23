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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.openejb;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;

@RunWith(Arquillian.class)
public class JPAArquillianAdapterTest {
    @Inject
    private Persister persister;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, JPAArquillianAdapterTest.class.getSimpleName().concat(".jar"))
                .addClasses(Person.class, Persister.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsManifestResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<persistence version=\"2.0\"\n" +
                        "             xmlns=\"http://java.sun.com/xml/ns/persistence\"\n" +
                        "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "             xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence\n" +
                        "                       http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\">\n" +
                        "  <persistence-unit name=\"person\">\n" +
                        "    <jta-data-source>My DataSource</jta-data-source>\n" +
                        "    <non-jta-data-source>My Unmanaged DataSource</non-jta-data-source>\n" +
                        "    <class>" + Person.class.getName() + "</class>\n" +
                        "    <properties>\n" +
                        "      <property name=\"openjpa.jdbc.SynchronizeMappings\" value=\"buildSchema(ForeignKeys=true)\"/>\n" +
                        "    </properties>\n" +
                        "  </persistence-unit>\n" +
                        "</persistence>"), ArchivePaths.create("persistence.xml"));
    }

    @Test
    public void persist() {
        persister.persist(new Person("foo"));
    }

    @Entity
    public static class Person {
        @Id
        @GeneratedValue
        private long id;

        private String name;

        public Person() {
            // no-op
        }

        public Person(final String name) {
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Singleton
    public static class Persister {
        @PersistenceContext
        private EntityManager em;

        public void persist(final Person person) {
            em.persist(person);
        }
    }
}

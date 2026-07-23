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
package org.apache.openejb.cdi.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * Verifies the Jakarta Persistence 3.2 CDI integration: for each persistence unit the
 * container registers an {@code EntityManagerFactory} and the five utility beans, carrying
 * the qualifiers declared by the {@code <qualifier>} elements of persistence.xml, or
 * {@code @Default} when none is declared.
 */
@RunWith(ApplicationComposer.class)
public class JpaCDIExtensionTest {

    @Inject
    private QualifiedBean qualifiedBean;

    @Inject
    private DefaultBean defaultBean;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.setProperty("JpaCDIExtensionTestDb", "new://Resource?type=DataSource");
        p.setProperty("JpaCDIExtensionTestDb.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.setProperty("JpaCDIExtensionTestDb.JdbcUrl", "jdbc:hsqldb:mem:jpa-cdi-extension");
        return p;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit qualified = new PersistenceUnit("qualified-unit");
        qualified.addClass(JpaCDIExtensionPerson.class);
        qualified.setExcludeUnlistedClasses(true);
        qualified.getQualifier().add(PersonUnit.class.getName());
        qualified.setJtaDataSource("JpaCDIExtensionTestDb");
        qualified.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        qualified.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");

        final PersistenceUnit unqualified = new PersistenceUnit("default-unit");
        unqualified.addClass(JpaCDIExtensionPerson.class);
        unqualified.setExcludeUnlistedClasses(true);
        unqualified.setJtaDataSource("JpaCDIExtensionTestDb");
        unqualified.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unqualified.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");

        final Persistence persistence = new Persistence(qualified);
        persistence.getPersistenceUnit().add(unqualified);
        persistence.setVersion("3.2");
        return persistence;
    }

    @Module
    public Class<?>[] beans() {
        return new Class<?>[]{QualifiedBean.class, DefaultBean.class};
    }

    @Test
    public void qualifiedEntityManagerFactoryIsInjectable() {
        assertNotNull("EntityManagerFactory should be injectable via its persistence.xml qualifier",
                qualifiedBean.getEntityManagerFactory());
    }

    @Test
    public void qualifierSelectsTheMatchingPersistenceUnit() {
        // the two units are distinct beans, so the qualifier must not resolve to the default one
        assertNotSame(qualifiedBean.getEntityManagerFactory(), defaultBean.getEntityManagerFactory());
    }

    @Test
    public void persistenceUnitUtilIsInjectableWithTheQualifier() {
        assertNotNull("PersistenceUnitUtil should be injectable", qualifiedBean.getPersistenceUnitUtil());
    }

    @Test
    public void unqualifiedPersistenceUnitIsInjectableWithDefaultQualifier() {
        assertNotNull("A unit without <qualifier> should be injectable with @Default",
                defaultBean.getEntityManagerFactory());
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
    public @interface PersonUnit {
    }

    @ApplicationScoped
    public static class QualifiedBean {
        @Inject
        @PersonUnit
        private EntityManagerFactory entityManagerFactory;

        @Inject
        @PersonUnit
        private PersistenceUnitUtil persistenceUnitUtil;

        public EntityManagerFactory getEntityManagerFactory() {
            return entityManagerFactory;
        }

        public PersistenceUnitUtil getPersistenceUnitUtil() {
            return persistenceUnitUtil;
        }

    }

    @ApplicationScoped
    public static class DefaultBean {
        @Inject
        private EntityManagerFactory entityManagerFactory;

        public EntityManagerFactory getEntityManagerFactory() {
            return entityManagerFactory;
        }
    }
}

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
package org.apache.openejb.junit;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Rule;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AppComposerAsJUnitRuleTest {
    @Rule
    public final ApplicationComposerRule rule = new ApplicationComposerRule(this);

    @EJB
    private PersistManager persistManager;

    @Resource
    private Validator validator;

    @Resource
    private ValidatorFactory validatorFactory;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("bvalDatabase", "new://Resource?type=DataSource");
        p.put("bvalDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("bvalDatabase.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module
    public StatelessBean app() throws Exception {
        final StatelessBean bean = new StatelessBean(PersistManager.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Module
    public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(EntityToValidate.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @LocalBean
    @Stateless
    public static class PersistManager {
        @PersistenceContext
        private EntityManager em;

        @Resource
        private Validator validator;

        @Resource
        private ValidatorFactory validatorFactory;

        public void persistValid() {
            final EntityToValidate entity = new EntityToValidate();
            entity.setName("name");
            em.persist(entity);
        }

        public void persistNotValid() {
            em.persist(new EntityToValidate());
        }

        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public Validator getValidator() {
            return validator;
        }

        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public ValidatorFactory getValidatorFactory() {
            return validatorFactory;
        }
    }

    @Entity
    public static class EntityToValidate {
        @Id
        @GeneratedValue
        private long id;

        @NotNull
        @Size(min = 1, max = 5)
        private String name;

        public long getId() {
            return id;
        }

        public void setId(final long i) {
            id = i;
        }

        public String getName() {
            return name;
        }

        public void setName(final String n) {
            name = n;
        }
    }

    @Test
    public void valid() {
        persistManager.persistValid();
    }

    @Test
    public void notValid() {
        try {
            persistManager.persistNotValid();
            fail();
        } catch (final EJBException ejbException) {
            assertTrue(ejbException.getCause() instanceof ConstraintViolationException);
            final ConstraintViolationException constraintViolationException = (ConstraintViolationException) ejbException.getCause();
            assertEquals(1, constraintViolationException.getConstraintViolations().size());
        }
    }

    @Test
    public void lookupValidatorFactory() throws Exception {
        final ValidatorFactory validatorFactory = (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
        assertNotNull(validatorFactory);
    }

    @Test
    public void lookupValidator() throws Exception {
        final Validator validator = (Validator) new InitialContext().lookup("java:comp/Validator");
        assertNotNull(validator);
    }

    @Test
    public void injectionValidatorFactory() {
        final ValidatorFactory validatorFactory = persistManager.getValidatorFactory();
        assertNotNull(validatorFactory);
    }

    @Test
    public void injectionValidator() {
        final Validator validator = persistManager.getValidator();
        assertNotNull(validator);
    }

    @Test
    public void injection2ValidatorFactory() {
        assertNotNull(validatorFactory);
    }

    @Test
    public void injection2Validator() {
        assertNotNull(validator);
    }
}

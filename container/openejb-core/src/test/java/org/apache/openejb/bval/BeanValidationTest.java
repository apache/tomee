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
package org.apache.openejb.bval;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(ApplicationComposer.class)
public class BeanValidationTest {
    @EJB private PersistManager persistManager;
    @Resource private Validator validator;
    @Resource private ValidatorFactory validatorFactory;

    @Configuration public Properties config() {
        final Properties p = new Properties();
        p.put("bvalDatabase", "new://Resource?type=DataSource");
        p.put("bvalDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("bvalDatabase.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module public StatelessBean app() throws Exception {
        final StatelessBean bean = new StatelessBean(PersistManager.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Module public Persistence persistence() {
        PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(EntityToValidate.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setExcludeUnlistedClasses(true);

        Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @LocalBean
    @Stateless
    public static class PersistManager {
        @PersistenceContext private EntityManager em;

        @Resource private Validator validator;
        @Resource private ValidatorFactory validatorFactory;

        public void persistValid() {
            EntityToValidate entity = new EntityToValidate();
            entity.setName("name");
            em.persist(entity);
        }

        public void persistNotValid() {
            em.persist(new EntityToValidate());
        }

        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) public Validator getValidator() {
            return validator;
        }

        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) public ValidatorFactory getValidatorFactory() {
            return validatorFactory;
        }
    }

    @Entity
    public static class EntityToValidate {
        @Id @GeneratedValue private long id;
        @NotNull @Size(min = 1, max = 5) private String name;

        public long getId() {
            return id;
        }

        public void setId(long i) {
            id = i;
        }

        public String getName() {
            return name;
        }

        public void setName(String n) {
            name = n;
        }
    }

    @Test public void valid()  {
        persistManager.persistValid();
    }

    @Test public void notValid()  {
        try {
            persistManager.persistNotValid();
            fail();
        } catch (EJBException ejbException) {
            assertTrue(ejbException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ejbException.getCause();
            assertEquals(1, constraintViolationException.getConstraintViolations().size());
        }
    }

    @Test public void lookupValidatorFactory() throws Exception {
        ValidatorFactory validatorFactory = (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
        assertNotNull(validatorFactory);
    }

    @Test public void lookupValidator() throws Exception {
        Validator validator = (Validator)  new InitialContext().lookup("java:comp/Validator");
        assertNotNull(validator);
    }

    @Test public void injectionValidatorFactory() {
        ValidatorFactory validatorFactory = persistManager.getValidatorFactory();
        assertNotNull(validatorFactory);
    }

    @Test public void injectionValidator() {
        Validator validator = persistManager.getValidator();
        assertNotNull(validator);
    }

    @Test public void injection2ValidatorFactory() {
        assertNotNull(validatorFactory);
    }

    @Test public void injection2Validator() {
        assertNotNull(validator);
    }
}

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
package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.junit.runner.RunWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

@RunWith(ValidationRunner.class)
public class CheckPersistenceContextUsageTest {
    @Keys({@Key(value = "persistenceContextExtented.nonStateful"), @Key(value = "persistenceContextRef.noPersistenceUnits", count = 3),
        @Key(value = "persistenceContextAnnotation.onClassWithNoName"), @Key(value = "persistenceContextAnnotation.onEntityManagerFactory"),
        @Key(value = "persistenceContextAnnotation.onNonEntityManager")})
    public EjbJar wrongUsage() throws OpenEJBException {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStateless.class));
        return ejbJar;
    }

    @Keys({@Key(value = "persistenceContextRef.noUnitName"), @Key(value = "persistenceContextRef.noMatches")})
    public AppModule noUnitName() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStatelessOne.class));
        final EjbModule ejbModule = new EjbModule(ejbJar);
        final AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);
        final PersistenceUnit pu = new PersistenceUnit("fooUnit");
        final PersistenceUnit pu1 = new PersistenceUnit("fooUnit1");
        final PersistenceUnit pu2 = new PersistenceUnit("fooUnit");
        final org.apache.openejb.jee.jpa.unit.Persistence p = new org.apache.openejb.jee.jpa.unit.Persistence(pu, pu1, pu2);
        final PersistenceModule pm = new PersistenceModule("foo", p);
        appModule.addPersistenceModule(pm);
        return appModule;
    }

    @Keys({@Key(value = "persistenceContextRef.vagueMatches")})
    public AppModule vagueMatches() {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStatelessTwo.class));
        final EjbModule ejbModule = new EjbModule(ejbJar);
        final AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);
        final PersistenceUnit pu = new PersistenceUnit("fooUnit");
        final org.apache.openejb.jee.jpa.unit.Persistence p = new org.apache.openejb.jee.jpa.unit.Persistence(pu);
        final PersistenceModule pm = new PersistenceModule("foo", p);
        appModule.getPersistenceModules().add(pm);
        final PersistenceUnit pu1 = new PersistenceUnit("fooUnit");
        final org.apache.openejb.jee.jpa.unit.Persistence p1 = new org.apache.openejb.jee.jpa.unit.Persistence(pu1);
        final PersistenceModule pm1 = new PersistenceModule("foo1", p1);
        appModule.addPersistenceModule(pm1);
        return appModule;
    }

    @PersistenceContext
    private static class FooStateless {
        @PersistenceContext(type = PersistenceContextType.EXTENDED)
        EntityManager em;
        @PersistenceContext
        EntityManagerFactory emf;
        @PersistenceContext
        String nonEntityManager;
    }

    private static class FooStatelessOne {
        @PersistenceContext
        EntityManager em;
        @PersistenceContext(unitName = "wrongName")
        EntityManager em1;
    }

    private static class FooStatelessTwo {
        @PersistenceContext(unitName = "fooUnit")
        EntityManager em1;
    }
}

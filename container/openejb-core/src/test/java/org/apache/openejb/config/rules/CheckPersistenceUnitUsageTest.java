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

@RunWith(ValidationRunner.class)
public class CheckPersistenceUnitUsageTest {
    @Keys({@Key(value = "persistenceUnitRef.noPersistenceUnits", count = 3), @Key("persistenceUnitAnnotation.onClassWithNoName"),
        @Key("persistenceUnitAnnotation.onEntityManager"), @Key("persistenceUnitAnnotation.onNonEntityManagerFactory")})
    public EjbJar wrongUsage() throws OpenEJBException {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStateless.class));
        return ejbJar;
    }

    @Keys({@Key(value = "persistenceUnitRef.noMatches"), @Key(value = "persistenceUnitRef.noUnitName")})
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

    @Keys({@Key(value = "persistenceUnitRef.vagueMatches")})
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

    @jakarta.persistence.PersistenceUnit
    private static class FooStateless {
        @jakarta.persistence.PersistenceUnit
        EntityManagerFactory emf;
        @jakarta.persistence.PersistenceUnit
        EntityManager em;
        @jakarta.persistence.PersistenceUnit
        String nonEntityManagerFactory;
    }

    private static class FooStatelessOne {
        @jakarta.persistence.PersistenceUnit(unitName = "wrongName")
        EntityManagerFactory emf;
        @jakarta.persistence.PersistenceUnit
        EntityManagerFactory emf1;
    }

    private static class FooStatelessTwo {
        @jakarta.persistence.PersistenceUnit(unitName = "fooUnit")
        EntityManagerFactory emf;
    }
}

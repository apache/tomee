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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.PersistenceModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.junit.runner.RunWith;

@RunWith(ValidationRunner.class)
public class CheckPersistenceContextUsageTest {
    @Keys( { @Key(value = "persistenceContextExtented.nonStateful"), @Key(value = "persistenceContextRef.noPersistenceUnits", count = 3),
            @Key(value = "persistenceContextAnnotation.onClassWithNoName"), @Key(value = "persistenceContextAnnotation.onEntityManagerFactory"),
            @Key(value = "persistenceContextAnnotation.onNonEntityManager") })
    public EjbJar wrongUsage() throws OpenEJBException {
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStateless.class));
        return ejbJar;
    }

    @Keys( { @Key(value = "persistenceContextRef.noUnitName"),@Key(value = "persistenceContextRef.noMatches") })
    public AppModule noUnitName() {
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStatelessOne.class));
        EjbModule ejbModule = new EjbModule(ejbJar);
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);
        PersistenceUnit pu = new PersistenceUnit("fooUnit");
        PersistenceUnit pu1 = new PersistenceUnit("fooUnit1");
        PersistenceUnit pu2 = new PersistenceUnit("fooUnit");
        org.apache.openejb.jee.jpa.unit.Persistence p = new org.apache.openejb.jee.jpa.unit.Persistence(pu, pu1, pu2);
        PersistenceModule pm = new PersistenceModule("foo", p);
        appModule.getPersistenceModules().add(pm);
        return appModule;
    }

    @Keys( { @Key(value = "persistenceContextRef.vagueMatches") })
    public AppModule vagueMatches() {
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStatelessTwo.class));
        EjbModule ejbModule = new EjbModule(ejbJar);
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);
        PersistenceUnit pu = new PersistenceUnit("fooUnit");
        org.apache.openejb.jee.jpa.unit.Persistence p = new org.apache.openejb.jee.jpa.unit.Persistence(pu);
        PersistenceModule pm = new PersistenceModule("foo", p);
        appModule.getPersistenceModules().add(pm);
        PersistenceUnit pu1 = new PersistenceUnit("fooUnit");
        org.apache.openejb.jee.jpa.unit.Persistence p1 = new org.apache.openejb.jee.jpa.unit.Persistence(pu1);
        PersistenceModule pm1 = new PersistenceModule("foo1", p1);
        appModule.getPersistenceModules().add(pm1);
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
        @PersistenceContext(unitName="wrongName")
        EntityManager em1;
    }

    private static class FooStatelessTwo {
        @PersistenceContext(unitName = "fooUnit")
        EntityManager em1;
    }
}

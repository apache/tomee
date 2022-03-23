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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@RunWith(ValidationRunner.class)
public class MistakenResourceRefUsageTest {
    @Keys({@Key(value = "resourceRef.onEntityManagerFactory", count = 2), @Key(value = "resourceRef.onEntityManager", count = 2),
        @Key(value = "resourceAnnotation.onClassWithNoName", count = 2)})
    public EjbJar wrongUsage() throws OpenEJBException {
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(FooStateless.class));
        ejbJar.addEnterpriseBean(new StatefulBean(FooStateful.class));
        return ejbJar;
    }

    @Resource
    private static class FooStateless {
        @Resource
        EntityManagerFactory emf;
        @Resource
        EntityManager em;
    }

    @Resource
    private static class FooStateful {
        @Resource
        EntityManagerFactory emf;
        @Resource
        EntityManager em;
    }
}

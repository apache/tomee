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
import org.apache.openejb.jee.SingletonBean;
import org.junit.runner.RunWith;

import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;

@RunWith(ValidationRunner.class)
public class CheckDependsOnTest {
    @Keys({@Key(value = "dependsOn.circuit", count = 2), @Key(value = "dependsOn.noSuchEjb", count = 2)})
    public EjbJar dependsOn() throws OpenEJBException {
        final EjbJar ejbJar = new EjbJar();
        final SingletonBean one = new SingletonBean(One.class);
        final SingletonBean two = new SingletonBean(Two.class);
        final SingletonBean three = new SingletonBean(Three.class);
        final SingletonBean four = new SingletonBean(Four.class);
        final SingletonBean five = new SingletonBean(Five.class);
        final SingletonBean six = new SingletonBean(Six.class);
        ejbJar.addEnterpriseBean(one);
        ejbJar.addEnterpriseBean(two);
        ejbJar.addEnterpriseBean(three);
        ejbJar.addEnterpriseBean(four);
        ejbJar.addEnterpriseBean(five);
        ejbJar.addEnterpriseBean(six);
        return ejbJar;
    }

    @Singleton
    @DependsOn("Two")
    private static class One {
    }

    @Singleton
    @DependsOn("One")
    private static class Two {
    }

    @Singleton
    @DependsOn("Four")
    private static class Three {
    }

    @Singleton
    @DependsOn("Three")
    private static class Four {
    }

    @Singleton
    @DependsOn("WrongOne")
    private static class Five {
    }

    @Singleton
    @DependsOn("WrongOne")
    private static class Six {
    }
}

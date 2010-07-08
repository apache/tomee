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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.singleton;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.OpenEJBException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class DependsOnTest extends TestCase {
    private static final String one = "one";
    private static final String two = "two";
    private static final String three = "three";
    private static final String four = "four";

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

        StatelessSessionContainerInfo statelessContainer = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainer.properties.setProperty("MinSize", "1");
        assembler.createContainer(statelessContainer);
        
        actual.clear();

        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Three.class));

        // startup and trigger @PostConstruct
        assembler.createApplication(config.configureApplication(ejbJar));

        assertEquals(expected(four, three, two, one), actual);

        actual.clear();

        // startup and trigger @PreDestroy
        for (AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.jarPath);
        }

        assertEquals(expected(one, two, three, four), actual);
    }

    public void testNoSuchEjb() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));


        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Three.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class)).setDependsOn("Five");

        try {
            config.configureApplication(ejbJar);
            fail("Validation should have found a circular reference");
        } catch (ValidationFailedException e) {
            ValidationFailure[] failures = e.getFailures();
            assertEquals(1, failures.length);
            assertEquals("dependsOn.noSuchEjb", failures[0].getMessageKey());
        }
    }

    public void testCircuit() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));


        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Three.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class)).setDependsOn("One");

        try {
            config.configureApplication(ejbJar);
            fail("Validation should have found a circular reference");
        } catch (ValidationFailedException e) {
            ValidationFailure[] failures = e.getFailures();
            assertEquals(1, failures.length);
            assertEquals("dependsOn.circuit", failures[0].getMessageKey());
        }
    }

    private List<String> expected(String... strings) {
        return Arrays.asList(strings);
    }

    private final static List<String> actual = new ArrayList<String>();

    public static interface Bean {

    }

    @Singleton
    @Startup
    @DependsOn("Two")
    public static class One implements Bean {

        @PostConstruct
        @PreDestroy
        public void callback() {
            actual.add(one);
        }
    }

    @Singleton
    @Startup
    @DependsOn("Three")
    public static class Two implements Bean {

        @PostConstruct
        @PreDestroy
        public void callback() {
            actual.add(two);
        }
    }

    @Singleton
    @Startup
    @DependsOn("Four")
    public static class Three implements Bean {

        @PostConstruct
        @PreDestroy
        public void callback() {
            actual.add(three);
        }
    }

    @Singleton
    @Startup
    public static class Four implements Bean {

        @PostConstruct
        @PreDestroy
        public void callback() {
            actual.add(four);
        }
    }


}

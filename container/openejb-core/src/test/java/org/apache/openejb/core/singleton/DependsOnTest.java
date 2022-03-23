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
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateless;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class DependsOnTest extends TestCase {
    private static final String one = "one";
    private static final String two = "two";
    private static final String three = "three";
    private static final String four = "four";

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

        final StatelessSessionContainerInfo statelessContainer = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainer.properties.setProperty("MinSize", "1");
        statelessContainer.properties.setProperty("MaxSize", "1");
        assembler.createContainer(statelessContainer);

        actualConstruct.clear();

        final EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Three.class));

        // startup and trigger @PostConstruct
        assembler.createApplication(config.configureApplication(ejbJar));

        assertEquals(expected(four, three, two, one), actualConstruct);

        actualDestroy.clear();

        // startup and trigger @PreDestroy
        for (final AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.path);
        }

        assertEquals(expected(one, two, three, four), actualDestroy);
    }

    public void testNoStartUp() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

        actualConstruct.clear();

        final EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new SingletonBean(Two.class)).setInitOnStartup(false);
        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class)).setInitOnStartup(false);
        ejbJar.addEnterpriseBean(new SingletonBean(Three.class)).setInitOnStartup(false);

        // startup and trigger @PostConstruct
        assembler.createApplication(config.configureApplication(ejbJar));

        assertEquals(expected(four, three, two, one), actualConstruct);

        actualDestroy.clear();

        // startup and trigger @PreDestroy
        for (final AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.path);
        }

        assertEquals(expected(one, two, three, four), actualDestroy);
    }

    public void testNoSuchEjb() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));


        final EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Three.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class)).setDependsOn("Five");

        try {
            config.configureApplication(ejbJar);
            fail("Validation should have found a circular reference");
        } catch (final ValidationFailedException e) {
            final ValidationFailure[] failures = e.getFailures();
            assertEquals(1, failures.length);
            assertEquals("dependsOn.noSuchEjb", failures[0].getMessageKey());
        }
    }

    public void testCircuit() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));


        final EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new SingletonBean(One.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Two.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Three.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Four.class)).setDependsOn("One");

        try {
            config.configureApplication(ejbJar);
            fail("Validation should have found a circular reference");
        } catch (final ValidationFailedException e) {
            final ValidationFailure[] failures = e.getFailures();
            assertEquals(1, failures.length);
            assertEquals("dependsOn.circuit", failures[0].getMessageKey());
        }
    }

    private List<String> expected(final String... strings) {
        return Arrays.asList(strings);
    }

    private final static List<String> actualConstruct = new ArrayList<String>();

    private final static List<String> actualDestroy = new ArrayList<String>();

    public static interface Bean {

    }

    @Singleton
    @Startup
    @DependsOn("Two")
    public static class One implements Bean {

        @PostConstruct
        public void callbackConstruct() {
            actualConstruct.add(one);
        }

        @PreDestroy
        public void callbackDestroy() {
            actualDestroy.add(one);
        }
    }

    @Singleton
    @Startup
    @DependsOn("Three")
    public static class Two implements Bean {

        @PostConstruct
        public void callbackConstruct() {
            actualConstruct.add(two);
        }

        @PreDestroy
        public void callbackDestroy() {
            actualDestroy.add(two);
        }
    }

    @Singleton
    @Startup
    @DependsOn("Four")
    public static class Three implements Bean {

        @PostConstruct
        public void callbackConstruct() {
            actualConstruct.add(three);
        }

        @PreDestroy
        public void callbackDestroy() {
            actualDestroy.add(three);
        }
    }

    @Singleton
    @Startup
    public static class Four implements Bean {

        @PostConstruct
        public void callbackConstruct() {
            actualConstruct.add(four);
        }

        @PreDestroy
        public void callbackDestroy() {
            actualDestroy.add(four);
        }
    }
}

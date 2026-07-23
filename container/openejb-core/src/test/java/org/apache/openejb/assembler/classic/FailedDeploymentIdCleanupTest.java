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
package org.apache.openejb.assembler.classic;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * TOMEE-4655: when a deployment fails, the deployment ids it already registered must be
 * released again. Otherwise the next application reusing one of those ids fails with a
 * DuplicateDeploymentIdException before any of its own checks get a chance to run.
 */
public class FailedDeploymentIdCleanupTest {

    private static final String DEPLOYMENT_ID = "TheSharedDeploymentId";

    @After
    public void tearDown() {
        OpenEJB.destroy();
    }

    @Test
    public void deploymentIdIsReusableAfterAFailedCdiDeployment() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // this app fails while CDI is starting, i.e. after initEjbs already registered the id
        try {
            assembler.createApplication(config.configureApplication(failingModule()));
            fail("the deployment was expected to fail while starting CDI");
        } catch (final Exception expected) {
            // that is the point of the test
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        assertNull("the failed deployment leaked its deployment id",
                containerSystem.getBeanContext(DEPLOYMENT_ID));

        // a later, perfectly valid app reusing the same id must deploy just fine
        assembler.createApplication(config.configureApplication(workingModule()));

        assertNotNull("the deployment id could not be reused after a failed deployment",
                containerSystem.getBeanContext(DEPLOYMENT_ID));
    }

    private EjbModule failingModule() {
        // the unsatisfied injection point makes OWB fail the application start
        return module("failing-app", BrokenSingleton.class);
    }

    private EjbModule workingModule() {
        return module("working-app", WorkingSingleton.class);
    }

    private EjbModule module(final String moduleId, final Class<?> beanClass) {
        final EjbJar ejbJar = new EjbJar(moduleId);
        ejbJar.addEnterpriseBean(new SingletonBean(beanClass));

        final OpenejbJar openejbJar = new OpenejbJar();
        final EjbDeployment deployment = new EjbDeployment();
        deployment.setEjbName(beanClass.getSimpleName());
        deployment.setDeploymentId(DEPLOYMENT_ID);
        openejbJar.addEjbDeployment(deployment);

        final EjbModule module = new EjbModule(ejbJar, openejbJar);
        module.setModuleId(moduleId);
        module.setBeans(new Beans());
        return module;
    }

    public interface NoImplementationAnywhere {
        void doSomething();
    }

    @Singleton
    public static class BrokenSingleton {
        @Inject
        private NoImplementationAnywhere unsatisfied;
    }

    @Singleton
    public static class WorkingSingleton {
        public String hello() {
            return "hello";
        }
    }
}

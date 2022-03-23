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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.ejb.Stateless;


public class ApplicationWideTest extends TestCase {

    public void testShouldCreateAResourceAndNotRemoveOnUndeploy() throws Exception {
        final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
        final EjbJar ejbJar = ejbModule.getEjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(EchoBean.class));

        final AppModule appModule = new AppModule(ejbModule);
        final Container container = new Container();
        container.setId("My Container");
        container.setCtype("STATELESS");
        container.getProperties().setProperty("ApplicationWide", "true");
        appModule.getContainers().add(container);

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();
        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        final AppInfo appInfo = config.configureApplication(appModule);
        assembler.createApplication(appInfo);

        {
            final ContainerSystem containerSystem = assembler.getContainerSystem();
            final org.apache.openejb.Container appContainer = containerSystem.getContainer(ejbModule.getModuleId() + "/My Container");
            assertNotNull(appContainer);
        }

        assembler.destroyApplication(appInfo);

        {
            final ContainerSystem containerSystem = assembler.getContainerSystem();
            final org.apache.openejb.Container appContainer = containerSystem.getContainer(ejbModule.getModuleId() + "/My Container");
            assertNotNull(appContainer);
        }
    }

    public void testShouldCreateAResourceAndRemoveOnUndeploy() throws Exception {
        final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
        final EjbJar ejbJar = ejbModule.getEjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(EchoBean.class));

        final AppModule appModule = new AppModule(ejbModule);
        final Container container = new Container();
        container.setId("My Container");
        container.setCtype("STATELESS");
        container.getProperties().setProperty("ApplicationWide", "false");
        appModule.getContainers().add(container);

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();
        { // setup the system
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        final AppInfo appInfo = config.configureApplication(appModule);
        assembler.createApplication(appInfo);

        {
            final ContainerSystem containerSystem = assembler.getContainerSystem();
            final org.apache.openejb.Container appContainer = containerSystem.getContainer(ejbModule.getModuleId() + "/My Container");
            assertNotNull(appContainer);
        }

        assembler.destroyApplication(appInfo);

        {
            final ContainerSystem containerSystem = assembler.getContainerSystem();
            final org.apache.openejb.Container appContainer = containerSystem.getContainer(ejbModule.getModuleId() + "/My Container");
            assertNull(appContainer);
        }
    }

    @Stateless
    public static class EchoBean {
        public String echo(final String input) {
            return input;
        }
    }

}

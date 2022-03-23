/*
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
package org.apache.openejb;

import junit.framework.TestCase;
import org.apache.bval.jsr.ConfigurationImpl;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.validation.ValidationException;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContextOptionsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        SystemInstance.reset();

        try { // hack for buildbot
            new ConfigurationImpl(null, null);
        } catch (final ValidationException ve) {
            // no-op
        }
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void testBeanContextOptions() throws Exception {

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        {
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            // Setup the descriptor information
            final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
            final EjbJar ejbJar = ejbModule.getEjbJar();
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            final StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

            { // Set at BeanContext level
                final EjbDeployment deployment = openejbJar.addEjbDeployment(statelessBean);
                deployment.getProperties().put("color", "orange");
            }

            assembler.createApplication(config.configureApplication(ejbModule));
        }


        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        assertOption(beanContext.getOptions(), "color", "orange");
        assertNoOption(beanContext.getModuleContext().getOptions(), "color");
        assertNoOption(beanContext.getModuleContext().getAppContext().getOptions(), "color");
    }

    public void testModuleContextOptions() throws Exception {

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        {
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            // Setup the descriptor information
            final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());

            final EjbJar ejbJar = ejbModule.getEjbJar();
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            openejbJar.getProperties().setProperty("color", "orange");

            ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));
            assembler.createApplication(config.configureApplication(ejbModule));
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        assertOption(beanContext.getOptions(), "color", "orange");
        assertOption(beanContext.getModuleContext().getOptions(), "color", "orange");
        assertNoOption(beanContext.getModuleContext().getAppContext().getOptions(), "color");
    }

    public void testAppContextOptions() throws Exception {

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        {
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            // Setup the descriptor information
            final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
            final EjbJar ejbJar = ejbModule.getEjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

            final AppModule appModule = new AppModule(ejbModule);
            appModule.getProperties().setProperty("color", "orange");

            assembler.createApplication(config.configureApplication(appModule));
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        assertOption(beanContext.getOptions(), "color", "orange");
        assertOption(beanContext.getModuleContext().getOptions(), "color", "orange");
        assertOption(beanContext.getModuleContext().getAppContext().getOptions(), "color", "orange");
    }

    public void testSystemInstanceOptions() throws Exception {

        SystemInstance.get().setProperty("color", "orange");
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        {
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            // Setup the descriptor information
            final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
            final EjbJar ejbJar = ejbModule.getEjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

            final AppModule appModule = new AppModule(ejbModule);
            assembler.createApplication(config.configureApplication(appModule));
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        assertOption(beanContext.getOptions(), "color", "orange");
        assertOption(beanContext.getModuleContext().getOptions(), "color", "orange");
        assertOption(beanContext.getModuleContext().getAppContext().getOptions(), "color", "orange");
        assertOption(SystemInstance.get().getOptions(), "color", "orange");
    }


    public void testAllLevels() throws Exception {

        SystemInstance.get().setProperty("color", "orangeSystem");
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        {
            assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
            assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        }

        {
            // Setup the descriptor information
            final EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
            final EjbJar ejbJar = ejbModule.getEjbJar();
            final StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

            { // Set at BeanContext level
                final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
                final EjbDeployment deployment = openejbJar.addEjbDeployment(statelessBean);
                deployment.getProperties().put("color", "orangeBean");
            }

            { // Set at ModuleContext level
                final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
                openejbJar.getProperties().put("color", "orangeModule");
            }

            final AppModule appModule = new AppModule(ejbModule);
            { // Set at AppContext level
                appModule.getProperties().put("color", "orangeApp");
            }
            assembler.createApplication(config.configureApplication(appModule));
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        assertOption(beanContext.getOptions(), "color", "orangeBean");
        assertOption(beanContext.getModuleContext().getOptions(), "color", "orangeModule");
        assertOption(beanContext.getModuleContext().getAppContext().getOptions(), "color", "orangeApp");
        assertOption(SystemInstance.get().getOptions(), "color", "orangeSystem");
    }

    private void assertOption(final Options options, final String key, final String value) {
        assertEquals(value, options.get(key, key + " (not set)"));
    }

    private void assertNoOption(final Options options, final String key) {
        final String defaultValue = key + " (not set)";
        assertEquals(defaultValue, options.get(key, defaultValue));
    }

    public static interface Widget {

    }

    public static class WidgetBean implements Widget {

    }
}

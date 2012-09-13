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
package org.apache.openejb;

import junit.framework.TestCase;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentContextPropertiesTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        SystemInstance.reset();
        Thread.currentThread().getContextClassLoader().loadClass("org.apache.bval.jsr303.ConfigurationImpl");
    }

    public void testBeanContextProperties() throws Exception {

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
            final EjbDeployment deployment = openejbJar.addEjbDeployment(statelessBean);

            deployment.getProperties().put("color", "orange");
            assembler.createApplication(config.configureApplication(ejbModule));
        }


        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        final Properties properties = beanContext.getProperties();
        assertProperty(properties, "color", "orange");
    }

    public void testModuleContextProperties() throws Exception {

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


            final EjbJarInfo moduleInfo = config.configureApplication(ejbModule);
            assertProperty(moduleInfo.properties, "color" , "orange");

            assembler.createApplication(moduleInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        final Properties properties = beanContext.getModuleContext().getProperties();

        assertProperty(properties, "color", "orange");
    }

    public void testAppContextProperties() throws Exception {

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


            final AppInfo appInfo = config.configureApplication(appModule);

            assertProperty(appInfo.properties, "color" , "orange");

            assembler.createApplication(appInfo);
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        final Properties properties = beanContext.getModuleContext().getAppContext().getProperties();

        assertProperty(properties, "color", "orange");
    }

    private void assertProperty(Properties properties, final String key, final String value) {
        assertTrue(properties.containsKey(key));
        assertEquals(value, properties.getProperty(key));
    }


    public static interface Widget {

    }

    public static class WidgetBean implements Widget {

    }
}

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
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
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
public class EjbDeploymentPropertiesTest extends TestCase {

    public void test() throws Exception {
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Setup the descriptor information

        EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));
        EjbDeployment deployment = openejbJar.addEjbDeployment(statelessBean);
        deployment.getProperties().put("color", "orange");

        assembler.createApplication(config.configureApplication(ejbModule));

        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

        BeanContext beanContext = containerSystem.getBeanContext("WidgetBean");

        Properties properties = beanContext.getProperties();
        assertTrue(properties.containsKey("color"));
        assertEquals("orange", properties.getProperty("color"));
    }


    public static interface Widget {

    }

    public static class WidgetBean implements Widget {

    }
}

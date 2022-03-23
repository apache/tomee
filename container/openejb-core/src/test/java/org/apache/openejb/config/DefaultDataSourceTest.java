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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.AfterClass;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

/**
 * @version $Rev$ $Date$
 */
public class DefaultDataSourceTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    /**
     * Default DataSource should be a JtaManaged DataSource
     *
     * @throws Exception
     */
    public void test() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(MyBean.class));
        assembler.createApplication(config.configureApplication(new EjbModule(ejbJar)));

        final OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        final ResourceInfo resourceInfo = configuration.facilities.resources.get(0);

        assertEquals("Default JDBC Database", resourceInfo.id);
        assertEquals("true", resourceInfo.properties.getProperty("JtaManaged"));

    }


    public static class MyBean {

        @Resource
        private DataSource dataSource;

    }
}

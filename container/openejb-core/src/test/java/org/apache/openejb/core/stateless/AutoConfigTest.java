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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.ejb.SessionContext;
import javax.annotation.Resource;
import java.util.List;
import java.util.Arrays;
import java.util.Stack;

import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ConnectionManagerInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.EjbJar;

/**
 * @version $Revision: 570984 $ $Date: 2007-08-29 16:42:12 -0700 (Wed, 29 Aug 2007) $
 */
public class AutoConfigTest extends TestCase {


    public void _test() throws Exception {

    }
    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createConnectionManager(config.configureService(ConnectionManagerInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        fail();
    }

    public static interface Widget {
    }

    public static class WidgetBean implements Widget {

        @Resource javax.jms.Queue redQueue;
        @Resource javax.jms.Queue blueQueue;
        @Resource javax.jms.Queue greenQueue;

        @Resource javax.sql.DataSource yellowDataSource;
        @Resource javax.sql.DataSource orangeDataSource;
        @Resource javax.sql.DataSource purpleDataSource;
    }
}

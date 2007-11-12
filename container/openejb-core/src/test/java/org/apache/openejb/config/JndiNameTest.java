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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import javax.ejb.Local;
import javax.naming.InitialContext;

/**
 * @version $Rev$ $Date$
 */
public class JndiNameTest extends TestCase {

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
        ejbModule.getEjbJar().addEnterpriseBean(new StatelessBean(FooBean.class));

        EjbDeployment ejbDeployment = new EjbDeployment(null, "FooBean", "FooBean");
        ejbDeployment.getJndi().add(new Jndi("thename", "Local"));
        ejbModule.getOpenejbJar().addEjbDeployment(ejbDeployment);

        assembler.createApplication(config.configureApplication(ejbModule));

        InitialContext initialContext = new InitialContext();
        Object o = initialContext.lookup("thename");
        assertNotNull(o);
    }

    @Local
    public static interface Foo {

    }

    public static class FooBean implements Foo {

    }
}

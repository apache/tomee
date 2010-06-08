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
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.SessionBeanType;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.EJBHome;
import javax.ejb.RemoteHome;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.LocalHome;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
        ejbDeployment.getJndi().add(new Jndi("anothername", "Remote"));
        ejbDeployment.getJndi().add(new Jndi("loldstyle", "LocalHome"));
        ejbDeployment.getJndi().add(new Jndi("roldstyle", "RemoteHome"));
        ejbModule.getOpenejbJar().addEjbDeployment(ejbDeployment);

        assembler.createApplication(config.configureApplication(ejbModule));

        InitialContext initialContext = new InitialContext();
        assertName(initialContext, Orange.class, "thename");
        assertName(initialContext, Red.class, "anothername");
        assertName(initialContext, LHYellow.class, "loldstyle");
        assertName(initialContext, RHGreen.class, "roldstyle");
    }

    public void testOpenejbJar2() throws Exception {
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

        EjbModule ejbModule = new EjbModule(new EjbJar(), null);
        ejbModule.getEjbJar().addEnterpriseBean(new StatelessBean(FooBean.class));

        OpenejbJarType v2 = new OpenejbJarType();
        SessionBeanType ejbDeployment = new SessionBeanType();
        ejbDeployment.setEjbName("FooBean");
        ejbDeployment.getJndi().add(new org.apache.openejb.jee.oejb2.Jndi("thename", "Local"));
        ejbDeployment.getJndi().add(new org.apache.openejb.jee.oejb2.Jndi("anothername", "Remote"));
        ejbDeployment.getJndi().add(new org.apache.openejb.jee.oejb2.Jndi("loldstyle", "LocalHome"));
        ejbDeployment.getJndi().add(new org.apache.openejb.jee.oejb2.Jndi("roldstyle", "RemoteHome"));
        v2.getEnterpriseBeans().add(ejbDeployment);
        ejbModule.getAltDDs().put("openejb-jar.xml", v2);

        assembler.createApplication(config.configureApplication(ejbModule));

        InitialContext initialContext = new InitialContext();
        assertName(initialContext, Orange.class, "thename");
        assertName(initialContext, Red.class, "anothername");
        assertName(initialContext, LHYellow.class, "loldstyle");
        assertName(initialContext, RHGreen.class, "roldstyle");
    }

    private void assertName(InitialContext initialContext, Class<?> clazz, String name) throws NamingException {
        Object o = initialContext.lookup(name);
        assertNotNull(o);
        assertTrue(clazz.isAssignableFrom(o.getClass()));
    }

    @Local
    public static interface Orange {
    }

    @Remote
    public static interface Red {
    }

    public static interface RHGreen extends EJBHome {
        RGreen create();
    }

    public static interface RGreen extends EJBObject {
    }

    public static interface LHYellow extends EJBLocalHome {
        LYellow create();
    }

    public static interface LYellow extends EJBLocalObject {
    }

    @RemoteHome(RHGreen.class)
    @LocalHome(LHYellow.class)
    public static class FooBean implements Orange, Red {

    }
}

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

import java.util.Properties;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;

/**
 * Test to ensure that the role-name/role-link elements in security-role-ref work correctly
 */
public class EjbSecurityRoleRefTest extends TestCase {
    private InitialContext context;
    private Assembler assembler;
    private ConfigurationFactory config;

    protected void setUp() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        final Properties props = new Properties();
        props.setProperty(Context.SECURITY_PRINCIPAL, "jonathan");
        props.setProperty(Context.SECURITY_CREDENTIALS, "secret");
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        context = new InitialContext(props);
    }

    protected void tearDown() throws Exception {
        for (final AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.path);
        }
        SystemInstance.get().setComponent(Assembler.class, null);
        SystemInstance.get().setComponent(ContainerSystem.class, null);
        super.tearDown();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void testShouldCheckUserRole() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean statelessBean = new StatelessBean(UserBean.class);
        final SecurityRoleRef securityRoleRef = new SecurityRoleRef();
        securityRoleRef.setRoleName("TEST");
        securityRoleRef.setRoleLink("committer");
        statelessBean.getSecurityRoleRef().add(securityRoleRef);
        ejbJar.addEnterpriseBean(statelessBean);

        final AppModule app = new AppModule(this.getClass().getClassLoader(), "classpath-" + ejbJar.hashCode());
        app.getEjbModules().add(new EjbModule(ejbJar));
        assembler.createApplication(config.configureApplication(app));

        final User user = (User) context.lookup("UserBeanLocal");
        assertTrue(user.isUserInRole());
    }

    public static interface User {
        public boolean isUserInRole();
    }

    public static class UserBean implements User {

        @Resource
        private SessionContext context;

        @Override
        public boolean isUserInRole() {
            return context.isCallerInRole("TEST");
        }
    }
}

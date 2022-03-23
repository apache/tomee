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
package org.apache.openejb.core.security;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.AfterClass;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class SecurityTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    private Assembler configureAssembler(final String defaultUser) throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));

        final SecurityServiceInfo serviceInfo = new SecurityServiceInfo();
        serviceInfo.service = "SecurityService";
        serviceInfo.className = SecurityServiceImpl.class.getName();
        serviceInfo.id = "New Security Service";
        serviceInfo.properties = new Properties();
        if (defaultUser != null) {
            // override the default user
            serviceInfo.properties.setProperty("DefaultUser", defaultUser);

        }

        assembler.createSecurityService(serviceInfo);

        // containers
        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        final EjbJar ejbJar = new EjbJar("SecurityTest");

        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(BarBean.class));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        assembler.createApplication(ejbJarInfo);

        return assembler;
    }

    public void test() throws Exception {
        final Assembler assembler = configureAssembler(null);

        final Properties props = new Properties();
        props.setProperty(Context.SECURITY_PRINCIPAL, "jonathan");
        props.setProperty(Context.SECURITY_CREDENTIALS, "secret");

        final InitialContext ctx = new InitialContext(props);

        final Project foo = (Project) ctx.lookup("FooBeanLocal");

        foo.svnCheckout("");
        foo.svnCommit("");

        try {
            foo.deleteProject("");
            fail("Should not be allowed");
        } catch (final Exception e) {
            // good.
        }

        assertTrue("not in role committer", foo.isCallerInRole("committer"));
        assertTrue("not in role community", foo.isCallerInRole("community"));
        assertFalse("in role contributor", foo.isCallerInRole("contributor"));
        assertTrue("Caller is not jonathan", foo.isCaller("jonathan"));

        ctx.close();
        assembler.destroy();

        //        Project bar = (Project) ctx.lookup("BarBeanLocal");
        //
        //        bar.svnCheckout("");
        //
        //        try {
        //            bar.svnCommit("");
        //            fail("Should not be allowed");
        //        } catch (Exception e) {
        //            // good
        //        }
        //
        //        try {
        //            bar.deleteProject("");
        //            fail("Should not be allowed");
        //        } catch (Exception e) {
        //            // good.
        //        }
        //
        //        assertFalse("in role committer", bar.isCallerInRole("committer"));
        //        assertFalse("in role community", bar.isCallerInRole("community"));
        //        assertTrue("not in role contributor", bar.isCallerInRole("contributor"));
    }

    // When no credentials are provided, the default user/role should be "guest"
    public void testUnauthenticatedUser() throws Exception {
        final Assembler assembler = configureAssembler(null);

        // no credentials provided, the default user should be "guest"
        final Properties props = new Properties();

        final InitialContext ctx = new InitialContext(props);

        final Project foo = (Project) ctx.lookup("FooBeanLocal");

        foo.svnCheckout("");
        try {
            foo.svnCommit("");
            fail("Should not be allowed");
        } catch (final Exception e) {
            // good.
        }

        assertFalse("in role committer", foo.isCallerInRole("committer"));
        assertFalse("in role community", foo.isCallerInRole("community"));
        assertFalse("in role contributor", foo.isCallerInRole("contributor"));
        assertTrue("not in role guest", foo.isCallerInRole("guest"));
        assertTrue("Caller is not guest", foo.isCaller("guest"));

        ctx.close();
        assembler.destroy();
    }

    // Just to be sure we can override the default user (ie. guest)
    public void testDefaultUser() throws Exception {
        final Assembler assembler = configureAssembler("public");

        // no credentials provided, the default user should be "guest"
        final Properties props = new Properties();

        final InitialContext ctx = new InitialContext(props);

        final Project foo = (Project) ctx.lookup("FooBeanLocal");

        foo.svnCheckout("");
        try {
            foo.svnCommit("");
            fail("Should not be allowed");
        } catch (final Exception e) {
            // good.
        }

        assertFalse("in role committer", foo.isCallerInRole("committer"));
        assertFalse("in role community", foo.isCallerInRole("community"));
        assertFalse("in role contributor", foo.isCallerInRole("contributor"));
        assertFalse("in role guest", foo.isCallerInRole("guest"));
        assertTrue("Caller is not public", foo.isCaller("public"));

        ctx.close();
        assembler.destroy();
    }

    @Stateless
    @DeclareRoles({"committer", "contributor", "community", "guest", "public"})
    public static class FooBean implements Project {

        @Resource
        private SessionContext context;

        @Override
        @RolesAllowed({"committer"})
        public String svnCommit(final String s) {
            return s;
        }

        @Override
        @RolesAllowed({"committer", "contributor"})
        public String submitPatch(final String s) {
            return s;
        }

        @Override
        @PermitAll
        public String svnCheckout(final String s) {
            return s;
        }

        @Override
        @DenyAll
        public String deleteProject(final String s) {
            return s;
        }

        @Override
        public boolean isCallerInRole(final String role) {
            return context.isCallerInRole(role);
        }

        @Override
        public boolean isCaller(final String user) {
            return context.getCallerPrincipal().getName().equals(user);
        }
    }

    @Stateless
    @RunAs("contributor")
    @DeclareRoles({"committer", "contributor", "community"})
    public static class BarBean implements Project {

        @Resource
        private SessionContext context;

        @Override
        @RolesAllowed({"committer"})
        public String svnCommit(final String s) {
            return s;
        }

        @Override
        @RolesAllowed({"committer", "contributor"})
        public String submitPatch(final String s) {
            return s;
        }

        @Override
        @PermitAll
        public String svnCheckout(final String s) {
            return s;
        }

        @Override
        @DenyAll
        public String deleteProject(final String s) {
            return s;
        }

        @Override
        @PermitAll
        public boolean isCallerInRole(final String role) {
            return context.isCallerInRole(role);
        }

        @Override
        @PermitAll
        public boolean isCaller(final String user) {
            return context.getCallerPrincipal().getName().equals(user);
        }
    }

    public static interface Project {

        public String svnCommit(String s);

        public String submitPatch(String s);

        public String svnCheckout(String s);

        public String deleteProject(String s);

        public boolean isCaller(String s);

        public boolean isCallerInRole(String s);
    }
}

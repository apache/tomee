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

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.AfterClass;
import org.junit.Test;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;

public class JWTSecurityTest {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        // unfortunate we can't reset the value, so setting to default (See Configuration)
        Security.setProperty("login.configuration.provider", "sun.security.provider.ConfigFile");
    }

    private Assembler configureAssembler(final String defaultUser) throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        Security.setProperty("login.configuration.provider", JaasConfig.class.getName());

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

        final EjbJar ejbJar = new EjbJar("JwtTest");

        ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(BarBean.class));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        assembler.createApplication(ejbJarInfo);

        return assembler;
    }

    @Test
    public void test() throws Exception {
        final Assembler assembler = configureAssembler(null);

        final Properties props = new Properties();
        props.setProperty("openejb.authentication.realmName", "jwt-realm");
        props.setProperty(Context.SECURITY_PRINCIPAL, JWTUtil.createValidJwtAccessToken("committer", "community"));
        props.setProperty(Context.SECURITY_CREDENTIALS, "");

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
    }

    // When no credentials are provided, the default user/role should be "guest"
    @Test
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
    @Test
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

    @Singleton
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

    @Singleton
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

    public interface Project {

        String svnCommit(String s);

        String submitPatch(String s);

        String svnCheckout(String s);

        String deleteProject(String s);

        boolean isCaller(String s);

        boolean isCallerInRole(String s);
    }

    public static class JaasConfig extends Configuration {

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
            final Set<AppConfigurationEntry> entries = new HashSet<AppConfigurationEntry>();

            final Map<String, Object> options = new HashMap<String, Object>();

            entries.add(new AppConfigurationEntry(
                    JWTLoginModule.class.getName(),
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options
            ));
            return entries.toArray(new AppConfigurationEntry[entries.size()]);
        }
    }

    public static class JWTLoginModule implements LoginModule {

        private Subject subject;
        private CallbackHandler callbackHandler;
        private Map<String, ?> sharedState;
        private Map<String, ?> options;

        @Override
        public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
            this.subject = subject;
            this.callbackHandler = callbackHandler;
            this.sharedState = sharedState;
            this.options = options;
            System.out.println("JWTLoginModule.initialize");
        }

        @Override
        public boolean login() throws LoginException {
            System.out.println("JWTLoginModule.login");
            subject.getPrincipals().add(new UserPrincipal("jonathan"));
            subject.getPrincipals().add(new GroupPrincipal("committer"));
            subject.getPrincipals().add(new GroupPrincipal("community"));
            return true;
        }

        @Override
        public boolean commit() throws LoginException {
            System.out.println("JWTLoginModule.commit");
            return true;
        }

        @Override
        public boolean abort() throws LoginException {
            System.out.println("JWTLoginModule.abort");
            return true;
        }

        @Override
        public boolean logout() throws LoginException {
            System.out.println("JWTLoginModule.logout");
            return true;
        }
    }
}

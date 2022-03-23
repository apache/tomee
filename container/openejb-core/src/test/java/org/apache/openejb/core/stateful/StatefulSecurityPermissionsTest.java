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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.junit.AfterClass;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Init;
import jakarta.ejb.Local;
import jakarta.ejb.LocalHome;
import jakarta.ejb.Remote;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.Remove;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.rmi.RemoteException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @version $Rev$ $Date$
 */
public class StatefulSecurityPermissionsTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        final SecurityServiceInfo securityServiceInfo = config.configureService(SecurityServiceInfo.class);
        securityServiceInfo.className = TestSecurityService.class.getName();
        assembler.createSecurityService(securityServiceInfo);

        final TestSecurityService securityService = (TestSecurityService) SystemInstance.get().getComponent(SecurityService.class);

        securityService.login("foo", "Jazz", "Rock", "Reggae", "HipHop");

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(Color.class));
        final List<MethodPermission> permissions = ejbJar.getAssemblyDescriptor().getMethodPermission();
        permissions.add(new MethodPermission("*", "Color", "*", "Foo"));
        permissions.add(new MethodPermission("*", "Color", "create").setUnchecked());
        permissions.add(new MethodPermission("*", "Color", "ejbCreate").setUnchecked());

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        final InitialContext context = new InitialContext();

        {
            final ColorLocal color = (ColorLocal) context.lookup("ColorLocal");

            assertEquals("Jazz", color.color());
            try {
                color.color((Object) null);
            } catch (final EJBAccessException e) {
                assertEquals("Excluded", actual.get());
            }
            assertEquals("Rock", color.color((String) null));
            assertEquals("Unchecked", color.color((Boolean) null));
            assertEquals("Reggae", color.color((Integer) null));
        }

    }

    public static ThreadLocal<String> expected = new ThreadLocal<String>();

    @LocalHome(ColorEjbLocalHome.class)
    @RemoteHome(ColorEjbHome.class)
    public static class Color implements ColorLocal, ColorRemote {

        protected String attribute() {
            return actual.get();
        }

        @Init
        public void ejbCreate(final String s) {
            assertEquals(s, attribute());
        }

        @Remove
        public void ejbRemove() {
            assertEquals(expected.get(), attribute());
        }


        @RolesAllowed({"Jazz"})
        public String color() {
            return attribute();
        }

        @DenyAll
        public String color(final Object o) {
            return attribute();
        }

        @RolesAllowed({"Rock"})
        public String color(final String s) {
            return attribute();
        }

        @PermitAll
        public String color(final Boolean b) {
            return attribute();
        }

        @RolesAllowed({"Reggae"})
        public String color(final Integer i) {
            return attribute();
        }


    }

    @Local
    public static interface ColorLocal {
        public String color();

        public String color(Object o);

        public String color(String s);

        public String color(Boolean b);

        public String color(Integer i);
    }

    @Remote
    public static interface ColorRemote {
        public String color();

        public String color(Object o);

        public String color(String s);

        public String color(Boolean b);

        public String color(Integer i);
    }

    public static interface ColorEjbHome extends EJBHome {
        ColorEjbObject create(String s) throws CreateException, RemoteException;
    }

    public static interface ColorEjbObject extends EJBObject {
        public String color() throws RemoteException;

        public String color(Object o) throws RemoteException;

        public String color(String s) throws RemoteException;

        public String color(Boolean b) throws RemoteException;

        public String color(Integer i) throws RemoteException;
    }

    public static interface ColorEjbLocalHome extends EJBLocalHome {
        ColorEjbLocalObject create(String s) throws CreateException;
    }

    public static interface ColorEjbLocalObject extends EJBLocalObject {
        public String color();

        public String color(Object o);

        public String color(String s);

        public String color(Boolean b);

        public String color(Integer i);
    }


    private static final ThreadLocal<String> actual = new ThreadLocal<String>();

    public static class TestSecurityService extends AbstractSecurityService {

        public TestSecurityService() {
            super(TestJaccProvider.class.getName());
        }

        public UUID login(final String securityRealm, final String user, final String pass) throws LoginException {
            return null;
        }

        public void login(final String user, final String... roles) throws LoginException {
            final Set<Principal> set = new HashSet<>();
            set.add(new UserPrincipal(user));
            for (final String role : roles) {
                set.add(new GroupPrincipal(role));
            }
            final Subject subject = new Subject(true, set, Collections.EMPTY_SET, Collections.EMPTY_SET);
            final UUID uuid = registerSubject(subject);
            associate(uuid);
        }

        public void logout() {
            this.disassociate();
        }

        public static class TestJaccProvider extends BasicJaccProvider {
            protected BasicPolicyConfiguration createPolicyConfiguration(final String contextID) {
                return new TestPolicy(contextID);
            }

            public static class TestPolicy extends BasicPolicyConfiguration {

                TestPolicy(final String contextID) {
                    super(contextID);
                }

                public boolean implies(final ProtectionDomain domain, final Permission permission) {

                    if (excluded != null && excluded.implies(permission)) {
                        actual.set("Excluded");
                        return false;
                    }

                    if (unchecked != null && unchecked.implies(permission)) {
                        actual.set("Unchecked");
                        return true;
                    }

                    final Principal[] principals = domain.getPrincipals();
                    if (principals.length == 0) return false;

                    final RoleResolver roleResolver = SystemInstance.get().getComponent(RoleResolver.class);
                    final Set<String> roles = roleResolver.getLogicalRoles(principals, rolePermissionsMap.keySet());

                    for (final String role : roles) {
                        final PermissionCollection permissions = rolePermissionsMap.get(role);

                        if (permissions != null && permissions.implies(permission)) {
                            actual.set(role);
                            return true;
                        }
                    }

                    actual.set("Denied");
                    return false;
                }
            }
        }
    }
}

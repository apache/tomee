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
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.MethodIntf;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

import javax.naming.InitialContext;
import javax.ejb.LocalHome;
import javax.ejb.RemoteHome;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.Init;
import javax.ejb.Remove;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBAccessException;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContextException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Collections;
import java.rmi.RemoteException;
import java.security.ProtectionDomain;
import java.security.Permission;
import java.security.Principal;
import java.security.Permissions;
import java.security.PermissionCollection;

/**
 * @version $Rev$ $Date$
 */
public class StatefulSecurityPermissionsTest extends TestCase {

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        SecurityServiceInfo securityServiceInfo = config.configureService(SecurityServiceInfo.class);
        securityServiceInfo.className = TestSecurityService.class.getName();
        assembler.createSecurityService(securityServiceInfo);

        TestSecurityService securityService = (TestSecurityService) SystemInstance.get().getComponent(SecurityService.class);

        securityService.login("foo", "Jazz", "Rock", "Reggae", "HipHop");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(Color.class));
        List<MethodPermission> permissions = ejbJar.getAssemblyDescriptor().getMethodPermission();
        permissions.add(new MethodPermission("*", "Color", "*", "Foo"));
        permissions.add(new MethodPermission("*", "Color", "create").setUnchecked());
        permissions.add(new MethodPermission("*", "Color", "ejbCreate").setUnchecked());

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        InitialContext context = new InitialContext();

        {
            ColorLocal color = (ColorLocal) context.lookup("ColorLocal");

            assertEquals("Jazz", color.color());
            try {
                color.color((Object) null);
            } catch (EJBAccessException e) {
                assertEquals("Excluded", actual.get());
            }
            assertEquals("Rock", color.color((String)null));
            assertEquals("Unchecked", color.color((Boolean)null));
            assertEquals("Reggae", color.color((Integer)null));
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
        public void ejbCreate(String s){
            assertEquals(s, attribute());
        }

        @Remove
        public void ejbRemove(){
            assertEquals(expected.get(), attribute());
        }


        @RolesAllowed({"Jazz"})
        public String color() {
            return attribute();
        }

        @DenyAll
        public String color(Object o) {
            return attribute();
        }

        @RolesAllowed({"Rock"})
        public String color(String s) {
            return attribute();
        }

        @PermitAll
        public String color(Boolean b) {
            return attribute();
        }

        @RolesAllowed({"Reggae"})
        public String color(Integer i) {
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


    private static ThreadLocal<String> actual = new ThreadLocal<String>();

    public static class TestSecurityService extends AbstractSecurityService {

        public TestSecurityService() {
            super(TestJaccProvider.class.getName());
        }

        public UUID login(String securityRealm, String user, String pass) throws LoginException {
            return null;
        }

        public void login(String user, String... roles) throws LoginException {
            Set<Principal> set = new HashSet<Principal>();
            set.add(new UserPrincipal(user));
            for (String role : roles) {
                set.add(new GroupPrincipal(role));
            }
            Subject subject = new Subject(true, set, Collections.EMPTY_SET, Collections.EMPTY_SET);
            UUID uuid = registerSubject(subject);
            associate(uuid);
        }

        public void logout(){
            this.disassociate();
        }

        public static class TestJaccProvider extends BasicJaccProvider {
            protected BasicPolicyConfiguration createPolicyConfiguration(String contextID) {
                return new TestPolicy(contextID);
            }

            public static class TestPolicy extends BasicPolicyConfiguration {

                TestPolicy(String contextID) {
                    super(contextID);
                }

                public boolean implies(ProtectionDomain domain, Permission permission) {

                    if (excluded != null && excluded.implies(permission)) {
                        actual.set("Excluded");
                        return false;
                    }

                    if (unchecked != null && unchecked.implies(permission)) {
                        actual.set("Unchecked");
                        return true;
                    }

                    Principal[] principals = domain.getPrincipals();
                    if (principals.length == 0) return false;

                    RoleResolver roleResolver = SystemInstance.get().getComponent(RoleResolver.class);
                    Set<String> roles = roleResolver.getLogicalRoles(principals, rolePermissionsMap.keySet());

                    for (String role : roles) {
                        Permissions permissions = rolePermissionsMap.get(role);

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

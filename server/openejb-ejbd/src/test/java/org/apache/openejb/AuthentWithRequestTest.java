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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.client.JNDIContext;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentsResolver;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.EJBException;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthentWithRequestTest {

    @BeforeClass
    public static void initJAAS() {
        System.setProperty("java.security.auth.login.config", AuthentWithRequestTest.class.getResource("/login.config").getFile());
    }

    @AfterClass
    public static void resetJAAS() {
        System.clearProperty("java.security.auth.login.config");
    }

    @Test
    public void invoke() throws Exception {
        final EjbServer ejbServer = new EjbServer();

        OpenEJB.init(new PropertiesBuilder().p(DeploymentsResolver.DEPLOYMENTS_CLASSPATH_PROPERTY, "false").build(), new ServerFederation());
        ejbServer.init(new Properties());

        final ServiceDaemon serviceDaemon = new ServiceDaemon(ejbServer, 0, "localhost");
        serviceDaemon.start();

        final int port = serviceDaemon.getPort();

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(RemoteWithSecurity.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        try {
            { // ok case
                final Context context = new InitialContext(new PropertiesBuilder()
                        .p(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName())
                        .p(Context.PROVIDER_URL, "ejbd://127.0.0.1:" + port)
                        .p(JNDIContext.AUTHENTICATE_WITH_THE_REQUEST, "true")
                        .p("java.naming.security.principal", "foo")
                        .p("java.naming.security.credentials", "bar")
                        .p("openejb.authentication.realmName", "LM")
                        .build());
                final AnInterfaceRemote client = AnInterfaceRemote.class.cast(context.lookup("RemoteWithSecurityRemote"));
                assertNotNull(client);

                assertEquals("foo", client.call());
            }

            {// now the failing case
                final Context context = new InitialContext(new PropertiesBuilder()
                        .p(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName())
                        .p(Context.PROVIDER_URL, "ejbd://127.0.0.1:" + port)
                        .p(JNDIContext.AUTHENTICATE_WITH_THE_REQUEST, "true")
                        .p("java.naming.security.principal", "wrong")
                        .p("java.naming.security.credentials", "wrong")
                        .p("openejb.authentication.realmName", "LM")
                        .build());
                final AnInterfaceRemote client = AnInterfaceRemote.class.cast(context.lookup("RemoteWithSecurityRemote"));
                try {
                    client.call();
                } catch (final EJBException e) {
                    if (!LoginException.class.isInstance(e.getCause())) {
                        e.printStackTrace();
                    }
                    assertTrue(LoginException.class.isInstance(e.getCause()));
                }
            }
        } finally {
            serviceDaemon.stop();
            OpenEJB.destroy();
        }
    }

    @Remote
    public static interface AnInterfaceRemote {

        String call();
    }

    @Stateless
    public static class RemoteWithSecurity implements AnInterfaceRemote {

        private static ThreadLocal<String> name = new ThreadLocal<String>();

        @Override
        public String call() {
            return name.get();
        }
    }

    public static class MyLoginModule implements LoginModule {

        private CallbackHandler callbackHandler;

        @Override
        public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
            this.callbackHandler = callbackHandler;
        }

        @Override
        public boolean login() throws LoginException {
            assertNull(SystemInstance.get().getComponent(SecurityService.class).currentState()); // check the user was not logged at lookup()

            final NameCallback nameCallback = new NameCallback("name?", "dummy");
            try {
                callbackHandler.handle(new Callback[]{nameCallback});
            } catch (final Exception e) {
                throw new LoginException(e.getMessage());
            }
            if (!"foo".equals(nameCallback.getName())) {
                throw new IllegalArgumentException("Not an Error/assert cause in java 9 jaas doesnt capture it anymore");
            }
            RemoteWithSecurity.name.set(nameCallback.getName());
            return true;
        }

        @Override
        public boolean commit() throws LoginException {
            return true;
        }

        @Override
        public boolean abort() throws LoginException {
            return true;
        }

        @Override
        public boolean logout() throws LoginException {
            return true;
        }
    }
}

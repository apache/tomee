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

import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jaas.UserPrincipal;
import org.apache.openejb.core.security.jaas.UsernamePasswordCallbackHandler;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

import static org.apache.openejb.util.URLs.toFilePath;
import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class CDILoginModuleTest {
    @BeforeClass
    public static void loadJassLoginConfig() {
        final URL resource = CDILoginModuleTest.class.getClassLoader().getResource("cdi-login.config");
        System.setProperty("java.security.auth.login.config", toFilePath(resource));
    }

    @AfterClass
    public static void reset() {
        System.clearProperty("java.security.auth.login.config");
    }

    @Module
    public Beans beans() {
        final Beans beans = new Beans();
        beans.addManagedClass(Delegate.class);
        beans.addManagedClass(CDIBean.class);
        return beans;
    }

    @Test
    public void testLogin() throws LoginException {
        final LoginContext context = new LoginContext("CDI", new UsernamePasswordCallbackHandler("foo", ""));
        context.login();

        final Subject subject = context.getSubject();

        assertEquals(1, subject.getPrincipals().size());
        assertEquals("foo", subject.getPrincipals(AbstractSecurityService.User.class).iterator().next().getName());

        context.logout();

        assertEquals(0, subject.getPrincipals().size());
    }

    @Test(expected = LoginException.class)
    public void testBadUseridLogin() throws Exception {
        new LoginContext("CDI", new UsernamePasswordCallbackHandler("bar", "secret")).login();
    }

    public static class CDIBean {
        public boolean ok(final String name) {
            return "foo".equals(name);
        }
    }

    public static class Delegate implements LoginModule {
        @Inject
        private CDIBean bean;

        private String name;
        private Subject subject;

        @Override
        public void initialize(final Subject subject, final CallbackHandler callbackHandler,
                               final Map<String, ?> sharedState, final Map<String, ?> options) {
            final NameCallback nameCallback = new NameCallback("whatever");
            try {
                callbackHandler.handle(new Callback[]{nameCallback});
            } catch (final Exception e) {
                // no-op
            }
            name = nameCallback.getName();
            this.subject = subject;
        }

        @Override
        public boolean login() throws LoginException {
            return bean.ok(name);
        }

        @Override
        public boolean commit() throws LoginException {
            subject.getPrincipals().add(new AbstractSecurityService.User(name));
            return true;
        }

        @Override
        public boolean abort() throws LoginException {
            return true;
        }

        @Override
        public boolean logout() throws LoginException {
            final Iterator<Principal> iterator = subject.getPrincipals().iterator();
            iterator.next();
            iterator.remove();
            return true;
        }
    }
}

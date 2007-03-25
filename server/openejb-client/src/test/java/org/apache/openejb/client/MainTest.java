/**
 *
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
package org.apache.openejb.client;

import junit.framework.TestCase;

import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;
import javax.naming.NameParser;
import javax.naming.NameNotFoundException;
import javax.security.auth.Subject;
import javax.security.auth.login.FailedLoginException;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.security.AccessController;

public class MainTest extends TestCase {
    static {
        try {
            NamingManager.setInitialContextFactoryBuilder(new MockContextFactoryBuilder());
        } catch (NamingException e) {
        }
    }

    public static Map<String,Object> jndi = new TreeMap<String,Object>();

    protected void setUp() throws Exception {
        super.setUp();
        LoginTestUtil.initialize();

        jndi.clear();
        jndi.put("java:comp/path", "fake.jar");
        jndi.put("java:comp/injections", new InjectionMetaData());
    }

    public void testSecureMain() throws Exception {
        jndi.put("java:comp/callbackHandler", StaticUsernamePasswordCallbackHandler.class.getName());

        StaticUsernamePasswordCallbackHandler.setUsername("victoria");
        StaticUsernamePasswordCallbackHandler.setPassword("secret");
        LoginTestUtil.setAuthGranted();
        
        jndi.put("java:comp/mainClass", SecureMain.class.getName());
        Main.main(new String[0]);
    }

    public void testSecureMainFailed() throws Exception {
        jndi.put("java:comp/callbackHandler", StaticUsernamePasswordCallbackHandler.class.getName());

        StaticUsernamePasswordCallbackHandler.setUsername("victoria");
        StaticUsernamePasswordCallbackHandler.setPassword("secret");
        LoginTestUtil.setAuthDenied();

        jndi.put("java:comp/mainClass", SecureMain.class.getName());
        try {
            Main.main(new String[0]);
            fail("Expected main method to throw FailedLoginException");
        } catch (FailedLoginException expected) {
        }
    }

    public static class SecureMain {
        public static void main(String[] args) {
            Subject subject = Subject.getSubject(AccessController.getContext());

            // verify subject
            assertEquals("Should have one principal", 1, subject.getPrincipals().size());
            assertEquals("Should have one user principal", 1, subject.getPrincipals(ClientIdentityPrincipal.class).size());
            ClientIdentityPrincipal principal = subject.getPrincipals(ClientIdentityPrincipal.class).iterator().next();
            assertEquals("victoria", principal.getName());
            assertEquals("SecretIdentity", principal.getClientIdentity());
        }
    }

    public void testNormalMain() throws Exception {
        jndi.put("java:comp/mainClass", NormalMain.class.getName());
        Main.main(new String[0]);
    }

    public static class NormalMain {
        public static void main(String[] args) {
            Subject subject = Subject.getSubject(AccessController.getContext());

            assertNull("subject is not null", subject);
        }
    }


    //
    // Ignore these
    //
    public static class MockContextFactoryBuilder implements InitialContextFactoryBuilder {
        public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
            return new MockContextFactory();
        }
    }

    public static class MockContextFactory implements InitialContextFactory {
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return new MockContext();
        }
    }

    public static class MockContext implements Context {
        public Object lookup(String name) throws NamingException {
            Object value = jndi.get(name);
            if (value == null) {
                throw new NameNotFoundException(name);
            }
            return value;
        }

        public Object lookup(Name name) throws NamingException {
            return null;
        }

        public void bind(Name name, Object obj) throws NamingException {

        }

        public void bind(String name, Object obj) throws NamingException {

        }

        public void rebind(Name name, Object obj) throws NamingException {

        }

        public void rebind(String name, Object obj) throws NamingException {

        }

        public void unbind(Name name) throws NamingException {

        }

        public void unbind(String name) throws NamingException {

        }

        public void rename(Name oldName, Name newName) throws NamingException {

        }

        public void rename(String oldName, String newName) throws NamingException {

        }

        public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
            return null;
        }

        public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
            return null;
        }

        public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
            return null;
        }

        public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
            return null;
        }

        public void destroySubcontext(Name name) throws NamingException {

        }

        public void destroySubcontext(String name) throws NamingException {

        }

        public Context createSubcontext(Name name) throws NamingException {
            return null;
        }

        public Context createSubcontext(String name) throws NamingException {
            return null;
        }

        public Object lookupLink(Name name) throws NamingException {
            return null;
        }

        public Object lookupLink(String name) throws NamingException {
            return null;
        }

        public NameParser getNameParser(Name name) throws NamingException {
            return null;
        }

        public NameParser getNameParser(String name) throws NamingException {
            return null;
        }

        public Name composeName(Name name, Name prefix) throws NamingException {
            return null;
        }

        public String composeName(String name, String prefix) throws NamingException {
            return null;
        }

        public Object addToEnvironment(String propName, Object propVal) throws NamingException {
            return null;
        }

        public Object removeFromEnvironment(String propName) throws NamingException {
            return null;
        }

        public Hashtable<?, ?> getEnvironment() throws NamingException {
            return null;
        }

        public void close() throws NamingException {

        }

        public String getNameInNamespace() throws NamingException {
            return null;
        }
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.EJBAccessException;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ContractTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(Contract.class, ContractImpl.class);
    }

    @ArquillianResource
    private URL base;

    @Test
    public void valid() throws NamingException {
        assertEquals("hi", hi(new Properties() {{
            setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            setProperty(Context.PROVIDER_URL, String.format("http://localhost:%s/tomee/ejb", base.getPort()));
            setProperty(Context.SECURITY_PRINCIPAL, "tomcat");
            setProperty(Context.SECURITY_CREDENTIALS, "users");
        }}));
    }

    @Test
    public void invalid() throws NamingException {
        try {
            hi(new Properties() {{
                setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
                setProperty(Context.PROVIDER_URL, String.format("http://localhost:%s/tomee/ejb", base.getPort()));
                setProperty(Context.SECURITY_PRINCIPAL, "tomcat");
                setProperty(Context.SECURITY_CREDENTIALS, "wrong");
            }});
            fail();
        } catch (final AuthenticationException ae) {
            // ok
        }
    }

    @Test
    public void missingCredentials() throws NamingException {
        try {
            hi(new Properties() {{
                setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
                setProperty(Context.PROVIDER_URL, String.format("http://localhost:%s/tomee/ejb", base.getPort()));
            }});
            fail();
        } catch (final EJBAccessException eae) {
            // no-op
        }
    }

    private String hi(final Properties clientConfig) throws NamingException {
        return Contract.class.cast(new InitialContext(clientConfig).lookup("java:global/test/ContractImpl!org.superbiz.Contract")).hi();
    }
}

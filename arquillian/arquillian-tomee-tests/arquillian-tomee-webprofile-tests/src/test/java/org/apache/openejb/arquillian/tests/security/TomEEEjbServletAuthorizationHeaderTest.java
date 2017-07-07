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
package org.apache.openejb.arquillian.tests.security;

import org.apache.openejb.arquillian.tests.TestRun;
import org.apache.openejb.arquillian.tests.TestSetup;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.server.httpd.ServerServlet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJBAccessException;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;


@RunWith(Arquillian.class)
@RunAsClient
public class TomEEEjbServletAuthorizationHeaderTest extends TestSetup  {
    private static final String REMOTE_NAME = "global/TomEEEjbServletAuthorizationHeaderTest/BusinessBean!" +
                                              "org.apache.openejb.arquillian.tests.security.BusinessRemote";

    @ArquillianResource
    private URL url;

    @Test
    public void testAuthenticate() throws Exception {
        final String ejbUrl = this.url.toExternalForm() + "ejb";

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, ejbUrl);
        p.setProperty("tomee.ejb.authentication.basic.login", "tomee");
        p.setProperty("tomee.ejb.authentication.basic.password", "password");
        final InitialContext context = new InitialContext(p);

        final BusinessRemote bean = (BusinessRemote) context.lookup(REMOTE_NAME);
        assertEquals("test", bean.echo("test"));
        assertEquals("tomee", bean.getPrincipal());
    }

    @Test(expected = AuthenticationException.class)
    public void testFailedAuthentication() throws Exception {
        final String ejbUrl = this.url.toExternalForm() + "ejb";

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, ejbUrl);
        p.setProperty("tomee.ejb.authentication.basic.login", "tomee");
        p.setProperty("tomee.ejb.authentication.basic.password", "wrong");
        final InitialContext context = new InitialContext(p);

        context.lookup(REMOTE_NAME);
    }

    @Test
    public void testAuthenticateWithPrincipal() throws Exception {
        final String ejbUrl = this.url.toExternalForm() + "ejb";

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, ejbUrl);
        p.setProperty("tomee.ejb.authentication.basic.login", "tomee");
        p.setProperty("tomee.ejb.authentication.basic.password", "password");
        p.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        p.setProperty(Context.SECURITY_CREDENTIALS, "admin");
        final InitialContext context = new InitialContext(p);

        final BusinessRemote bean = (BusinessRemote) context.lookup(REMOTE_NAME);
        assertEquals("test", bean.echo("test"));
        assertEquals("admin", bean.getPrincipal());
    }

    @Test(expected = AuthenticationException.class)
    public void testFailedPrincipalAuthentication() throws Exception {
        final String ejbUrl = this.url.toExternalForm() + "ejb";

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, ejbUrl);
        p.setProperty("tomee.ejb.authentication.basic.login", "tomee");
        p.setProperty("tomee.ejb.authentication.basic.password", "password");
        p.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        p.setProperty(Context.SECURITY_CREDENTIALS, "wrong");
        final InitialContext context = new InitialContext(p);

        context.lookup(REMOTE_NAME);
    }

    @Test(expected = EJBAccessException.class)
    public void testAuthenticateWithPrincipalForbiddenCall() throws Exception {
        final String ejbUrl = this.url.toExternalForm() + "ejb";

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, ejbUrl);
        p.setProperty("tomee.ejb.authentication.basic.login", "tomee");
        p.setProperty("tomee.ejb.authentication.basic.password", "password");
        p.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        p.setProperty(Context.SECURITY_CREDENTIALS, "admin");
        final InitialContext context = new InitialContext(p);

        final BusinessRemote bean = (BusinessRemote) context.lookup(REMOTE_NAME);
        bean.forbidden();
    }

    @Deployment(testable = false)
    public static WebArchive getArchive() {
        return new TomEEEjbServletAuthorizationHeaderTest().createDeployment(TestRun.class, BusinessBean.class, BusinessRemote.class);
    }

    @Override
    protected void decorateDescriptor(WebAppDescriptor descriptor) {
        descriptor
            .createServlet()
                .servletName("ServerServlet")
                .servletClass(ServerServlet.class.getName()).up()
            .createServletMapping()
                .servletName("ServerServlet")
                .urlPattern("/ejb").up()
            .createSecurityConstraint()
                .getOrCreateWebResourceCollection()
                    .webResourceName("all")
                    .urlPattern("/*").up()
                .getOrCreateAuthConstraint()
                    .roleName("tomee-admin")
                    .up().up()
            .createLoginConfig()
                .authMethod("BASIC");
    }

}




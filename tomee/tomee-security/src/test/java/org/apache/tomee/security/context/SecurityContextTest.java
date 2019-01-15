/*
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
package org.apache.tomee.security.context;

import org.apache.tomee.security.AbstractTomEESecurityTest;
import org.junit.Test;

import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;

import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static org.junit.Assert.assertEquals;

public class SecurityContextTest extends AbstractTomEESecurityTest {
    @Test
    public void authenticate() throws Exception {
        final String servlet = getAppUrl() + "/securityContext";
        final Response response = ClientBuilder.newBuilder()
                                               .build()
                                               .target(servlet)
                                               .queryParam("username", "tomcat")
                                               .queryParam("password", "tomcat")
                                               .request()
                                               .get();
        assertEquals(200, response.getStatus());
        assertEquals("ok!", response.readEntity(String.class));
    }

    @Test
    public void callerPrincipal() throws Exception {
        final String servlet = getAppUrl() + "/securityContextPrincipal";
        final Response response = ClientBuilder.newBuilder()
                                               .build()
                                               .target(servlet)
                                               .queryParam("username", "tomcat")
                                               .queryParam("password", "tomcat")
                                               .request()
                                               .get();
        assertEquals(200, response.getStatus());
        assertEquals("tomcat", response.readEntity(String.class));
    }

    @Test
    public void callerInRole() throws Exception {
        final String servlet = getAppUrl() + "/securityContextRole";
        final Response response = ClientBuilder.newBuilder()
                                               .build()
                                               .target(servlet)
                                               .queryParam("username", "tomcat")
                                               .queryParam("password", "tomcat")
                                               .queryParam("role", "tomcat")
                                               .request()
                                               .get();
        assertEquals(200, response.getStatus());
        assertEquals("ok", response.readEntity(String.class));
    }

    @Test
    public void wrongPassword() throws Exception {
        final String servlet = getAppUrl() + "/securityContext";
        assertEquals(401, ClientBuilder.newBuilder().build()
                                       .target(servlet)
                                       .queryParam("username", "tomcat")
                                       .queryParam("password", "wrong")
                                       .request()
                                       .get().getStatus());
    }

    @WebServlet(urlPatterns = "/securityContext")
    public static class TestServlet extends HttpServlet {
        @Inject
        private SecurityContext securityContext;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

            final AuthenticationParameters parameters =
                    AuthenticationParameters.withParams()
                                            .credential(new UsernamePasswordCredential(req.getParameter("username"),
                                                                                       req.getParameter("password")))
                                            .newAuthentication(true);

            securityContext.authenticate(req, resp, parameters);

            resp.getWriter().write("ok!");
        }
    }

    @WebServlet(urlPatterns = "/securityContextPrincipal")
    public static class PrincipalServlet extends HttpServlet {
        @Inject
        private SecurityContext securityContext;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

            final AuthenticationParameters parameters =
                    AuthenticationParameters.withParams()
                                            .credential(new UsernamePasswordCredential(req.getParameter("username"),
                                                                                       req.getParameter("password")))
                                            .newAuthentication(true);

            securityContext.authenticate(req, resp, parameters);

            final Principal callerPrincipal = securityContext.getCallerPrincipal();

            resp.getWriter().write(callerPrincipal.getName());
        }
    }

    @WebServlet(urlPatterns = "/securityContextRole")
    public static class RoleServlet extends HttpServlet {
        @Inject
        private SecurityContext securityContext;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

            final AuthenticationParameters parameters =
                    AuthenticationParameters.withParams()
                                            .credential(new UsernamePasswordCredential(req.getParameter("username"),
                                                                                       req.getParameter("password")))
                                            .newAuthentication(true);

            securityContext.authenticate(req, resp, parameters);

            resp.getWriter().write(securityContext.isCallerInRole(req.getParameter("role")) ? "ok" : "nok");
        }
    }

    public static class SecurityContextHttpAuthenticationMechanism implements HttpAuthenticationMechanism {
        @Inject
        private IdentityStoreHandler identityStoreHandler;

        @Override
        public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final HttpMessageContext httpMessageContext)
                throws AuthenticationException {

            if (httpMessageContext.isAuthenticationRequest()) {
                try {
                    final CredentialValidationResult result =
                            identityStoreHandler.validate(httpMessageContext.getAuthParameters().getCredential());

                    if (result.getStatus().equals(VALID)) {
                        return httpMessageContext.notifyContainerAboutLogin(result);
                    }

                } catch (final IllegalArgumentException | IllegalStateException e) {
                    // Something was sent in the header was not valid.
                }

                return httpMessageContext.responseUnauthorized();
            }

            return httpMessageContext.doNothing();
        }
    }
}

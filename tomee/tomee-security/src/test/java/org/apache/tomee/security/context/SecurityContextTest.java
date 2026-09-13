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
import org.apache.tomee.security.cdi.TomcatUserIdentityStoreDefinition;
import org.junit.Test;

import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;

import static jakarta.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void authenticateReturnsSuccessStatus() throws Exception {
        final Response response = ClientBuilder.newBuilder().build()
                                               .target(getAppUrl() + "/securityContextStatus")
                                               .queryParam("username", "tomcat")
                                               .queryParam("password", "tomcat")
                                               .request()
                                               .get();
        assertEquals("SUCCESS", response.readEntity(String.class));
    }

    @Test
    public void authenticateReturnsSendFailureWhenMechanismThrows() throws Exception {
        final Response response = ClientBuilder.newBuilder().build()
                                               .target(getAppUrl() + "/securityContextStatus")
                                               .queryParam("username", "throws")
                                               .queryParam("password", "whatever")
                                               .request()
                                               .get();
        assertEquals("SEND_FAILURE", response.readEntity(String.class));
    }

    @Test
    public void authenticatePersistsAcrossRequests() throws Exception {
        final Client client = ClientBuilder.newBuilder().build();

        final Response login = client.target(getAppUrl() + "/securityContextPrincipal")
                                     .queryParam("username", "tomcat")
                                     .queryParam("password", "tomcat")
                                     .request()
                                     .get();
        assertEquals(200, login.getStatus());
        assertEquals("tomcat", login.readEntity(String.class));

        assertNotNull("expected a session to be created by @AutoApplySession", login.getCookies().get("JSESSIONID"));
        final String sessionId = login.getCookies().get("JSESSIONID").getValue();

        final Response whoami = client.target(getAppUrl() + "/securityContextWhoAmI")
                                      .request()
                                      .cookie("JSESSIONID", sessionId)
                                      .get();
        assertEquals(200, whoami.getStatus());
        assertEquals("tomcat", whoami.readEntity(String.class));
    }

    @TomcatUserIdentityStoreDefinition
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

    @TomcatUserIdentityStoreDefinition
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

    @TomcatUserIdentityStoreDefinition
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

    @TomcatUserIdentityStoreDefinition
    @WebServlet(urlPatterns = "/securityContextStatus")
    public static class StatusServlet extends HttpServlet {
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

            final AuthenticationStatus status = securityContext.authenticate(req, resp, parameters);
            resp.getWriter().write(status.name());
        }
    }

    @WebServlet(urlPatterns = "/securityContextWhoAmI")
    public static class WhoAmIServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {

            final Principal principal = req.getUserPrincipal();
            resp.getWriter().write(principal == null ? "null" : principal.getName());
        }
    }

    @AutoApplySession
    public static class SecurityContextHttpAuthenticationMechanism implements HttpAuthenticationMechanism {
        @Inject
        private IdentityStoreHandler identityStoreHandler;

        @Override
        public AuthenticationStatus validateRequest(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final HttpMessageContext httpMessageContext)
                throws AuthenticationException {

            if (httpMessageContext.isAuthenticationRequest()) {
                final Credential credential = httpMessageContext.getAuthParameters().getCredential();

                // Sentinel used by the tests to exercise the "mechanism throws" path: authenticate()
                // must recover SEND_FAILURE for this even though request.authenticate() only sees a boolean.
                if (credential instanceof UsernamePasswordCredential
                        && "throws".equals(((UsernamePasswordCredential) credential).getCaller())) {
                    throw new AuthenticationException("simulated mechanism failure");
                }

                try {
                    final CredentialValidationResult result = identityStoreHandler.validate(credential);

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

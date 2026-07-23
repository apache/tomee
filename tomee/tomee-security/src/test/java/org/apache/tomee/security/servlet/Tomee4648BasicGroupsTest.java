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
package org.apache.tomee.security.servlet;

import org.apache.tomee.security.AbstractTomEESecurityTest;
import org.apache.tomee.security.client.BasicAuthFilter;
import org.junit.Test;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Mirrors the Jakarta Security TCK app-mem-basic module: an application supplied
 * {@link IdentityStore} that only declares the {@code validate(UsernamePasswordCredential)}
 * overload and returns the caller groups from that validation.
 */
public class Tomee4648BasicGroupsTest extends AbstractTomEESecurityTest {

    @Test
    public void authenticatedCallerKeepsGroups() {
        final Response response = ClientBuilder.newBuilder()
                                               .register(new BasicAuthFilter("reza", "secret1"))
                                               .build()
                                               .target(getAppUrl() + "/tomee4648")
                                               .request()
                                               .get();

        assertEquals(200, response.getStatus());

        final String body = response.readEntity(String.class);
        assertTrue(body, body.contains("web username: reza"));
        assertTrue(body, body.contains("web user has role \"foo\": true"));
        assertTrue(body, body.contains("web user has role \"bar\": true"));
    }

    @ApplicationScoped
    public static class TestIdentityStore implements IdentityStore {
        public CredentialValidationResult validate(final UsernamePasswordCredential credential) {
            if (credential.compareTo("reza", "secret1")) {
                return new CredentialValidationResult("reza", new HashSet<>(asList("foo", "bar")));
            }
            return INVALID_RESULT;
        }
    }

    @Qualifier
    @Retention(RUNTIME)
    @Target({FIELD, METHOD, TYPE, PARAMETER})
    public @interface Tomee4648Mechanism {
        final class Literal extends AnnotationLiteral<Tomee4648Mechanism> implements Tomee4648Mechanism {
            private static final long serialVersionUID = 1L;
            public static final Literal INSTANCE = new Literal();
        }
    }

    @WebServlet(urlPatterns = "/tomee4648")
    @DeclareRoles({"foo", "bar", "kaz"})
    @ServletSecurity(@HttpConstraint(rolesAllowed = "foo"))
    @BasicAuthenticationMechanismDefinition(
            realmName = "test realm",
            qualifiers = Tomee4648Mechanism.class)
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().write("This is a servlet \n");
            resp.getWriter().write("web username: "
                    + (req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName()) + "\n");
            resp.getWriter().write("web user has role \"foo\": " + req.isUserInRole("foo") + "\n");
            resp.getWriter().write("web user has role \"bar\": " + req.isUserInRole("bar") + "\n");
            resp.getWriter().write("web user has role \"kaz\": " + req.isUserInRole("kaz") + "\n");
        }
    }
}

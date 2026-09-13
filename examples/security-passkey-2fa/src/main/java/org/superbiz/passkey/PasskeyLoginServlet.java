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
package org.superbiz.passkey;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.Principal;

@WebServlet("/api/login/*")
public class PasskeyLoginServlet extends HttpServlet {

    static final String FIRST_FACTOR_USER = "passkey.firstFactor.user";

    @Inject
    private UserRepository users;

    @Inject
    private WebAuthnService webAuthn;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        if ("/assertion-options".equals(request.getPathInfo())) {
            final String username = firstFactorUser(request);
            if (username == null) {
                Http.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Complete the password step first");
                return;
            }
            Http.json(response, webAuthn.assertionOptions(request, username));
            return;
        }

        Http.error(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String path = request.getPathInfo();
        if ("/password".equals(path)) {
            handlePassword(request, response);
        } else if ("/assertion".equals(path)) {
            reportAssertionResult(request, response);
        } else {
            Http.error(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
        }
    }

    private void handlePassword(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {

        final var body = WebAuthnService.parse(Http.body(request));
        final String username = body.getString("username", null);
        final String password = body.getString("password", null);

        if (username == null || password == null || !users.validatePassword(username, password)) {
            Http.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Bad username or password");
            return;
        }

        request.getSession(true).setAttribute(FIRST_FACTOR_USER, username);
        Http.json(response, "{\"firstFactor\":true}");
    }

    private void reportAssertionResult(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {

        final Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Http.json(response, "{\"authenticated\":true,\"redirect\":\"" + request.getContextPath() + "/app\"}");
        } else {
            Http.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }
    }

    private static String firstFactorUser(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(FIRST_FACTOR_USER);
    }
}

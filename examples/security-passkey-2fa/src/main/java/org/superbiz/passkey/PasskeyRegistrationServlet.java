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

@WebServlet("/api/register/*")
public class PasskeyRegistrationServlet extends HttpServlet {

    @Inject
    private WebAuthnService webAuthn;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        if ("/options".equals(request.getPathInfo())) {
            final String username = currentUser(request);
            if (username == null) {
                Http.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Log in with your password first");
                return;
            }
            Http.json(response, webAuthn.registrationOptions(request, username));
            return;
        }

        Http.error(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String username = currentUser(request);
        if (username == null) {
            Http.error(response, HttpServletResponse.SC_UNAUTHORIZED, "Log in with your password first");
            return;
        }

        try {
            webAuthn.finishRegistration(request, username, Http.body(request));
        } catch (final RuntimeException e) {
            Http.error(response, HttpServletResponse.SC_BAD_REQUEST, "Registration failed");
            return;
        }

        Http.json(response, "{\"registered\":true}");
    }

    private static String currentUser(final HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        final HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(PasskeyLoginServlet.FIRST_FACTOR_USER);
    }
}

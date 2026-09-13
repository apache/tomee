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

import jakarta.annotation.security.DeclareRoles;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;

@WebServlet("/app")
@DeclareRoles({"user", "admin"})
@ServletSecurity(@HttpConstraint(rolesAllowed = "user"))
public class ProtectedServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final Principal principal = request.getUserPrincipal();
        final String name = principal == null ? null : principal.getName();

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(
                "<html><body>"
                        + "<h1>Protected area</h1>"
                        + "<p>You reached this page on a follow-up request, so the passkey login "
                        + "was persisted across requests.</p>"
                        + "<p>caller: <b>" + name + "</b></p>"
                        + "<p>role \"user\":  " + request.isUserInRole("user") + "</p>"
                        + "<p>role \"admin\": " + request.isUserInRole("admin") + "</p>"
                        + "<p><a href=\"" + request.getContextPath() + "/\">home</a></p>"
                        + "</body></html>");
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/movies")
@DeclareRoles({"foo","bar","kaz"})
@ServletSecurity(@HttpConstraint(rolesAllowed = "foo"))
@BasicAuthenticationMechanismDefinition
public class MovieServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        String webName = null;
        if (request.getUserPrincipal() != null) {
            webName = request.getUserPrincipal().getName();
        }

        response.getWriter().write(
            "<html><body> Welcome to Movie servlet <br><br>\n" +

            "web username: " + webName + "<br><br>\n" +

            "web user has role \"foo\": " + request.isUserInRole("foo") + "<br>\n" +
            "web user has role \"bar\": " + request.isUserInRole("bar") + "<br>\n" +
            "web user has role \"kaz\": " + request.isUserInRole("kaz") + "<br><br>\n" +


            "<form method=\"POST\">" +
            "<input type=\"hidden\" name=\"logout\" value=\"true\"  >" +
            "<input type=\"submit\" value=\"Logout\">" +
            "</form>" +
            "</body></html>");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("true".equals(request.getParameter("logout"))) {
            request.logout();
            request.getSession().invalidate();
        }

        doGet(request, response);
    }

}
package org.superbiz.openid;

import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@OpenIdAuthenticationMechanismDefinition(
        providerURI = "#{openIdConfig.providerUri}",
        clientId = "#{openIdConfig.clientId}",
        clientSecret = "#{openIdConfig.clientSecret}",
        redirectURI = "#{baseURL}/secured")
@ServletSecurity(@HttpConstraint(rolesAllowed = "user"))
@WebServlet(name = "Secured Servlet", urlPatterns = "/secured")
public class SecuredServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.getWriter().print("Hello, " + req.getUserPrincipal().getName());

        if (req.isUserInRole("admin")) {
            resp.getWriter().print("\nYou're an admin!");
        }
    }
}

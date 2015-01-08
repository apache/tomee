package org.apache.openejb.arquillian.tests.realm;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/test")
public class MyService extends HttpServlet {
    @Inject
    private MultiAuthenticator authenticator;

    @Inject
    private MyAwesomeEjb ejb;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // invoke the ejb to make sure security is applied
        try {
            ejb.hello();
        } catch (Exception e) {
            resp.sendError(403);
        }

        final String result = authenticator.isStacked() ? "ok" : "ko";
        resp.setContentType("text/plain");
        resp.getWriter().write(result);
    }

    @Singleton
    public static class MyAwesomeEjb {
        @RolesAllowed("admin")
        public String hello() {
            return "hello";
        }
    }
}

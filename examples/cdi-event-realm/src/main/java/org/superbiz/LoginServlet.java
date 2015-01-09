package org.superbiz;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String username = req.getParameter("username");
        final String password = req.getParameter("password");

        try {
            // create a session
            req.getSession(true);

            // login
            req.login(username, password);

        } catch (final ServletException se) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }

}

package org.apache.openejb.arquillian.tests.listenerremote;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ServletToCheckListener extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ServletContext ctxt = req.getServletContext();
        for (ContextAttributeName s : ContextAttributeName.values()) {
            resp.getOutputStream().println("Context: " + ctxt.getAttribute(s.name()));
        }

        final HttpSession session = req.getSession();
        for (ContextAttributeName s : ContextAttributeName.values()) {
            resp.getOutputStream().println("Session: " + session.getAttribute(s.name()));
        }
    }
}
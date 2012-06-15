package org.superbiz.cdi.session;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "input-servlet", urlPatterns = { "/set-name" })
public class InputServlet extends HttpServlet {
    @Inject
    private SessionBean bean;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            resp.getWriter().write("please add a parameter name=xxx");
        } else {
            bean.setName(name);
            resp.getWriter().write("done, go to /name servlet");
        }

    }
}

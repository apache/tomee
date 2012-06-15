package org.superbiz.cdi.session;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "output-servlet", urlPatterns = { "/name" })
public class OutputServlet extends HttpServlet {
    @Inject
    private AnswerBean bean;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String name = bean.value();
        if (name == null || name.isEmpty()) {
            resp.getWriter().write("please go to servlet /set-name please");
        } else {
            resp.getWriter().write("name = " + name);
        }
    }
}

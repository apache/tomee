package org.superbiz.cdi.ejbcontext;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/ejbcontext")
public class CdiServlet extends HttpServlet {
    @Inject
    private CdiBean bean;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.login("tomee", "tomee");
        resp.getWriter().write(bean.info());
    }
}

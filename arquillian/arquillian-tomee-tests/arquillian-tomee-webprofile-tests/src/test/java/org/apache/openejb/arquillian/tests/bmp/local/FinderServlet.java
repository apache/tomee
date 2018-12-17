package org.apache.openejb.arquillian.tests.bmp.local;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FinderServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final FinderTestHome testHome;
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            testHome = (FinderTestHome) ctx.lookup("java:comp/env/ejb/FinderTest");
            resp.getWriter().println(testHome.create().runTest());
            resp.getWriter().flush();
        } catch (final Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (final Exception e) {
                throw new ServletException(e);
            }
        }
    }
}

package org.apache.openejb.arquillian.tests.classloader;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/hash")
public class HashServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final BeanManager bm = HashCdiExtension.BMS.get(tccl);
        // todo: do some lookup
        resp.getWriter().write(Boolean.toString(bm != null));
    }
}

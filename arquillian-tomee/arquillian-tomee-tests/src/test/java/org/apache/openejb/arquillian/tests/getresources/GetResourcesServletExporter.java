package org.apache.openejb.arquillian.tests.getresources;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static junit.framework.Assert.assertTrue;

/**
 * @author rmannibucau
 */
@WebServlet(name = "get-resources", urlPatterns = "/get-resources")
public class GetResourcesServletExporter extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        final PrintWriter writer = resp.getWriter();
        writer.write("foundFromListener=" + GetResourcesHolder.RESOURCE_NUMBER);

        try {
            // all this tests will throw an exception if it fails
            getServletContext().getResource("/config/test.getresources").openStream().close();
            getServletContext().getResourceAsStream("/config/test.getresources").close();
            getServletContext().getResourcePaths("/config/").iterator().next();
            assertTrue(new File(getServletContext().getRealPath("/config/test.getresources")).exists());

            writer.write("servletContextGetResource=ok");
        } catch (Exception e) {
            writer.write("servletContextGetResource=ko");
        }
    }
}

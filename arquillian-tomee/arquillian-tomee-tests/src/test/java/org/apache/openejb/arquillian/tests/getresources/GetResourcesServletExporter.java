package org.apache.openejb.arquillian.tests.getresources;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author rmannibucau
 */
@WebServlet(name = "get-resources", urlPatterns = "/get-resources")
public class GetResourcesServletExporter extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        final PrintWriter writer = resp.getWriter();
        writer.write("found=" + GetResourcesHolder.RESOURCE_NUMBER);
    }
}

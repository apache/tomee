package org.apache.openejb.arquillian.tests.cmp;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@WebServlet(name="Cmp", urlPatterns = "/*")
public class CmpServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final Collection<AppInfo> deployedApplications = assembler.getDeployedApplications();

        for (final AppInfo deployedApplication : deployedApplications) {
            if ("CmpMappingTest".equals(deployedApplication.appId)) {
                final String cmpMappingsXml = deployedApplication.cmpMappingsXml;
                resp.getWriter().write(cmpMappingsXml == null ? "null" : cmpMappingsXml);
            }
        }
    }
}

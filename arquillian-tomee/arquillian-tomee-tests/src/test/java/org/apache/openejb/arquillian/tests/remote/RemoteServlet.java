package org.apache.openejb.arquillian.tests.remote;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class RemoteServlet extends HttpServlet {

    @EJB
    private CompanyRemote remoteCompany;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        if (StringUtils.isEmpty(name)) {
            name = "OpenEJB";
        }

        if (remoteCompany != null) {
            resp.getOutputStream().println("Remote: " + remoteCompany.employ(name));
        }
    }

}
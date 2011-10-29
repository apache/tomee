package org.apache.openejb.arquillian.tests.localinject;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class PojoServlet extends HttpServlet {

    @EJB(beanName = "DefaultCompany")
    private CompanyLocal localCompany;

    @EJB
    private SuperMarket market;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        if (StringUtils.isEmpty(name)) {
            name = "OpenEJB";
        }

        if (localCompany != null) {
            resp.getOutputStream().println("Local: " + localCompany.employ(name));
        }
        if (market != null) {
            resp.getOutputStream().println(market.shop(name));
        }
    }
}
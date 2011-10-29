package org.apache.openejb.arquillian.tests.cdi.constructor;

import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SimpleServlet extends HttpServlet {

    @Resource
    private BeanManager beanManager;

    private final Car car;

    public SimpleServlet() {
        car = null;
    }

    @Inject
    public SimpleServlet(Car car) {
        this.car = car;
    }

    @PostConstruct
    public void construct() {
        System.out.println("construct");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        if (StringUtils.isEmpty(name)) {
            name = "OpenEJB";
        }

        if (car != null) {
            resp.getOutputStream().println(car.drive(name));
        }

        if (beanManager != null) {
            resp.getOutputStream().println("beanManager");
        }
    }
}
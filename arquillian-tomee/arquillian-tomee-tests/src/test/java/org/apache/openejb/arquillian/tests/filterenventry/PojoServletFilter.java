package org.apache.openejb.arquillian.tests.filterenventry;

import java.io.IOException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;

public class PojoServletFilter implements Filter {

    @Inject
    private Car car;

    @EJB
    private CompanyLocal localCompany;

    @EJB
    private SuperMarket market;

    @Resource(name = "returnEmail")
    private String returnEmail;

    @Resource(name = "connectionPool")
    private Integer connectionPool;

    @Resource(name = "startCount")
    private Long startCount;

    @Resource(name = "initSize")
    private Short initSize;

    @Resource(name = "totalQuantity")
    private Byte totalQuantity;

    @Resource(name = "enableEmail")
    private Boolean enableEmail;

    @Resource(name = "optionDefault")
    private Character optionDefault;

    /* TODO: Enable this resource after functionality is fixed
    @Resource
    */
    private Code defaultCode;

    /* TODO: Enable this resource after functionality is fixed
            @Resource
            @SuppressWarnings("unchecked")
    */
    private Class auditWriter;


    private FilterConfig config;

    public void init(FilterConfig config) {
        this.config = config;
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String name = req.getParameter("name");
        if (StringUtils.isEmpty(name)) {
            name = "OpenEJB";
        }

        if (car != null) {
            resp.getOutputStream().println(car.drive(name));
        }
        if (localCompany != null) {
            resp.getOutputStream().println("Local: " + localCompany.employ(name));
        }
        if (market != null) {
            resp.getOutputStream().println(market.shop(name));
        }
        if (connectionPool != null) {
            resp.getOutputStream().println("Connection Pool: " + connectionPool);
        }
        if (startCount != null) {
            resp.getOutputStream().println("Start Count: " + startCount);
        }
        if (initSize != null) {
            resp.getOutputStream().println("Init Size: " + initSize);
        }
        if (totalQuantity != null) {
            resp.getOutputStream().println("Total Quantity: " + totalQuantity);
        }
        if (enableEmail != null) {
            resp.getOutputStream().println("Enable Email: " + enableEmail);
        }
        if (optionDefault != null) {
            resp.getOutputStream().println("Option Default: " + optionDefault);
        }
        if (StringUtils.isNotEmpty(returnEmail) && returnEmail.equals("tomee@apache.org")) {
            resp.getOutputStream().println(returnEmail);
        }
        if (auditWriter != null) {
            resp.getOutputStream().println(auditWriter.getClass().getName());
        }
        if (defaultCode != null) {
            resp.getOutputStream().println("DefaultCode: " + defaultCode);
        }
    }


}
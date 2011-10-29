package org.apache.openejb.arquillian.tests.filterlocalinject;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.openejb.arquillian.tests.TestRun;
import org.junit.Assert;

public class PojoServletFilter implements Filter {

    @EJB
    private CompanyLocal localCompany;

    @EJB
    private SuperMarket market;

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        TestRun.run(req, resp, this);
    }

    public void testLocalBean() {
        Assert.assertNotNull(market);
        market.shop("test");
    }

    public void testLocalEjb() {
        Assert.assertNotNull(localCompany);
        localCompany.employ("test");
    }


}
package org.apache.openejb.arquillian.tests.filtercdiinject;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.openejb.arquillian.tests.TestRun;
import org.junit.Assert;

public class PojoServletFilter implements Filter {

    @Inject
    private Car car;

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        TestRun.run(req, resp, this);
    }

    public void testCdi() {
        Assert.assertNotNull(car);
        car.drive("test");
    }
}
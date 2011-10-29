package org.apache.openejb.arquillian.tests.filterremote;

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

public class RemoteServletFilter implements Filter {

    @EJB
    private CompanyRemote remoteCompany;

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        TestRun.run(req, resp, this);
    }

    public void testEjb () {
        Assert.assertNotNull(remoteCompany);
        remoteCompany.employ("test");
    }

}
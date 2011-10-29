package org.apache.openejb.arquillian.tests.sharedenv;

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

import org.apache.openejb.arquillian.tests.TestRun;
import org.junit.Assert;

public class PojoServletFilter implements Filter, Environment {

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

    @Override
    public String getReturnEmail() {
        return returnEmail;
    }

    @Override
    public Integer getConnectionPool() {
        return connectionPool;
    }

    @Override
    public Long getStartCount() {
        return startCount;
    }

    @Override
    public Short getInitSize() {
        return initSize;
    }

    @Override
    public Byte getTotalQuantity() {
        return totalQuantity;
    }

    @Override
    public Boolean getEnableEmail() {
        return enableEmail;
    }

    @Override
    public Character getOptionDefault() {
        return optionDefault;
    }

    @Inject
    private Green green;

    @EJB
    private Orange orange;

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        TestRun.run(req, resp, this);
    }


    public void testCdi() {
        assertEnvironment(green);
    }

    public void testEjb() {
        assertEnvironment(orange);
    }

    public void testFilter() {
        assertEnvironment(this);
    }

    public void assertEnvironment(Environment actual) {
        Assert.assertNotNull("component", actual);

        Environment expected = new Green("tomee@apache.org", 20, 200000l, (short) 6, (byte) 5, true, 'X');

        Assert.assertEquals("ReturnEmail", expected.getReturnEmail(), actual.getReturnEmail());
        Assert.assertEquals("ConnectionPool", expected.getConnectionPool(), actual.getConnectionPool());
        Assert.assertEquals("StartCount", expected.getStartCount(), actual.getStartCount());
        Assert.assertEquals("InitSize", expected.getInitSize(), actual.getInitSize());
        Assert.assertEquals("TotalQuantity", expected.getTotalQuantity(), actual.getTotalQuantity());
        Assert.assertEquals("EnableEmail", expected.getEnableEmail(), actual.getEnableEmail());
        Assert.assertEquals("OptionDefault", expected.getOptionDefault(), actual.getOptionDefault());
    }
}
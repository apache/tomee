package org.superbiz.interceptors;

import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

public class SecondStatelessInterceptedTest extends TestCase {

    private InitialContext initCtx;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.deployments.classpath.include", ".*interceptors/target/classes.*");

        initCtx = new InitialContext(properties);
    }

    @Test
    public void testMethodWithDefaultInterceptorsExcluded() throws Exception {
        SecondStatelessInterceptedLocal bean =
                (SecondStatelessInterceptedLocal) initCtx.lookup("SecondStatelessInterceptedBeanLocal");

        assert bean != null;

        List<String> expected = new ArrayList<String>();
        expected.add("ClassLevelInterceptorOne");
        expected.add("ClassLevelInterceptorTwo");
        expected.add("MethodLevelInterceptorOne");
        expected.add("MethodLevelInterceptorTwo");
        expected.add("SecondStatelessInterceptedBean");
        expected.add("methodWithDefaultInterceptorsExcluded");

        List<String> actual = bean.methodWithDefaultInterceptorsExcluded();
        assert expected.equals(actual) : "Expected " + expected + ", but got " + actual;
    }
}

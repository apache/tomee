package org.apache.openejb.arquillian.tests.resenventry;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.annotation.Resource;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.openejb.arquillian.tests.TestRun;
import org.junit.Assert;

@WebServlet("/blue")
public class Blue extends HttpServlet {

    @Resource(name = "java:comp/Validator")
    private Validator validator;

    @Resource(name = "java:comp/ValidatorFactory")
    private ValidatorFactory validatorFactory;

    @Resource(name = "java:comp/TransactionManager")
    private TransactionManager transactionManager;

    @Resource(name = "java:comp/TransactionSynchronizationRegistry")
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Resource(name = "java:comp/UserTransaction")
    private UserTransaction userTransaction;

    @Resource(name = "java:comp/BeanManager")
    private BeanManager beanManager;

    @Resource(name = "java:app/AppName")
    private String app;

    @Resource(name = "java:module/ModuleName")
    private String module;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TestRun.run(req, resp, this);
    }

    public void test() throws Exception {

        final Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Assert.assertNotNull(field.getName(), field.get(this));
        }

        Assert.assertEquals("app", "ServletResourceEnvEntryInjectionTest", app);
        Assert.assertEquals("module", "ServletResourceEnvEntryInjectionTest", module);
    }

}
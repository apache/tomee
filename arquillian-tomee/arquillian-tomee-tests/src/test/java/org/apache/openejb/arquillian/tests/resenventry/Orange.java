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
import javax.validation.ValidatorFactory;

import org.apache.openejb.arquillian.tests.TestRun;
import org.junit.Assert;

@WebServlet("/orange")
public class Orange extends HttpServlet {

    @Resource(name = "java:app/some/longer/path/MyValidatorFactory")
    private ValidatorFactory validatorFactory;

    @Resource(name = "java:app/some/longer/path/MyTransactionManager")
    private TransactionManager transactionManager;

    @Resource(name = "java:app/some/longer/path/MyTransactionSynchronizationRegistry")
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Resource(name = "java:app/some/longer/path/MyUserTransaction")
    private UserTransaction userTransaction;

    @Resource(name = "java:app/some/longer/path/MyBeanManager")
    private BeanManager beanManager;

    @Resource(name = "java:app/AppName")
    private String app;

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

        Assert.assertEquals("app", "BuiltInEnvironmentEntriesTest", app);
    }

}
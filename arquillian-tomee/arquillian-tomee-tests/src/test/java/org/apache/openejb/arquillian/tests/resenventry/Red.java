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

@WebServlet("/red")
public class Red  extends HttpServlet {

    @Resource
    private Validator validator;

    @Resource
    private ValidatorFactory validatorFactory;

    @Resource
    private TransactionManager transactionManager;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Resource
    private UserTransaction userTransaction;

    @Resource
    private BeanManager beanManager;

    @Resource
    private Purple purple;

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

        purple.test();
    }
}
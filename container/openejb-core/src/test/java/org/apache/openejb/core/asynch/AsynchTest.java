/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.asynch;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import java.util.concurrent.Future;

/**
 * Testing of the @Asynchronous annotation on beans.
 */
public class AsynchTest {

    private Assembler assembler;

    private ConfigurationFactory config;

    @Before
    public void beforeTest() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        config = new ConfigurationFactory();
        assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
    }

    @After
    public void afterTest() throws Exception {
        assembler.destroy();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Test
    public void testMethodScopeAsynch() throws Exception {
        System.out.println(long.class.getName());
        System.out.println(String[].class.getCanonicalName());
        //Build the application
        final AppModule app = new AppModule(this.getClass().getClassLoader(), "testasynch");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(TestBeanC.class));
        ejbJar.addEnterpriseBean(new SingletonBean(TestBeanD.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        final AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        final InitialContext context = new InitialContext();

        final String[] beans = new String[]{"TestBeanCLocal", "TestBeanDLocal"};
        for (final String beanName : beans) {
            final TestBean testBean = (TestBean) context.lookup(beanName);

            testBean.testA(Thread.currentThread().getId());
            Thread.sleep(1000L);
            Assert.assertEquals("testA was never executed", "testA", testBean.getLastInvokeMethod());
            final Future<String> future = testBean.testB(Thread.currentThread().getId());
            Thread.sleep(1000L);
            Assert.assertTrue("The task should be done", future.isDone());
            Assert.assertEquals("testB was never executed", "testB", testBean.getLastInvokeMethod());
            testBean.testC(Thread.currentThread().getId());
            Assert.assertEquals("testC was never executed", "testC", testBean.getLastInvokeMethod());
            testBean.testD(Thread.currentThread().getId());
            Assert.assertEquals("testD was never executed", "testD", testBean.getLastInvokeMethod());
        }
    }

    @Test
    public void testClassScopeAsynch() throws Exception {
        //Build the application
        final AppModule app = new AppModule(this.getClass().getClassLoader(), "testclassasynch");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(TestBeanA.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        final AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        final InitialContext context = new InitialContext();
        final TestBean test = (TestBean) context.lookup("TestBeanALocal");

        test.testA(Thread.currentThread().getId());
        Thread.sleep(1000L);
        Assert.assertEquals("testA was never executed", "testA", test.getLastInvokeMethod());

        final Future<String> future = test.testB(Thread.currentThread().getId());
        Thread.sleep(1000L);
        Assert.assertTrue("The task should be done", future.isDone());
        Assert.assertEquals("testB was never executed", "testB", test.getLastInvokeMethod());

        test.testC(Thread.currentThread().getId());
        Assert.assertEquals("testC was never executed", "testC", test.getLastInvokeMethod());

        test.testD(Thread.currentThread().getId());
        Assert.assertEquals("testD was never executed", "testD", test.getLastInvokeMethod());
    }

    @Test
    public void testSessionContext() throws Exception {
        //Build the application
        final AppModule app = new AppModule(this.getClass().getClassLoader(), "testcanceltask");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(TestBeanB.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        final AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        final InitialContext context = new InitialContext();
        final TestBean test = (TestBean) context.lookup("TestBeanBLocal");

        test.testA(Thread.currentThread().getId());
        Assert.assertEquals("testA was never executed", "testA", test.getLastInvokeMethod());

        final Future<String> future = test.testB(Thread.currentThread().getId());
        Thread.sleep(1000L);
        Assert.assertFalse(future.cancel(true));
        Assert.assertFalse(future.isCancelled());
        Assert.assertFalse(future.isDone());
        Thread.sleep(3000L);
        Assert.assertTrue(future.isDone());
        Assert.assertEquals("testB was never executed", "testB", test.getLastInvokeMethod());

        test.testC(Thread.currentThread().getId());
        Assert.assertEquals("testC was never executed", "testC", test.getLastInvokeMethod());

        test.testD(Thread.currentThread().getId());
        Thread.sleep(3000L);
        Assert.assertEquals("testD was never executed", "testD", test.getLastInvokeMethod());
    }

    public interface TestBean {

        public void testA(long callerThreadId);

        public Future<String> testB(long callerThreadId);

        public Future<String> testC(long callerThreadId);

        public void testD(long callerThreadId);

        public String getLastInvokeMethod();
    }

    @Stateless
    public static class TestBeanC implements TestBean {

        private String lastInvokeMethod;

        @Override
        @Asynchronous
        public void testA(final long callerThreadId) {
            Assert.assertFalse("testA should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testA";
        }

        @Override
        @Asynchronous
        public Future<String> testB(final long callerThreadId) {
            Assert.assertFalse("testB should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testB";
            return new AsyncResult<>("testB");
        }

        @Override
        public Future<String> testC(final long callerThreadId) {
            Assert.assertTrue("testC should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testC";
            return new AsyncResult<>("testC");
        }

        @Override
        public void testD(final long callerThreadId) {
            Assert.assertTrue("testD should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testD";
        }

        @Override
        public String getLastInvokeMethod() {
            return lastInvokeMethod;
        }
    }

    @Singleton
    public static class TestBeanD implements TestBean {

        private String lastInvokeMethod;

        @Override
        @Asynchronous
        public void testA(final long callerThreadId) {
            Assert.assertFalse("testA should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testA";
        }

        @Override
        @Asynchronous
        public Future<String> testB(final long callerThreadId) {
            Assert.assertFalse("testB should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testB";
            return new AsyncResult<>("testB");
        }

        @Override
        public Future<String> testC(final long callerThreadId) {
            Assert.assertTrue("testC should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testC";
            return new AsyncResult<>("testC");
        }

        @Override
        public void testD(final long callerThreadId) {
            Assert.assertTrue("testD should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testD";
        }

        @Override
        public String getLastInvokeMethod() {
            return lastInvokeMethod;
        }
    }

    @Asynchronous
    public static abstract class AbstractBean implements TestBean {

        protected String lastInvokeMethod;

        @Override
        public void testA(final long callerThreadId) {
            Assert.assertFalse("testA should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testA";
        }

        @Override
        public Future<String> testB(final long callerThreadId) {
            Assert.assertFalse("testB should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testB";
            return new AsyncResult<>("testB" + callerThreadId);
        }

    }

    @Singleton
    public static class TestBeanA extends AbstractBean implements TestBean {

        @Override
        public String getLastInvokeMethod() {
            return lastInvokeMethod;
        }

        @Override
        public Future<String> testC(final long callerThreadId) {
            Assert.assertTrue("testC should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testC";
            return new AsyncResult<>("testC");
        }

        @Override
        public void testD(final long callerThreadId) {
            Assert.assertTrue("testD should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            lastInvokeMethod = "testD";
        }
    }

    @Stateless
    public static class TestBeanB implements TestBean {

        private String lastInvokeMethod;

        @Resource
        private SessionContext sessionContext;

        @Override
        public void testA(final long callerThreadId) {
            Assert.assertTrue("testA should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            Exception expectedException = null;
            try {
                sessionContext.wasCancelCalled();
            } catch (final IllegalStateException e) {
                expectedException = e;
            }
            lastInvokeMethod = "testA";
            Assert.assertNotNull("IllegalStateException should be thrown", expectedException);
        }

        @Override
        @Asynchronous
        public Future<String> testB(final long callerThreadId) {
            Assert.assertFalse("testB should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            Assert.assertFalse(sessionContext.wasCancelCalled());
            try {
                Thread.sleep(3000L);
            } catch (final InterruptedException e) {
                //Ignore
            }
            Assert.assertTrue(sessionContext.wasCancelCalled());
            lastInvokeMethod = "testB";
            return new AsyncResult<>("echoB");
        }

        @Override
        public Future<String> testC(final long callerThreadId) {
            Assert.assertTrue("testC should be executed in blocing mode", Thread.currentThread().getId() == callerThreadId);
            Exception expectedException = null;
            try {
                sessionContext.wasCancelCalled();
            } catch (final IllegalStateException e) {
                expectedException = e;
            }
            Assert.assertNotNull("IllegalStateException should be thrown", expectedException);
            lastInvokeMethod = "testC";
            return null;
        }

        @Override
        @Asynchronous
        public void testD(final long callerThreadId) {
            Assert.assertFalse("testD should be executed in asynchronous mode", Thread.currentThread().getId() == callerThreadId);
            Exception expectedException = null;
            try {
                sessionContext.wasCancelCalled();
            } catch (final IllegalStateException e) {
                expectedException = e;
            }
            Assert.assertNotNull("IllegalStateException should be thrown", expectedException);
            lastInvokeMethod = "testD";
        }

        @Override
        public String getLastInvokeMethod() {
            return lastInvokeMethod;
        }
    }
}

/*
 * Copyright 2011 The Apache OpenEJB development community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.stateful;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.ConcurrentMethod;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.Timeout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.Local;
import jakarta.ejb.Stateful;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Using real non-pooled multithreaded access to test concurrent code execution on a stateful bean.
 * The test also employs a test for failure.
 */
public class StatefulConcurrentLookupTest {

    private static final int THREAD_COUNT = 100;

    @BeforeClass
    public static synchronized void beforeClass() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        assembler.createContainer(statefulContainerInfo);

        final EjbJar ejbJar = new EjbJar();

        final StatefulBean bean1 = new StatefulBean(MyLocalBeanImpl.class);
        final Timeout timeout1 = new Timeout();
        timeout1.setTimeout(10);
        timeout1.setUnit(TimeUnit.SECONDS);
        final ConcurrentMethod method1 = new ConcurrentMethod();
        method1.setMethod(new NamedMethod("*"));
        method1.setAccessTimeout(timeout1);
        bean1.getConcurrentMethod().add(method1);

        ejbJar.addEnterpriseBean(bean1);

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Test
    public void testLookup() throws Exception {
        runScenario(false);
    }

    @Test
    public void testLookupWithFail() throws Exception {
        runScenario(true);
    }

    private void runScenario(final boolean throwException) throws InterruptedException {
        final CountDownLatch startingLine = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch finishingLine = new CountDownLatch(THREAD_COUNT);

        final List<TestRunnable> runnables = new ArrayList<>();

        int i = 0;
        for (; i < THREAD_COUNT; i++) {
            final TestRunnable runnable = new TestRunnable("Lookup." + i, throwException, startingLine, finishingLine);
            runnables.add(runnable);
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();
        }

        assertTrue("Threads failed to start", startingLine.await(30, TimeUnit.SECONDS));
        assertTrue("Threads failed to finish", finishingLine.await(30, TimeUnit.SECONDS));

        for (final TestRunnable runnable : runnables) {
            if (!throwException == runnable.isSuccess()) {
                i--;
            }
        }

        assertEquals(THREAD_COUNT, (THREAD_COUNT - i));
    }

    private static class TestRunnable implements Runnable {

        private final CountDownLatch startingLine;
        private final CountDownLatch finishingLine;
        private final String name;
        private final boolean throwException;
        private volatile boolean success = false;

        private TestRunnable(final String name, final boolean throwException, final CountDownLatch startingLine, final CountDownLatch finishingLine) {
            this.name = name;
            this.throwException = throwException;
            this.startingLine = startingLine;
            this.finishingLine = finishingLine;
        }

        @Override
        public void run() {

            startingLine.countDown();
            try {
                startingLine.await();

                final InitialContext ctx = new InitialContext();
                final MyLocalBean bean = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");

                bean.set(name, throwException);
                success = name.equals(bean.get());
            } catch (final Throwable t) {
                success = false;
            } finally {
                finishingLine.countDown();
            }
        }

        public boolean isSuccess() {
            return success;
        }
    }

    @Local
    public static interface MyLocalBean {

        void set(final String txt, final boolean throwException);

        String get();
    }

    @Stateful
    public static class MyLocalBeanImpl implements MyLocalBean {

        private String txt = "default";
        private boolean throwException = false;

        @Override
        public void set(final String txt, final boolean throwException) {
            this.txt = txt;
            this.throwException = throwException;
        }

        @Override
        public String get() {

            if (this.throwException) {
                throw new UnsupportedOperationException(this.txt + " - This is an expected test Exception");
            }

            return this.txt;
        }
    }
}

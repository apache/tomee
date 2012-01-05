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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.ConcurrentMethod;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.Timeout;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Using real non-pooled multithreaded access to test concurrent code execution on a stateful bean.
 * The test also employs a test for failure.
 */
public class StatefulConcurrentLookupTest {

    private static final int THREAD_COUNT = 1000;

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

    @Test
    public void testLookup() throws Exception {

        final CountDownLatch latchInit = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch latchComplete = new CountDownLatch(THREAD_COUNT);

        final List<TestRunnable> runnables = new ArrayList<TestRunnable>();
        final List<Thread> threads = new ArrayList<Thread>();

        int i = 0;
        for (; i < THREAD_COUNT; i++) {
            runnables.add(new TestRunnable("Lookup." + i, false, latchInit, latchComplete));
            threads.add(new Thread(runnables.get(i)));
        }

        for (final Thread thread : threads) {
            thread.setDaemon(false);
            thread.start();
        }

        for (final Thread thread : threads) {
            thread.join();
        }

        for (final TestRunnable runnable : runnables) {
            if (runnable.isSuccess()) {
                i--;
            }
        }

        StatefulConcurrentLookupTest.print("testLookup: Threads successfully processed - " + (THREAD_COUNT - i));
        assertEquals(THREAD_COUNT, (THREAD_COUNT - i));
    }

    @Test
    public void testLookupWithFail() throws Exception {

        final CountDownLatch latchInit = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch latchComplete = new CountDownLatch(THREAD_COUNT);

        final List<TestRunnable> runnables = new ArrayList<TestRunnable>();
        final List<Thread> threads = new ArrayList<Thread>();

        int i = 0;
        for (; i < THREAD_COUNT; i++) {
            runnables.add(new TestRunnable("Lookup.Fail." + i, true, latchInit, latchComplete));
            threads.add(new Thread(runnables.get(i)));
        }

        for (final Thread thread : threads) {
            thread.setDaemon(false);
            thread.start();
        }

        for (final Thread thread : threads) {
            thread.join();
        }

        for (final TestRunnable runnable : runnables) {
            if (!runnable.isSuccess()) {
                i--;
            }
        }

        StatefulConcurrentLookupTest.print("testLookupWithFail: Threads successfully processed - " + (THREAD_COUNT - i));
        assertEquals(THREAD_COUNT, (THREAD_COUNT - i));
    }

    private static class TestRunnable implements Runnable {

        private final CountDownLatch latchInit;
        private final CountDownLatch latchComplete;
        private final String name;
        private final boolean throh;
        private boolean success = false;

        private TestRunnable(final String name, final boolean throh, final CountDownLatch latchInit, final CountDownLatch latchComplete) {
            this.name = name;
            this.throh = throh;
            this.latchInit = latchInit;
            this.latchComplete = latchComplete;
        }

        @Override
        public void run() {

            InitialContext ctx = null;

            try {
                ctx = new InitialContext();
            } catch (NamingException ex) {
                Logger.getLogger(StatefulConcurrentLookupTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                //Get all threads raring to go.
                latchInit.countDown();
            }

            try {
                latchInit.await();
            } catch (InterruptedException e) {
                //Ignore
            }

            //All threads will now fight for concurrent access
            try {

                final MyLocalBean bean = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");
                bean.set(this.name, this.throh);
                success = this.name.equals(bean.get());

            } catch (Throwable t) {
                success = false;
                //StatefulConcurrentLookupTest.print(t.getMessage());
            } finally {
                latchComplete.countDown();
            }

            try {
                latchComplete.await();
            } catch (InterruptedException e) {
                //Ignore
            }
        }

        public String getName() {
            return name;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    private static synchronized void print(final String txt) {
        System.out.println(txt);
    }

    @Local
    public static interface MyLocalBean {

        void set(final String txt, final boolean throh);

        String get();
    }

    @Stateful
    public static class MyLocalBeanImpl implements MyLocalBean {

        private String txt = "default";
        private boolean throh = false;

        @Override
        public void set(final String txt, final boolean throh) {
            this.txt = txt;
            this.throh = throh;
        }

        @Override
        public String get() {

            if (this.throh) {
                throw new UnsupportedOperationException(this.txt);
            }

            return this.txt;
        }
    }
}

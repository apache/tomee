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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
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

import jakarta.ejb.ConcurrentAccessTimeoutException;
import jakarta.ejb.Local;
import jakarta.ejb.Stateful;
import javax.naming.InitialContext;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class StatefulConcurrencyTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

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
        timeout1.setTimeout(1000);
        timeout1.setUnit(TimeUnit.MILLISECONDS);
        final ConcurrentMethod method1 = new ConcurrentMethod();
        method1.setMethod(new NamedMethod("*"));
        method1.setAccessTimeout(timeout1);
        bean1.getConcurrentMethod().add(method1);

        final StatefulBean bean2 = new StatefulBean("BeanNegative", MyLocalBeanImpl.class);
        final Timeout timeout2 = new Timeout();
        timeout2.setTimeout(-1);
        timeout2.setUnit(TimeUnit.MILLISECONDS);
        final ConcurrentMethod method2 = new ConcurrentMethod();
        method2.setMethod(new NamedMethod("*"));
        method2.setAccessTimeout(timeout2);
        bean2.getConcurrentMethod().add(method2);

        ejbJar.addEnterpriseBean(bean1);
        ejbJar.addEnterpriseBean(bean2);

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void testConcurrentMethodCall() throws Exception {
        MyLocalBeanImpl.semaphore = new Semaphore(0);

        final InitialContext ctx = new InitialContext();
        final MyLocalBean bean = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");
        final MyLocalBean bean2 = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");

        final CallRentrantThread call = new CallRentrantThread(bean, 3000);
        (new Thread(call)).start();

        // ensure the call on thread came in
        assertTrue(MyLocalBeanImpl.semaphore.tryAcquire(1, 30, TimeUnit.SECONDS));

        try {
            java.util.logging.Logger.getLogger(this.getClass().getName()).info("Expecting a SEVERE jakarta.ejb.ConcurrentAccessTimeoutException");
            bean2.callRentrant(bean, 0);
            fail("Expected exception");
        } catch (final Exception e) {
            if (e.getCause() instanceof ConcurrentAccessTimeoutException) {
                // that's what we want
            } else {
                throw e;
            }
        }
    }

    public void testNegativeAccessTimeout() throws Exception {
        MyLocalBeanImpl.semaphore = new Semaphore(0);

        final InitialContext ctx = new InitialContext();
        final MyLocalBean bean = (MyLocalBean) ctx.lookup("BeanNegativeLocal");

        final CallRentrantThread call = new CallRentrantThread(bean, 3000);
        (new Thread(call)).start();

        // ensure the call on thread came in
        assertTrue(MyLocalBeanImpl.semaphore.tryAcquire(1, 30, TimeUnit.SECONDS));

        bean.callRentrant(bean, 0);
    }

    @Local
    public static interface MyLocalBean {

        void callRentrant(MyLocalBean myself, long sleep);

        void sleep(long sleep);
    }

    @Stateful
    public static class MyLocalBeanImpl implements MyLocalBean {

        public static Semaphore semaphore;

        @Override
        public void callRentrant(final MyLocalBean myself, final long sleep) {
            semaphore.release();
            myself.sleep(sleep);
        }

        @Override
        public void sleep(final long sleep) {
            try {
                Thread.sleep(sleep);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public class CallRentrantThread implements Runnable {

        private final MyLocalBean bean;
        private final long sleep;

        public CallRentrantThread(final MyLocalBean bean, final long sleep) {
            this.bean = bean;
            this.sleep = sleep;
        }

        @Override
        public void run() {
            bean.callRentrant(bean, sleep);
        }
    }
}
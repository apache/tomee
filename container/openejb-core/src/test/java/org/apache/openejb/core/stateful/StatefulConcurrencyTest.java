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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.Timeout;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.TimeUnitType;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import java.util.concurrent.CyclicBarrier;

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
        statefulContainerInfo.properties.setProperty("BulkPassivate", "1");
        assembler.createContainer(statefulContainerInfo);

        final EjbJar ejbJar = new EjbJar();
        final StatefulBean bean = new StatefulBean(MyLocalBeanImpl.class);

        final Timeout timeout = new Timeout();
        timeout.setTimeout(1000);
        timeout.setUnit(TimeUnitType.Milliseconds);
        bean.setAccessTimeout(timeout);

        ejbJar.addEnterpriseBean(bean);

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    public void testConcurrentMethodCall() throws Exception {
        InitialContext ctx = new InitialContext();
        MyLocalBean bean = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");
        MyLocalBean bean2 = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");

        boolean error = bean.method1(bean, 2000);
        assertTrue(error);

        error = bean2.method1(bean, 500);
        assertFalse(error);
    }

    @Local
    public static interface MyLocalBean {
        boolean method1(MyLocalBean bean, long sleep);

        void method2(CyclicBarrier barrier);
    }

    @Stateful
    public static class MyLocalBeanImpl implements MyLocalBean {


        public boolean method1(MyLocalBean bean, long sleep) {
            System.out.println("Method 1 invoked! Thread: " + Thread.currentThread().getName());

            CyclicBarrier barrier = new CyclicBarrier(1);
            MyRunningMethod method = new MyRunningMethod(barrier, bean);
            Thread thread = new Thread(method);
            thread.setName("MyRunningMethodThread");
            thread.start();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return method.isError();
        }

        public void method2(CyclicBarrier barrier) {
            System.out.println("Method 2 invoked! Thread: "
                    + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class MyRunningMethod implements Runnable {
        private final MyLocalBean bean;
        private final CyclicBarrier barrier;

        private boolean error;

        public MyRunningMethod(CyclicBarrier barrier, MyLocalBean bean) {
            super();
            this.bean = bean;
            this.barrier = barrier;
        }

        public boolean isError() {
            return error;
        }

        public void run() {
            try {
                bean.method2(barrier);
                error = false;
            } catch (ConcurrentAccessTimeoutException e) {
                error = true;
            }
        }
    }

}
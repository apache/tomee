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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.ConcurrentMethod;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.Timeout;

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
                
        StatefulBean bean1 = new StatefulBean(MyLocalBeanImpl.class);
        Timeout timeout1 = new Timeout();
        timeout1.setTimeout(1000);
        timeout1.setUnit(TimeUnit.MILLISECONDS);
        ConcurrentMethod method1 = new ConcurrentMethod();
        method1.setMethod(new NamedMethod("*"));
        method1.setAccessTimeout(timeout1);
        bean1.getConcurrentMethod().add(method1);
        
        StatefulBean bean2 = new StatefulBean("BeanNegative", MyLocalBeanImpl.class);
        Timeout timeout2 = new Timeout();
        timeout2.setTimeout(-1);
        timeout2.setUnit(TimeUnit.MILLISECONDS);
        ConcurrentMethod method2 = new ConcurrentMethod();
        method2.setMethod(new NamedMethod("*"));
        method2.setAccessTimeout(timeout2);
        bean2.getConcurrentMethod().add(method2);

        ejbJar.addEnterpriseBean(bean1);
        ejbJar.addEnterpriseBean(bean2);

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    public void testConcurrentMethodCall() throws Exception {
        MyLocalBeanImpl.semaphore = new Semaphore(0);
        
        InitialContext ctx = new InitialContext();
        MyLocalBean bean = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");
        MyLocalBean bean2 = (MyLocalBean) ctx.lookup("MyLocalBeanImplLocal");
        
        CallRentrantThread call = new CallRentrantThread(bean, 3000);
        (new Thread(call)).start();
        
        // ensure the call on thread came in
        assertTrue(MyLocalBeanImpl.semaphore.tryAcquire(1, 30, TimeUnit.SECONDS));
        
        try {
            bean2.callRentrant(bean, 0);
            fail("Expected exception");
        } catch (Exception e) {
            if (e.getCause() instanceof ConcurrentAccessTimeoutException) {
                // that's what we want
            } else {
                throw e;
            }
        }
    }
  
    public void testNegativeAccessTimeout() throws Exception {
        MyLocalBeanImpl.semaphore = new Semaphore(0);
        
        InitialContext ctx = new InitialContext();
        MyLocalBean bean = (MyLocalBean) ctx.lookup("BeanNegativeLocal");
        
        CallRentrantThread call = new CallRentrantThread(bean, 3000);
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
        
        public void callRentrant(MyLocalBean myself, long sleep) {
            semaphore.release();
            myself.sleep(sleep);
        }

        public void sleep(long sleep) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public class CallRentrantThread implements Runnable {
        private final MyLocalBean bean;
        private final long sleep;
        
        public CallRentrantThread(MyLocalBean bean, long sleep) {
            this.bean = bean;
            this.sleep = sleep;
        }

        public void run() {
            bean.callRentrant(bean, sleep);
        }
    }
}
/**
 *
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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import jakarta.ejb.ConcurrentAccessTimeoutException;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Revision$ $Date$
 */
public class StatelessInstanceManagerPoolingTest extends TestCase {

    public static final AtomicInteger instances = new AtomicInteger();
    public static final AtomicInteger discardedInstances = new AtomicInteger();

    public void testStatelessBeanPooling() throws Exception {

        final InitialContext ctx = new InitialContext();
        final Object object = ctx.lookup("CounterBeanLocal");
        assertTrue("instanceof counter", object instanceof Counter);

        final CountDownLatch startPistol = new CountDownLatch(1);
        final CountDownLatch startingLine = new CountDownLatch(10);
        final CountDownLatch finishingLine = new CountDownLatch(30);

        final Counter counter = (Counter) object;
        // Do a business method...
        final Runnable r = new Runnable() {
            public void run() {
                counter.race(startingLine, startPistol);
                finishingLine.countDown();
            }
        };

        //  -- READY --

        // How much ever the no of client invocations the count should be 10 as only 10 instances will be created.
        for (int i = 0; i < 30; i++) {
            final Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the finish line
        startingLine.await(1000, TimeUnit.MILLISECONDS);

        //  -- SET --

        assertEquals(10, instances.get());

        //  -- GO --

        startPistol.countDown(); // go

        finishingLine.await(1000, TimeUnit.MILLISECONDS);

        //  -- DONE --

        assertEquals(10, instances.get());

    }

    public void testStatelessBeanRelease() throws Exception {

        final int count = 50;
        final CountDownLatch invocations = new CountDownLatch(count);
        final InitialContext ctx = new InitialContext();

        // 'count' instances should be created and discarded.
        for (int i = 0; i < count; i++) {
            final Thread thread = new Thread(new Runnable() {
                public void run() {

                    Object object = null;
                    try {
                        object = ctx.lookup("CounterBeanLocal");
                    } catch (final NamingException e) {
                        assertTrue(false);
                    }
                    final Counter counter = (Counter) object;
                    assertNotNull(counter);

                    boolean run = true;

                    while (run) {
                        try {
                            counter.explode();
                        } catch (final jakarta.ejb.ConcurrentAccessTimeoutException e) {
                            //Try again in moment...
                            try {
                                Thread.sleep(10);
                            } catch (final InterruptedException ie) {
                                //Ignore
                            }
                        } catch (final Exception e) {
                            invocations.countDown();
                            run = false;
                        }
                    }
                }
            }, "test-thread-" + count);

            thread.setDaemon(false);
            thread.start();
        }

        final boolean success = invocations.await(20, TimeUnit.SECONDS);

        assertTrue("invocations timeout -> invocations.getCount() == " + invocations.getCount(), success);

        assertEquals(count, discardedInstances.get());

    }


    public void testStatelessBeanTimeout() throws Exception {

        final InitialContext ctx = new InitialContext();
        final Object object = ctx.lookup("CounterBeanLocal");
        assertTrue("instanceof counter", object instanceof Counter);

        final CountDownLatch timeouts = new CountDownLatch(10);
        final CountDownLatch startPistol = new CountDownLatch(1);
        final CountDownLatch startingLine = new CountDownLatch(10);

        final Counter counter = (Counter) object;

        // Do a business method...
        final Runnable r = new Runnable() {
            public void run() {
                try {
                    counter.race(startingLine, startPistol);
                } catch (final ConcurrentAccessTimeoutException ex) {
                    comment("Leap Start");
                    timeouts.countDown();
                }
            }
        };


        comment("On your mark!");

        for (int i = 0; i < 20; i++) {
            final Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the start line
        assertTrue("expected 10 invocations", startingLine.await(3000, TimeUnit.MILLISECONDS));

        comment("Get Set!");

        // Wait for the other beans timeout
        assertTrue("expected 10 timeouts", timeouts.await(3000, TimeUnit.MILLISECONDS));

        assertEquals(10, instances.get(), 1.1);

        comment("Go!");

        startPistol.countDown(); // go
    }

    public static Object lock = new Object[]{};

    private static void comment(final String x) {
//        synchronized(lock){
//            System.out.println(x);
//            System.out.flush();
//        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("MaxSize", "10");
        statelessContainerInfo.properties.setProperty("MinSize", "2");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        final StatelessBean bean = new StatelessBean(CounterBean.class);
        bean.addBusinessLocal(Counter.class.getName());
        bean.addBusinessRemote(RemoteCounter.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        instances.set(0);
        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public static interface Counter {
        int count();

        void race(CountDownLatch ready, CountDownLatch go);

        void explode();
    }

    @Remote
    public static interface RemoteCounter extends Counter {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    @Stateless
    public static class CounterBean implements Counter, RemoteCounter {

        private final int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        public int count() {
            return instances.get();
        }

        public void explode() {
            final int i = discardedInstances.incrementAndGet();
            throw new NullPointerException("Test expected this null pointer: " + i);
        }

        public void race(final CountDownLatch ready, final CountDownLatch go) {
            comment("ready = " + count);
            ready.countDown();
            try {
                go.await();
                comment("running = " + count);
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        }

        public void init() {

        }

        public void destroy() {

        }
    }
}

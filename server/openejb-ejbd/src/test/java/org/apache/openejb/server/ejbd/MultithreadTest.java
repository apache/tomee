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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.ejb.ConcurrentAccessException;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @version $Rev$ $Date$
 */
public class MultithreadTest {

    private ServiceDaemon serviceDaemon;
    private Counter counter;

    private static CountDownLatch startPistol;
    private static CountDownLatch startingLine;
    private static CountDownLatch invocations;

    @Test
    public void testStatelessBeanPooling() throws Exception {

        startPistol = new CountDownLatch(1);
        startingLine = new CountDownLatch(10);
        final CountDownLatch finishingLine = new CountDownLatch(30);

        // Do a business method...
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                counter.race();
                finishingLine.countDown();
            }
        };

        //  -- READY --

        // How much ever the no of client invocations the count should be 10 as only 10 instances will be created.
        final Collection<Thread> threads = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            final Thread t = new Thread(r);
            threads.add(t);
            t.start();
        }

        // Wait for the beans to reach the finish line
        startingLine.await(1000, TimeUnit.MILLISECONDS);

        //  -- SET --

        assertEquals(10, CounterBean.instances.get());

        //  -- GO --

        startPistol.countDown(); // go

        finishingLine.await(1000, TimeUnit.MILLISECONDS);

        //  -- DONE --

        assertEquals(10, CounterBean.instances.get());

        for (final Thread t : threads) {
            t.join(1000);
        }
    }

    @Test
    public void testStatelessBeanRelease() throws Exception {

        invocations = new CountDownLatch(30);

        // Do a business method...
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    counter.explode();
                } catch (Exception e) {
                    //Ignore
                }
            }
        };

        //  -- READY --

        // 30 instances should be created and discarded.
        final Collection<Thread> threads = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            final Thread t = new Thread(r);
            threads.add(t);
            t.start();
        }

        final boolean success = invocations.await(10, TimeUnit.SECONDS);
        final int count = CounterBean.discardedInstances.get();
        assertTrue("Timeout after 10s. CountDownLatch: " + count + " of 30 invocations", success);
        assertEquals(30, CounterBean.discardedInstances.get());

        for (final Thread t : threads) {
            t.join(1000);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testStatelessBeanTimeout() throws Exception {

        final CountDownLatch timeouts = new CountDownLatch(10);
        startPistol = new CountDownLatch(1);
        startingLine = new CountDownLatch(10);

        // Do a business method...
        final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    counter.race();
                } catch (ConcurrentAccessException ex) {
                    comment("Leap Start");
                    timeouts.countDown();
                    assertEquals("No instances available in Stateless Session Bean pool.  Waited 100 MILLISECONDS", ex.getMessage());
                } catch (Throwable t) {
                    error.set(t);
                    fail("Unexpected exception" + t.getClass().getName() + " " + t.getMessage()); // useless in another thread
                }
            }
        };

        comment("On your mark!");

        final Collection<Thread> threads = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            final Thread t = new Thread(r);
            threads.add(t);
            t.start();
        }

        // Wait for the beans to reach the start line
        assertTrue("expected 10 invocations", startingLine.await(3000, TimeUnit.MILLISECONDS));

        comment("Get Set!");

        // Wait for the other beans timeout
        assertTrue("expected 10 timeouts", timeouts.await(300000, TimeUnit.MILLISECONDS));

        if (error.get() != null) {
            error.get().printStackTrace();
            fail(error.get().getMessage());
        }

        assertEquals(10, CounterBean.instances.get());

        comment("Go!");

        startPistol.countDown(); // go

        for (final Thread t : threads) {
            t.join(1000);
        }
    }

    @After
    public void tearDown() throws Exception {
        serviceDaemon.stop();
        OpenEJB.destroy();
    }

    @Before
    public void setUp() throws Exception {
        final int poolSize = 10;

        System.setProperty("openejb.client.connectionpool.size", "" + (poolSize * 2));

        final EjbServer ejbServer = new EjbServer();
        final KeepAliveServer keepAliveServer = new KeepAliveServer(ejbServer, false);

        final Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        final ServicePool pool = new ServicePool(keepAliveServer, (poolSize * 2));
        this.serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        final int port = serviceDaemon.getPort();

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("PoolSize", "" + poolSize);
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

        CounterBean.instances.set(0);
        assembler.createApplication(config.configureApplication(ejbJar));

        final Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
        final Context context = new InitialContext(props);
        counter = (Counter) context.lookup("CounterBeanRemote");
    }

    public static Object lock = new Object[]{};

    private static void comment(final String x) {
        //        synchronized(lock){
        //            System.out.println(x);
        //            System.out.flush();
        //        }
    }

    public static interface Counter {

        int count();

        void race();

        void explode();
    }

    @Remote
    public static interface RemoteCounter extends Counter {

    }

    @Stateless
    public static class CounterBean implements Counter, RemoteCounter {

        public static AtomicInteger instances = new AtomicInteger();
        public static AtomicInteger discardedInstances = new AtomicInteger();

        private final int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        @Override
        public int count() {
            return instances.get();
        }

        public int discardCount() {
            return discardedInstances.get();
        }

        @Override
        public void explode() {
            try {
                discardedInstances.incrementAndGet();
                throw new NullPointerException("Test expected this null pointer");
            } finally {
                invocations.countDown();
            }
        }

        @Override
        public void race() {
            comment("ready = " + count);
            startingLine.countDown();
            try {
                startPistol.await();
                comment("running = " + count);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        public void init() {

        }

        public void destroy() {

        }
    }
}

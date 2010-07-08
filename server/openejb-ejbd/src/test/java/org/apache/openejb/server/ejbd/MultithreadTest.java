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

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.client.ConnectionPoolTimeoutException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.ConcurrentAccessException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class MultithreadTest extends TestCase {

    private ServiceDaemon serviceDaemon;
    private Counter counter;

    private static CountDownLatch startPistol;
    private static CountDownLatch startingLine;
    private static CountDownLatch invocations;

    public void testStatelessBeanPooling() throws Exception {

        startPistol = new CountDownLatch(1);
        startingLine = new CountDownLatch(10);
        final CountDownLatch finishingLine = new CountDownLatch(30);

        // Do a business method...
        Runnable r = new Runnable() {
            public void run() {
                counter.race();
                finishingLine.countDown();
            }
        };

        //  -- READY --

        // How much ever the no of client invocations the count should be 10 as only 10 instances will be created.
        for (int i = 0; i < 30; i++) {
            Thread t = new Thread(r);
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

    }

    public void testStatelessBeanRelease() throws Exception {

    	invocations = new CountDownLatch(30);

        // Do a business method...
        Runnable r = new Runnable(){
        	public void run(){
                try{
        		    counter.explode();
                }catch(Exception e){

                }
            }
        };

        //  -- READY --

        // 30 instances should be created and discarded.
        for (int i = 0; i < 30; i++) {
            Thread t = new Thread(r);
            t.start();
        }

        boolean success = invocations.await(10000, TimeUnit.MILLISECONDS);

        assertTrue(success);

        assertEquals(30, CounterBean.discardedInstances.get());

    }


    public void testStatelessBeanTimeout() throws Exception {

        final CountDownLatch timeouts = new CountDownLatch(10);
        startPistol = new CountDownLatch(1);
        startingLine = new CountDownLatch(10);

        // Do a business method...
        Runnable r = new Runnable(){
        	public void run(){
        		try{
                    counter.race();
                }catch (ConcurrentAccessException ex){
                    comment("Leap Start");
                    timeouts.countDown();
                    assertEquals("An invocation of the Stateless Session Bean CounterBean has timed-out", ex.getMessage());
        		} catch (Throwable t) {
                    fail("Unexpected exception" + t.getClass().getName() + " " + t.getMessage());
                }
        	}
        };


        comment("On your mark!");

        for (int i = 0; i < 20; i++) {
            Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the start line
        assertTrue("expected 10 invocations", startingLine.await(3000, TimeUnit.MILLISECONDS));

        comment("Get Set!");

        // Wait for the other beans timeout
        assertTrue("expected 10 timeouts", timeouts.await(300000, TimeUnit.MILLISECONDS));

        assertEquals(10, CounterBean.instances.get());

        comment("Go!");

        startPistol.countDown(); // go
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        serviceDaemon.stop();
        OpenEJB.destroy();
    }

    protected void setUp() throws Exception {
        super.setUp();

        int poolSize = 10;

        System.setProperty("openejb.client.connectionpool.size", "" + (poolSize*2));

        EjbServer ejbServer = new EjbServer();
        KeepAliveServer keepAliveServer = new KeepAliveServer(ejbServer);

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServicePool pool = new ServicePool(keepAliveServer, "ejbd", (poolSize*2));
        this.serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        int port = serviceDaemon.getPort();

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("PoolSize", "" + poolSize);
        statelessContainerInfo.properties.setProperty("MinSize", "2");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        StatelessBean bean = new StatelessBean(CounterBean.class);
        bean.addBusinessLocal(Counter.class.getName());
        bean.addBusinessRemote(RemoteCounter.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        CounterBean.instances.set(0);
        assembler.createApplication(config.configureApplication(ejbJar));

        Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
        Context context = new InitialContext(props);
        counter = (Counter) context.lookup("CounterBeanRemote");
    }


    public static Object lock = new Object[]{};

    private static void comment(String x) {
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

        private int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        public int count() {
            return instances.get();
        }

        public int discardCount() {
            return discardedInstances.get();
        }

        public void explode() {
            try {
                discardedInstances.incrementAndGet();
                throw new NullPointerException();
            } finally {
                invocations.countDown();
            }
        }

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

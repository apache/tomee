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
package org.apache.openejb.core.singleton;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.NoSuchEJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class SingletonLazyInstantiationTest extends TestCase {

    private static final AtomicInteger accesses = new AtomicInteger();
    private static final AtomicBoolean exception = new AtomicBoolean();

    @Override
    protected void setUp() throws Exception {
        exception.set(false);
        MySingleton.instances.set(0);

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

        // Setup the descriptor information

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(MySingleton.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void testSuccess() throws Exception {
        final Context context = new InitialContext();

        final int threads = 200;
        final CyclicBarrier start = new CyclicBarrier(threads + 1);
        final CountDownLatch finish = new CountDownLatch(threads);

        for (int i = threads; i > 0; i--) {
            final Thread thread = new Thread(new Client(context, start, finish));
            thread.setDaemon(true);
            thread.start();
        }

        start.await(30, TimeUnit.SECONDS);

        assertFalse("All threads did not start", start.isBroken());

        assertTrue("Client threads did not complete", finish.await(30, TimeUnit.SECONDS));

        assertEquals("incorrect number of instances", 1, MySingleton.instances.get());

        // Invoke a business method just to be sure
        final MySingletonLocal singletonLocal = (MySingletonLocal) context.lookup("MySingletonLocal");
        assertEquals(1, singletonLocal.getId());
    }

    public void testFailure() throws Throwable {

        final Exception exception1 = new Exception("Inner exception");
        exception1.fillInStackTrace();

        exception.set(true);

        final Context context = new InitialContext();

        final int threads = 200;
        final CyclicBarrier start = new CyclicBarrier(threads + 1);
        final CountDownLatch finish = new CountDownLatch(threads);

        for (int i = threads; i > 0; i--) {
            final Thread thread = new Thread(new Client(context, start, finish));
            thread.setDaemon(true);
            thread.start();
        }

        start.await(30, TimeUnit.SECONDS);

        assertFalse("All threads did not start", start.isBroken());

        assertTrue("Client threads did not complete", finish.await(30, TimeUnit.SECONDS));

        assertEquals("incorrect number of instances", 1, MySingleton.instances.get());

        // Invoke a business method just to be sure
        final MySingletonLocal singletonLocal = (MySingletonLocal) context.lookup("MySingletonLocal");
        try {
            assertEquals(1, singletonLocal.getId());
            fail("Expected NoSuchEJBException");
        } catch (final NoSuchEJBException e) {
            // pass
        }
    }

    public static class Client implements Runnable {
        private final CyclicBarrier start;
        private final CountDownLatch finish;
        private final Context context;

        public Client(final Context context, final CyclicBarrier start, final CountDownLatch finish) {
            this.context = context;
            this.start = start;
            this.finish = finish;
        }

        public void run() {
            try {
                log("waiting to start");

                if (start != null) start.await(20, TimeUnit.SECONDS);

                log("looking up the singleton");

                final MySingletonLocal singletonLocal = (MySingletonLocal) context.lookup("MySingletonLocal");

                // Have to invoke a method to ensure creation
                singletonLocal.getId();

                log("singleton retrieved " + singletonLocal);
            } catch (final NoSuchEJBException e) {
                if (!exception.get()) {
                    synchronized (System.out) {
                        log("exception");
                        e.printStackTrace(System.out);
                    }
                    throw new RuntimeException(e);
                }
            } catch (final Exception e) {
                synchronized (System.out) {
                    log("exception");
                    e.printStackTrace(System.out);
                }
                throw new RuntimeException(e);
            } finally {
                log("finished");
                finish.countDown();
            }
        }

    }

    public static void log(final String s) {
//        System.out.println(Thread.currentThread().getName() + " : " + s);
    }


    public static interface MySingletonLocal {
        public int getId();
    }

    public static class MySingleton implements MySingletonLocal {
        public static final AtomicInteger instances = new AtomicInteger();
        private int id;

        @PostConstruct
        public void construct() throws Exception {
            id = instances.incrementAndGet();
            log("constructing singleton: " + id);
            Thread.sleep(5000);
            if (exception.get()) throw new Exception("I threw an exception");
        }

        public int getId() {
            return id;
        }
    }
}

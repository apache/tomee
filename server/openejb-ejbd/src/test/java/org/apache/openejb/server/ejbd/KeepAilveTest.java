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

import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;

import jakarta.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class KeepAilveTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void _testPool() throws Exception {
        final int threads = 2;
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(threads));

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                waitOneSecond();
            }
        };

        print(pool);

        for (int i = 0; i < 10; i++) {
            System.out.println("" + i);
            pool.execute(runnable);
            print(pool);
        }

        waitOneSecond();

        for (int i = 0; i < 10; i++) {
            print(pool);
            waitOneSecond();
        }
        print(pool);
        fail();
    }

    private void waitOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void print(final ThreadPoolExecutor pool) {
        System.out.println("==========================================");
        final int activeCount = pool.getActiveCount();
        System.out.println("activeCount = " + activeCount);
        final int corePoolSize = pool.getCorePoolSize();
        System.out.println("corePoolSize = " + corePoolSize);
        final int largestPoolSize = pool.getLargestPoolSize();
        System.out.println("largestPoolSize = " + largestPoolSize);
        final int maximumPoolSize = pool.getMaximumPoolSize();
        System.out.println("maximumPoolSize = " + maximumPoolSize);
        final int poolSize = pool.getPoolSize();
        System.out.println("poolSize = " + poolSize);
        final int queueSize = pool.getQueue().size();
        System.out.println("queueSize = " + queueSize);
        final long taskCount = pool.getTaskCount();
        System.out.println("taskCount = " + taskCount);
        System.out.println("==========================================");
    }

    public void _test() throws Exception {

    }

    public void test() throws Exception {
        final EjbServer ejbServer = new EjbServer();
        final KeepAliveServer keepAliveServer = new KeepAliveServer(ejbServer, false);

        final Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        final ServicePool pool = new ServicePool(keepAliveServer, 10, 5000, true);
        final ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        try {

            final int port = serviceDaemon.getPort();

            final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            final ConfigurationFactory config = new ConfigurationFactory();

            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(EchoBean.class));

            assembler.createApplication(config.configureApplication(ejbJar));

            // good creds

            final int threads = 1;
            final CountDownLatch latch = new CountDownLatch(threads);

            final Collection<Thread> th = new ArrayList<>(threads);
            for (int i = 0; i < threads; i++) {
                final Client client = new Client(latch, i, port);
                th.add(thread(client, false));
            }

            final boolean await = latch.await(60, TimeUnit.SECONDS);
            assertTrue(await);

            for (final Thread t : th) {
                t.join(1000);
            }
        } finally {
            serviceDaemon.stop();
            OpenEJB.destroy();
        }
    }

    public static Thread thread(final Runnable runnable, final boolean daemon) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        thread.start();
        return thread;
    }

    public static class Client implements Runnable {

        private final Echo echo;
        private final CountDownLatch latch;
        private final int id;

        public Client(final CountDownLatch latch, final int i, final int port) throws NamingException {
            this.latch = latch;
            this.id = i;

            final Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port + "?" + id);
            final Context context = new InitialContext(props);

            this.echo = (Echo) context.lookup("EchoBeanRemote");
        }

        @Override
        public void run() {

            try {
                int count = 10;
                for (; count >= 0; count--) {
                    final String message = count + " bottles of beer on the wall";

                    //                    Thread.currentThread().setName("client-"+id+": "+count);

                    final String response = echo.echo(message);
                    Assert.assertEquals(message, reverse(response));
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                latch.countDown();
            }
        }

        private Object reverse(final String s) {
            return new StringBuilder(s).reverse().toString();
        }
    }

    public static class EchoBean implements Echo {

        @Override
        public String echo(final String s) {
            //            System.out.println(s);
            return new StringBuilder(s).reverse().toString();
        }
    }

    @Remote
    public static interface Echo {

        public String echo(String s);
    }
}

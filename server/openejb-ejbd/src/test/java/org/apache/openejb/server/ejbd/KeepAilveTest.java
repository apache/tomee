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
import junit.framework.Assert;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.core.ServerFederation;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.Remote;

/**
 * @version $Rev$ $Date$
 */
public class KeepAilveTest extends TestCase {
    public void _testPool() throws Exception {
        int threads = 2;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, 120, TimeUnit.SECONDS, new LinkedBlockingQueue());

        Runnable runnable = new Runnable(){
            public void run() {
                waitOneSecond();
            }
        };

        print(pool);

        for (int i = 0; i < 10 ; i++) {
            System.out.println("" + i);
            pool.execute(runnable);
            print(pool);
        }

        waitOneSecond();

        for (int i = 0; i < 10 ; i++) {
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

    private void print(ThreadPoolExecutor pool) {
        System.out.println("==========================================");
        int activeCount = pool.getActiveCount();
        System.out.println("activeCount = " + activeCount);
        int corePoolSize = pool.getCorePoolSize();
        System.out.println("corePoolSize = " + corePoolSize);
        int largestPoolSize = pool.getLargestPoolSize();
        System.out.println("largestPoolSize = " + largestPoolSize);
        int maximumPoolSize = pool.getMaximumPoolSize();
        System.out.println("maximumPoolSize = " + maximumPoolSize);
        int poolSize = pool.getPoolSize();
        System.out.println("poolSize = " + poolSize);
        int queueSize = pool.getQueue().size();
        System.out.println("queueSize = " + queueSize);
        long taskCount = pool.getTaskCount();
        System.out.println("taskCount = " + taskCount);
        System.out.println("==========================================");
    }

    public void test() throws Exception {
        
    }
    public void _test() throws Exception {
        EjbServer ejbServer = new EjbServer();
        KeepAliveServer keepAliveServer = new KeepAliveServer(ejbServer);

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServicePool pool = new ServicePool(keepAliveServer, 10);
        ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        try {

            int port = serviceDaemon.getPort();

            Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            ConfigurationFactory config = new ConfigurationFactory();

            EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(EchoBean.class));

            assembler.createApplication(config.configureApplication(ejbJar));

            // good creds

            int threads = 1;
            CountDownLatch latch = new CountDownLatch(threads);

            for (int i = 0; i < threads; i++) {
                Client client = new Client(latch, i, port);
                thread(client, false);
            }

            assertTrue(latch.await(60, TimeUnit.SECONDS));
        } finally {
            serviceDaemon.stop();
            OpenEJB.destroy();
        }
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        thread.start();
    }

    public static class Client implements Runnable {

        private final Echo echo;
        private final CountDownLatch latch;
        private final int id;

        public Client(CountDownLatch latch, int i, int port) throws NamingException {
            this.latch = latch;
            this.id = i;

            Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port +"?"+id);
            Context context = new InitialContext(props);

            this.echo = (Echo) context.lookup("EchoBeanRemote");
        }

        public void run() {

            try {
                int count = 10;
                for (; count >= 0; count--){
                    String message = count + " bottles of beer on the wall";

//                    Thread.currentThread().setName("client-"+id+": "+count);

                    String response = echo.echo(message);
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

        private Object reverse(String s) {
            return new StringBuilder(s).reverse().toString();
        }
    }


    public static class EchoBean implements Echo {
        public String echo(String s) {
//            System.out.println(s);
            return new StringBuilder(s).reverse().toString();
        }
    }

    @Remote
    public static interface Echo {
        public String echo(String s);
    }
}

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
import org.apache.openejb.util.Duration;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.core.ServerFederation;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ejb.Remote;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MultithreadTest extends TestCase {

    public void test() throws Exception {
        EjbServer ejbServer = new EjbServer();

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServicePool pool = new ServicePool(ejbServer, "ejbd", 10);
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
            Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
            Context context = new InitialContext(props);
            Echo echo = (Echo) context.lookup("EchoBeanRemote");

            int threads = 20;
            CountDownLatch latch = new CountDownLatch(threads);
            Client client = new Client(echo, latch);

            for (int i = 0; i < threads; i++) {
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
        private CountDownLatch latch;

        public Client(Echo echo, CountDownLatch latch) {
            this.echo = echo;
            this.latch = latch;
        }

        public void run() {

            try {
                int count = 250;
                for (; count >= 0; count--){
                    String message = count + " bottles of beer on the wall";
                    String response = echo.echo(message);
                    Assert.assertEquals(message, reverse(response));
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
            return new StringBuilder(s).reverse().toString();
        }
    }

    @Remote
    public static interface Echo {
        public String echo(String s);
    }
}

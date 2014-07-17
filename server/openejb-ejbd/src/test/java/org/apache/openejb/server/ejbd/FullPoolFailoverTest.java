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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.client.Client;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServerServiceFilter;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.util.CountingLatch;

import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class FullPoolFailoverTest extends TestCase {

    private Counter counter;

    private static CountDownLatch resume;
    private static CountingLatch paused;
    private static final URI red = URI.create("red");
    private static final URI blue = URI.create("blue");

    public static List<URI> hits = new ArrayList<URI>();
    public static List<URI> hold = new ArrayList<URI>();

    public void testStatelessBeanTimeout() throws Exception {

        setup(10, 20);

        Client.addRetryCondition(ConcurrentAccessTimeoutException.class);

        resume = new CountDownLatch(1);
        paused = new CountingLatch(10);

        // Do a business method...
        final Runnable r = new Runnable() {
            public void run() {
                counter.hit();
            }
        };

        hold.add(red);

        for (int i = 0; i < 10; i++) {
            final Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the start line
        try {
            if (!paused.await(3000, TimeUnit.MILLISECONDS)) {
                System.out.println();
            }
        } catch (final Throwable t) {
            System.out.println();
        }
        assertTrue("expected 10 invocations", paused.await(3000, TimeUnit.MILLISECONDS));

        assertEquals(10, CounterBean.instances.get());
        assertEquals(10, hits.size());

        final List<URI> expected = new ArrayList<URI>();
        for (int i = 0; i < 10; i++) {
            expected.add(red);
        }

        assertEquals(expected, hits);

        // This one should failover to the blue server
        try {
            counter.hit();
            fail("Expected ConcurrentAccessTimeoutException");
        } catch (final ConcurrentAccessTimeoutException e) {
            // both "red" and "blue" servers are technically using the
            // same stateless session bean pool, which is fully busy
            // but ... this exception should have come from the "blue" server
        }

        // one more hit on red that should have failed over to blue
        expected.add(red);
        expected.add(blue);

        assertEquals(expected, hits);

        // A second invoke on this should now have using talking to blue
        // then it should fail back to red
        try {
            counter.hit();
            fail("Expected ConcurrentAccessTimeoutException");
        } catch (final ConcurrentAccessTimeoutException e) {
        }

        expected.add(blue);
        expected.add(red);

        assertEquals(expected, hits);

        resume.countDown(); // go
    }

    public void testConnectionPoolTimeout() throws Exception {

        setup(30, 10);

        resume = new CountDownLatch(1);

        // This is used to cause invoking threads to pause
        // so all pools can be depleted
        paused = new CountingLatch(10);

        // Do a business method...
        final Runnable r = new Runnable() {
            public void run() {
                counter.hit();
            }
        };

        hold.add(red);

        final List<URI> expected = new ArrayList<URI>();

        for (int i = 0; i < 10; i++) {
            expected.add(red);
            final Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the start line
        assertTrue("expected 10 invocations", paused.await(3000, TimeUnit.MILLISECONDS));

        assertEquals(10, CounterBean.instances.get());
        assertEquals(10, hits.size());
        assertEquals(expected, hits);

        // This one should failover to the blue server
        final URI uri = counter.hit();
        assertEquals(blue, uri);

        // the red pool is fully busy, so we should have failed over to blue
        expected.add(blue);
        assertEquals(expected, hits);

        // Now hold blue as well
        hold.add(blue);

        for (int i = 0; i < 10; i++) {
            paused.countUp();
            expected.add(blue);
            final Thread t = new Thread(r);
            t.start();
        }

        // Wait for the beans to reach the start line
        assertTrue("expected 20 invocations", paused.await(3000, TimeUnit.MILLISECONDS));

        // The extra 10 invocations should all have been on the "blue" server
        assertEquals(expected, hits);

        // A second invoke on this should now have using talking to blue
        // then it should fail back to red
        try {
            counter.hit();
            fail("Expected javax.ejb.EJBException");
        } catch (final javax.ejb.EJBException e) {
        }

        // there should be no hits on any server, both connection pools are fully busy
        assertEquals(expected, hits);

        resume.countDown(); // go
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        for (final ServiceDaemon daemon : daemons) {
            daemon.stop();
        }

        OpenEJB.destroy();
    }

    private final List<ServiceDaemon> daemons = new ArrayList<ServiceDaemon>();

    protected void setup(final int statelessPoolSize, final int connectionPoolSize) throws Exception {
        final Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());

        System.setProperty(org.apache.openejb.client.SocketConnectionFactory.PROPERTY_POOL_SIZE, "" + connectionPoolSize);

        final EjbServer ejbServer = new EjbServer();
        ejbServer.init(new Properties());

        daemons.add(createServiceDaemon(connectionPoolSize, ejbServer, red));
        daemons.add(createServiceDaemon(connectionPoolSize, ejbServer, blue));

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);

        // containers
        final StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("PoolSize", "" + statelessPoolSize);
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

        String failoverURI = "failover:sticky:";
        failoverURI += "ejbd://127.0.0.1:" + daemons.get(0).getPort() + "?red,";
        failoverURI += "ejbd://127.0.0.1:" + daemons.get(1).getPort() + "?blue";

        final Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", failoverURI);
        final Context context = new InitialContext(props);
        counter = (Counter) context.lookup("CounterBeanRemote");

        hold.clear();
        hits.clear();
    }

    private ServiceDaemon createServiceDaemon(final int poolSize, final EjbServer ejbServer, final URI uri) throws ServiceException {
        final ServiceIdentifier serviceIdentifier = new ServiceIdentifier(ejbServer, uri);
        final KeepAliveServer keepAliveServer = new KeepAliveServer(serviceIdentifier);
        final ServicePool pool = new ServicePool(keepAliveServer, poolSize);
        final ServiceDaemon daemon = new ServiceDaemon(pool, 0, "localhost");
        daemon.start();
        return daemon;
    }

    public static ThreadLocal<URI> host = new ThreadLocal<URI>();

    public static class ServiceIdentifier extends ServerServiceFilter {

        private final URI me;

        public ServiceIdentifier(final ServerService service, final URI me) {
            super(service);
            this.me = me;
        }

        @Override
        public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
            synchronized (hits) {
                hits.add(me);
            }
            host.set(me);
            try {
                super.service(in, out);
            } finally {
                host.remove();
            }
        }
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

        URI hit();
    }

    @Remote
    public static interface RemoteCounter extends Counter {

    }

    @Stateless
    public static class CounterBean implements Counter, RemoteCounter {

        public static AtomicInteger instances = new AtomicInteger();

        private int count;

        public CounterBean() {
            count = instances.incrementAndGet();
        }

        public int count() {
            return instances.get();
        }

        public URI hit() {
            final URI uri = host.get();

            if (hold.contains(uri)) {
                try {
                    paused.countDown();
                    resume.await();
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                }
            }

            return uri;
        }

        public void init() {

        }

        public void destroy() {

        }
    }
}
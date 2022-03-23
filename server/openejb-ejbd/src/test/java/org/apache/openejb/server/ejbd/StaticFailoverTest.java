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

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version $Rev$ $Date$
 */
public class StaticFailoverTest extends TestCase {

    private Counter counter;

    private static CountDownLatch resume;
    private static CountingLatch paused;
    private static final URI red = URI.create("red");
    private static final URI blue = URI.create("blue");

    public static List<URI> hits = new ArrayList<URI>();
    public static List<URI> hold = new ArrayList<URI>();

    public void testStatelessBeanTimeout() throws Exception {

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());

        System.setProperty(org.apache.openejb.client.SocketConnectionFactory.PROPERTY_POOL_SIZE, "" + 20);

        EjbServer ejbServer = new EjbServer();
        ejbServer.init(new Properties());

        daemons.add(createServiceDaemon(20, ejbServer, red));
        daemons.add(createServiceDaemon(20, ejbServer, blue));

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("PoolSize", "" + 10);
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

        String failoverURI = "failover:roundrobin:";
        failoverURI += "ejbd://127.0.0.1:" + daemons.get(0).getPort() + "?red,";
        failoverURI += "ejbd://127.0.0.1:" + daemons.get(1).getPort() + "?blue";

        Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", failoverURI);
        Context context = new InitialContext(props);
        counter = (Counter) context.lookup("CounterBeanRemote");

        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
        System.out.println(counter.hit());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        for (ServiceDaemon daemon : daemons) {
            daemon.stop();
        }

        OpenEJB.destroy();
    }

    private final List<ServiceDaemon> daemons = new ArrayList<ServiceDaemon>();

    private ServiceDaemon createServiceDaemon(int poolSize, EjbServer ejbServer, URI uri) throws ServiceException {
        ServiceIdentifier serviceIdentifier = new ServiceIdentifier(ejbServer, uri);
        KeepAliveServer keepAliveServer = new KeepAliveServer(serviceIdentifier);
        ServicePool pool = new ServicePool(keepAliveServer, poolSize);
        ServiceDaemon daemon = new ServiceDaemon(pool, 0, "localhost");
        daemon.start();
        return daemon;
    }

    public static ThreadLocal<URI> host = new ThreadLocal<URI>();

    public static class ServiceIdentifier extends ServerServiceFilter {

        private final URI me;

        public ServiceIdentifier(ServerService service, URI me) {
            super(service);
            this.me = me;
        }

        @Override
        public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
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

    private static void comment(String x) {
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
            URI uri = host.get();

            if (hold.contains(uri)) {
                try {
                    paused.countDown();
                    resume.await();
                } catch (InterruptedException e) {
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
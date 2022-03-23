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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServerServiceFilter;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServiceException;

import jakarta.ejb.EJBException;
import jakarta.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class FailoverTest extends TestCase {

    private static DiscoveryAgent agent;

    public void _testCleanShutdown() throws Exception {

        agent = new TestAgent();

        try {
            //            Properties initProps = System.getProperties();
            final Properties initProps = new Properties();
            initProps.setProperty("openejb.deployments.classpath.include", "");
            initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
            OpenEJB.init(initProps, new ServerFederation());
        } catch (Exception e) {
        }
        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(Target.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        SystemInstance.get().setComponent(DiscoveryAgent.class, agent);

        final ServerService red = server(Host.RED);
        final ServerService blue = server(Host.BLUE);
        final ServerService green = server(Host.GREEN);

        red.start();
        blue.start();
        green.start();

        final TargetRemote target = getBean(red);

        assertEquals(Host.RED, target.getHost());

        red.stop();

        assertEquals(Host.BLUE, target.getHost());

        blue.stop();

        assertEquals(Host.GREEN, target.getHost());

        green.stop();

        try {
            target.getHost();
            fail("EJBException should have been thrown");
        } catch (EJBException e) {
            // pass
        }

        red.start();

        assertEquals(Host.RED, target.getHost());

    }

    public void testCrash() throws Exception {
        agent = new TestAgent();

        try {
            //            Properties initProps = System.getProperties();
            final Properties initProps = new Properties();
            initProps.setProperty("openejb.deployments.classpath.include", "");
            initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
            OpenEJB.init(initProps, new ServerFederation());
        } catch (Exception e) {
        }
        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(Target.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        SystemInstance.get().setComponent(DiscoveryAgent.class, agent);

        final ServerService red = server(Host.RED);
        final ServerService blue = server(Host.BLUE);
        final ServerService green = server(Host.GREEN);

        red.start();
        blue.start();
        green.start();

        final TargetRemote target = getBean(red);

        assertEquals(Host.GREEN, target.kill(Host.RED, Host.BLUE).host);
        assertEquals(Host.GREEN, target.getHost());

        red.stop();
        blue.stop();
        green.stop();

        try {
            target.getHost();
            fail("EJBException should have been thrown");
        } catch (EJBException e) {
            // pass
        }

        red.start();

        assertEquals(Host.RED, target.getHost());

        red.stop();
        OpenEJB.destroy();
    }

    private TargetRemote getBean(final ServerService server) throws NamingException, IOException, OpenEJBException {
        final int port = server.getPort();

        // good creds
        final Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "ejbd://localhost:" + port + "/RED");
        System.setProperty("openejb.client.keepalive", "ping_pong");
        System.setProperty("openejb.client.requestretry", "true");
        final Context context = new InitialContext(props);
        return (TargetRemote) context.lookup("TargetRemote");
    }

    private ServerService server(final Host host) throws Exception {
        ServerService server = new EjbServer();

        server = new HostFilter(server, host);

        server = new ServiceDaemon(server, 0, "localhost");

        server = new AgentFilter(server, agent, host);

        server.init(new Properties());

        return server;
    }

    // Simple single-threaded version, way easier on testing
    public static class TestAgent implements DiscoveryAgent {

        private final List<DiscoveryListener> listeners = new ArrayList<DiscoveryListener>();

        @Override
        public void registerService(final URI serviceUri) throws IOException {
            for (final DiscoveryListener listener : listeners) {
                listener.serviceAdded(serviceUri);
            }
        }

        @Override
        public void reportFailed(final URI serviceUri) throws IOException {
        }

        @Override
        public void setDiscoveryListener(final DiscoveryListener listener) {
            listeners.add(listener);
        }

        @Override
        public void unregisterService(final URI serviceUri) throws IOException {
            for (final DiscoveryListener listener : listeners) {
                listener.serviceRemoved(serviceUri);
            }
        }

    }

    public static enum Host {
        RED,
        BLUE,
        GREEN
    }

    public static final ThreadLocal<Host> host = new ThreadLocal<Host>();

    public static Socket serverSideSocket;

    public static class AgentFilter extends ServerServiceFilter {

        private final Host host;
        private final DiscoveryAgent agent;
        private URI uri;

        public AgentFilter(final ServerService service, final DiscoveryAgent agent, final Host host) {
            super(service);
            this.agent = agent;
            this.host = host;
        }

        @Override
        public void start() throws ServiceException {
            super.start();
            try {
                uri = new URI("ejb:ejbd://localhost:" + getPort() + "/" + host);
                agent.registerService(uri);
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

        @Override
        public void stop() throws ServiceException {
            super.stop();
            try {
                agent.unregisterService(uri);
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    public static class HostFilter extends ServerServiceFilter {

        private final Host me;

        public HostFilter(final ServerService service, final Host me) {
            super(service);
            this.me = me;
        }

        @Override
        public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
            try {
                host.set(me);
                super.service(in, out);
            } finally {
                host.remove();
            }
        }

        @Override
        public void service(final Socket socket) throws ServiceException, IOException {
            serverSideSocket = socket;
            try {
                host.set(me);
                super.service(socket);
            } finally {
                host.remove();
            }
        }
    }

    public static class Target implements TargetRemote {

        @Override
        public Host getHost() {
            return host.get();
        }

        @Override
        public Wrapper kill(final Host... hosts) {
            final Host host = getHost();
            for (final Host h : hosts) {
                if (h == host) {
                    return new Wrapper(host, serverSideSocket);
                }
            }
            return new Wrapper(host, null);
        }
    }

    public static class Wrapper implements Serializable {

        private static final long serialVersionUID = 4604591462681914507L;
        transient Socket socket;
        private final Host host;

        public Wrapper(final Host host, final Socket socket) {
            this.host = host;
            this.socket = socket;
        }

        private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            if (socket != null) {
                socket.close();
            }
        }
    }

    @Remote
    public static interface TargetRemote {

        Host getHost();

        Wrapper kill(Host... hosts);
    }
}

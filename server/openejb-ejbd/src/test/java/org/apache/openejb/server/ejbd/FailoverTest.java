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

import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class FailoverTest extends TestCase {

    private static DiscoveryAgent agent = new TestAgent();

    public void test() throws Exception {

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());

        SystemInstance.get().setComponent(DiscoveryAgent.class, agent);

        ServerService red = server(Host.RED);
        ServerService blue = server(Host.BLUE);
        ServerService green = server(Host.GREEN);

        red.start();
        blue.start();
        green.start();

        TargetRemote target = getBean(red);

        assertEquals(Host.RED, target.getHost());

        red.stop();

        assertEquals(Host.BLUE, target.getHost());

        blue.stop();

        assertEquals(Host.GREEN, target.getHost());
    }

    private TargetRemote getBean(ServerService server) throws NamingException, IOException, OpenEJBException {
        int port = server.getPort();

        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        ConfigurationFactory config = new ConfigurationFactory();

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(Target.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        // good creds
        Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "ejbd://localhost:" + port + "/RED");
        System.setProperty("openejb.client.keepalive", "ping_pong");
        Context context = new InitialContext(props);
        TargetRemote target = (TargetRemote) context.lookup("TargetRemote");
        return target;
    }

    private ServerService server(Host host) throws Exception {
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

        public void registerService(URI serviceUri) throws IOException {
            for (DiscoveryListener listener : listeners) {
                listener.serviceAdded(serviceUri);
            }
        }

        public void reportFailed(URI serviceUri) throws IOException {
        }

        public void setDiscoveryListener(DiscoveryListener listener) {
            listeners.add(listener);
        }

        public void unregisterService(URI serviceUri) throws IOException {
            for (DiscoveryListener listener : listeners) {
                listener.serviceRemoved(serviceUri);
            }
        }

    }

    public static enum Host {
        RED, BLUE, GREEN;
    }

    public static ThreadLocal<Host> host = new ThreadLocal<Host>();


    public static class AgentFilter extends ServerServiceFilter {
        private final Host host;
        private final DiscoveryAgent agent;
        private URI uri;

        public AgentFilter(ServerService service, DiscoveryAgent agent, Host host) {
            super(service);
            this.agent = agent;
            this.host = host;
        }

        public void start() throws ServiceException {
            super.start();
            try {
                uri = new URI("ejb:ejbd://localhost:" + getPort() + "/" + host);
                agent.registerService(uri);
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

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

        public HostFilter(ServerService service, Host me) {
            super(service);
            this.me = me;
        }

        public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
            try {
                host.set(me);
                super.service(in, out);
            } finally {
                host.remove();
            }
        }

        public void service(Socket socket) throws ServiceException, IOException {
            try {
                host.set(me);
                super.service(socket);
            } finally {
                host.remove();
            }
        }
    }

    public static class Target implements TargetRemote {
        public Host getHost() {
            return host.get();
        }
    }

    @Remote
    public static interface TargetRemote {
        Host getHost();
    }
}

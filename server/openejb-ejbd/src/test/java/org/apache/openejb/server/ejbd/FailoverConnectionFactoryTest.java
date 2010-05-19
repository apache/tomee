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
import org.apache.openejb.client.ClusterMetaData;
import org.apache.openejb.client.Connection;
import org.apache.openejb.client.ConnectionManager;
import org.apache.openejb.client.ConnectionStrategy;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.client.StickyConnectionStrategy;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;

import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Rev$ $Date$
 */
public class FailoverConnectionFactoryTest extends TestCase {

    public void test() throws Exception {

        ConnectionManager.registerStrategy("test", new TestConnectionStrategy());
        EjbServer ejbServer = new EjbServer();

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServicePool pool = new ServicePool(ejbServer, "ejbd", 10);
        ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        int port = serviceDaemon.getPort();

        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        ConfigurationFactory config = new ConfigurationFactory();

        EjbModule ejbModule = new EjbModule(new EjbJar(), new OpenejbJar());
        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));
        EjbDeployment deployment = openejbJar.addEjbDeployment(statelessBean);
        deployment.getProperties().put("openejb.client.connection.strategy", "test");

        assembler.createApplication(config.configureApplication(ejbModule));

        Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "failover:sticky:ejbd://agwdt:9999,ejbd://127.0.0.1:" + port);
        Context context = new InitialContext(props);

        Widget remote = (Widget) context.lookup("WidgetBeanRemote");

        assertFalse(TestConnectionStrategy.called.get());

        remote.echo("foo");

        assertTrue(TestConnectionStrategy.called.get());
    }


    @Remote
    public static interface Widget {
        public Object echo(Object o);
    }

    public static class WidgetBean implements Widget {
        public Object echo(Object o) {
            return o;
        }
    }

    public static class TestConnectionStrategy implements ConnectionStrategy {
        public static final AtomicBoolean called = new AtomicBoolean();

        private final StickyConnectionStrategy strategy = new StickyConnectionStrategy();

        public Connection connect(ClusterMetaData cluster, ServerMetaData server) throws IOException {
            called.set(true);
            return strategy.connect(cluster, server);
        }
    }
}
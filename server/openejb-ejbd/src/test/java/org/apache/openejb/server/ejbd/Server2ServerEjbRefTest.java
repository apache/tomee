/*
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

import org.apache.openejb.server.ServiceManager;
import org.junit.Assert;
import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.JndiContextInfo;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.JndiProvider;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Server2ServerEjbRefTest extends TestCase {

    public void test() throws Exception {

        final Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());

        final ServiceDaemon blue = server();
        final ServiceDaemon orange = server();

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final JndiProvider jndiProvider = new JndiProvider("orange");
        final Properties p = jndiProvider.getProperties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, "ejbd://localhost:" + orange.getPort());

        final JndiContextInfo contextInfo = config.configureService(jndiProvider, JndiContextInfo.class);
        assembler.createExternalContext(contextInfo);

        {// Create the "Orange" bean
            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(OrangeBean.class));

            assembler.createApplication(config.configureApplication(ejbJar));

            // Lets look it up the normal way to be sure it can work
            final InitialContext initialContext = new InitialContext(jndiProvider.getProperties());
            final OrangeRemote orangeBeanRemote = (OrangeRemote) initialContext.lookup("OrangeBeanRemote");
            assertNotNull(orangeBeanRemote);
        }

        {// Create the "Blue" bean
            final EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(BlueBean.class));

            assembler.createApplication(config.configureApplication(ejbJar));

            // Lets look it up the normal way to be sure it can work
            final Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:" + blue.getPort());
            final InitialContext initialContext = new InitialContext(properties);
            final BlueRemote blueBeanRemote = (BlueRemote) initialContext.lookup("BlueBeanRemote");
            assertNotNull(blueBeanRemote);
            blueBeanRemote.hasOrangeRemote();
        }

        blue.stop();
        orange.stop();
        OpenEJB.destroy();
    }

    private ServiceDaemon server() throws Exception {
        final EjbServer ejbServer = new EjbServer();
        ejbServer.init(new Properties());

        final ServicePool pool = new ServicePool(ejbServer, 10, 5000, true);
        final ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        return serviceDaemon;
    }

    public static class OrangeBean implements OrangeRemote {

        @Override
        public String echo(final String message) {
            return new StringBuilder(message).reverse().toString();
        }
    }

    @Remote
    public static interface OrangeRemote {

        public String echo(String message);
    }

    public static class BlueBean implements BlueRemote {

        @EJB(mappedName = "jndi:ext://orange/OrangeBeanRemote")
        private OrangeRemote orangeRemote;

        @Resource(lookup = "openejb:remote_jndi_contexts/orange")
        private Context ctx;

        @Override
        public void hasOrangeRemote() {
            Assert.assertNotNull("orangeRemote is null", orangeRemote);
            assertEquals("olleh", orangeRemote.echo("hello"));
            try {
                final OrangeRemote bean = OrangeRemote.class.cast(ctx.lookup("OrangeBeanRemote"));
                assertNotNull(bean);
                assertEquals("olleh", bean.echo("hello"));
            } catch (final NamingException e) {
                fail(e.getMessage());
            }
        }
    }

    @Remote
    public static interface BlueRemote {

        public void hasOrangeRemote();
    }
}

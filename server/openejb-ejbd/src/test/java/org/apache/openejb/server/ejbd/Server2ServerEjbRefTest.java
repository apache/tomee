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

import junit.framework.Assert;
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

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Server2ServerEjbRefTest extends TestCase {

    public void test() throws Exception {

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());


        int blue = server();
        int orange = server();

        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        ConfigurationFactory config = new ConfigurationFactory();

        final JndiProvider jndiProvider = new JndiProvider("orange");
        final Properties p = jndiProvider.getProperties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        p.setProperty(Context.PROVIDER_URL, "ejbd://localhost:" + orange);

        final JndiContextInfo contextInfo = config.configureService(jndiProvider, JndiContextInfo.class);
        assembler.createExternalContext(contextInfo);


        {// Create the "Orange" bean
            EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(OrangeBean.class));

            assembler.createApplication(config.configureApplication(ejbJar));

            // Lets look it up the normal way to be sure it can work
            final InitialContext initialContext = new InitialContext(jndiProvider.getProperties());
            final OrangeRemote orangeBeanRemote = (OrangeRemote) initialContext.lookup("OrangeBeanRemote");
            assertNotNull(orangeBeanRemote);
        }

        {// Create the "Blue" bean
            EjbJar ejbJar = new EjbJar();
            ejbJar.addEnterpriseBean(new StatelessBean(BlueBean.class));

            assembler.createApplication(config.configureApplication(ejbJar));

            // Lets look it up the normal way to be sure it can work
            final Properties properties = new Properties();
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
            properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:" + blue);
            final InitialContext initialContext = new InitialContext(properties);
            final BlueRemote blueBeanRemote = (BlueRemote) initialContext.lookup("BlueBeanRemote");
            assertNotNull(blueBeanRemote);
            blueBeanRemote.hasOrangeRemote();
        }


    }

    private int server() throws Exception {
        EjbServer ejbServer = new EjbServer();
        ejbServer.init(new Properties());

        ServicePool pool = new ServicePool(ejbServer, "ejbd", 10);
        ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        return serviceDaemon.getPort();
    }


    public static class OrangeBean implements OrangeRemote {

        @Override
        public String echo(String message) {
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

        public void hasOrangeRemote() {
            Assert.assertNotNull("orangeRemote is null", orangeRemote);
            assertEquals("olleh", orangeRemote.echo("hello"));
        }
    }

    @Remote
    public static interface BlueRemote {
        public void hasOrangeRemote();
    }
}

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
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;

import jakarta.annotation.Resource;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Remote;
import jakarta.ejb.RemoteHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class AppClientTest extends TestCase {

    public void test() throws Exception {

        final EjbServer ejbServer = new EjbServer();

        final Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        final ServicePool pool = new ServicePool(ejbServer, 10);
        final ServiceDaemon serviceDaemon = new ServiceDaemon(pool, 0, "localhost");
        serviceDaemon.start();

        int port = serviceDaemon.getPort();

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final EjbModule ejbModule = new EjbModule(new EjbJar("testejbmodule"), new OpenejbJar());
        final EjbJar ejbJar = ejbModule.getEjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(Orange.class));

        final ClassLoader loader = this.getClass().getClassLoader();

        final ClientModule clientModule = new ClientModule(new ApplicationClient(), loader, "orange-client", OrangeAppClient.class.getName(), "orange-client");

        final AppModule appModule = new AppModule(loader, "testapp");

        appModule.getClientModules().add(clientModule);
        appModule.getEjbModules().add(ejbModule);

        assembler.createApplication(config.configureApplication(appModule));

        final Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
        props.put("openejb.client.moduleId", "orange-client");

        Context context = new InitialContext(props);

        final Object home = context.lookup("comp/env/home");
        assertTrue(home instanceof OrangeHome);

        OrangeHome orangeHome = (OrangeHome) home;
        final OrangeRemote orangeRemote = orangeHome.create();
        assertEquals("bat", orangeRemote.echo("tab"));

        final Object business = context.lookup("comp/env/business");
        assertTrue(business instanceof OrangeBusinessRemote);
        OrangeBusinessRemote orangeBusinessRemote = (OrangeBusinessRemote) business;
        assertEquals("nap", orangeBusinessRemote.echo("pan"));

        final Object dataSourceObject = context.lookup("comp/env/datasource");
        assertTrue(dataSourceObject instanceof DataSource);
        //        DataSource dataSource = (DataSource) dataSourceObject;
        //        assertEquals("nap", orangeBusinessRemote.echo("pan"));

        props.put("openejb.client.moduleId", "openejb/global");
        context = new InitialContext(props);

        final Object global = context.lookup("global/testapp/testejbmodule/Orange!" + OrangeBusinessRemote.class.getName());
        assertTrue(global instanceof OrangeBusinessRemote);
        OrangeBusinessRemote globalOrangeBusinessRemote = (OrangeBusinessRemote) global;
        assertEquals("nap", globalOrangeBusinessRemote.echo("pan"));

        serviceDaemon.stop();
        OpenEJB.destroy();
    }

    public static interface OrangeHome extends EJBHome {

        OrangeRemote create() throws RemoteException, CreateException;
    }

    public static interface OrangeRemote extends EJBObject {

        public String echo(String string) throws RemoteException;
    }

    public static interface OrangeBusinessRemote {

        public String echo(String string);

    }

    @RemoteHome(OrangeHome.class)
    @Remote(OrangeBusinessRemote.class)
    public static class Orange implements OrangeBusinessRemote {

        @Override
        public String echo(String string) {
            return new StringBuilder(string).reverse().toString();
        }
    }

    public static class OrangeAppClient {

        @Resource(name = "datasource")
        public static DataSource dataSource;

        @EJB(name = "home")
        public static OrangeHome orangeHome;

        @EJB(name = "business")
        public static OrangeBusinessRemote orangeBusinessRemote;

    }
}

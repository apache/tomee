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
package org.apache.openejb;

import junit.framework.TestCase;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;
import javax.ejb.Remote;
import java.util.Properties;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class JndiTest extends TestCase {
    public void test() throws Exception {
        EjbServer ejbServer = new EjbServer();

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServiceDaemon serviceDaemon = new ServiceDaemon(ejbServer, 0, "localhost");
        serviceDaemon.start();

        int port = serviceDaemon.getPort();

        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        ConfigurationFactory config = new ConfigurationFactory();

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("Orange", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("Apple", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("Peach", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("Pear", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("Plum", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("ejb/Orange", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("ejb/Apple", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("ejb/Peach", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("ejb/Pear", Fruit.class));
        ejbJar.addEnterpriseBean(new StatelessBean("ejb/Plum", Fruit.class));
        assembler.createApplication(config.configureApplication(ejbJar));

        try {

            // good creds
            Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
            Context context = new InitialContext(props);

            assertNameClassPair(context.list(""));
            assertNameClassPair(context.list("ejb"));

            assertBindings(context.listBindings(""));
            assertBindings(context.listBindings("ejb"));

        } finally {
            serviceDaemon.stop();
            OpenEJB.destroy();
        }

    }

    private void assertNameClassPair(NamingEnumeration<NameClassPair> namingEnumeration) {
        assertNotNull("namingEnumeration", namingEnumeration);

        Map<String, String> map = new HashMap<String, String>();
        while (namingEnumeration.hasMoreElements()) {
            NameClassPair pair = namingEnumeration.nextElement();
            map.put(pair.getName(), pair.getClassName());
        }

        assertTrue("OrangeRemote", map.containsKey("OrangeRemote"));
        assertTrue("AppleRemote", map.containsKey("AppleRemote"));
        assertTrue("PeachRemote", map.containsKey("PeachRemote"));
        assertTrue("PearRemote", map.containsKey("PearRemote"));
        assertTrue("PlumRemote", map.containsKey("PlumRemote"));
    }

    private void assertBindings(NamingEnumeration<Binding> namingEnumeration) {
        assertNotNull("namingEnumeration", namingEnumeration);

        Map<String, Object> map = new HashMap<String, Object>();
        while (namingEnumeration.hasMoreElements()) {
            Binding pair = namingEnumeration.nextElement();
            map.put(pair.getName(), pair.getObject());
        }

        assertTrue("OrangeRemote", map.containsKey("OrangeRemote"));
        assertTrue("OrangeRemote is FruitRemote", map.get("OrangeRemote") instanceof FruitRemote);

        assertTrue("AppleRemote", map.containsKey("AppleRemote"));
        assertTrue("AppleRemote is FruitRemote", map.get("AppleRemote") instanceof FruitRemote);

        assertTrue("PeachRemote", map.containsKey("PeachRemote"));
        assertTrue("PeachRemote is FruitRemote", map.get("PeachRemote") instanceof FruitRemote);

        assertTrue("PearRemote", map.containsKey("PearRemote"));
        assertTrue("PearRemote is FruitRemote", map.get("PearRemote") instanceof FruitRemote);

        assertTrue("PlumRemote", map.containsKey("PlumRemote"));
        assertTrue("PlumRemote is FruitRemote", map.get("PlumRemote") instanceof FruitRemote);
    }

    @Remote
    public static interface FruitRemote {
    }

    public static class Fruit implements FruitRemote {
    }
}

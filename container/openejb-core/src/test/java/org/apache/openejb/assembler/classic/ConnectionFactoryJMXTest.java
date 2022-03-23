/**
 *
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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.monitoring.LocalMBeanServer;

import jakarta.annotation.Resource;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ConnectionFactoryJMXTest extends TestCase {

    public void test() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // Fake connection factory
        assembler.createResource(config.configureService("Default JMS Resource Adapter", ResourceInfo.class));
        final ResourceInfo resourceInfo = config.configureService("Default JMS Connection Factory", ResourceInfo.class);
        resourceInfo.id = "CF";
        resourceInfo.properties.setProperty("TransactionSupport", "xa");
        resourceInfo.properties.setProperty("MaxConnections", "5");
        assembler.createResource(resourceInfo);

        // generate ejb jar application
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("fakeBean", FakeStatelessBean.class));
        final EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "FakeEjbJar", "fake.jar", ejbJar, null);

        // configure and deploy it
        final EjbJarInfo info = config.configureApplication(ejbModule);
        assembler.createEjbJar(info);

        check(new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=ConnectionFactory,name=CF"), 0, 0);

        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        final FakeStateless fakeBeanLocal = (FakeStateless) new InitialContext(p).lookup("fakeBeanLocal");
        fakeBeanLocal.doIt();

        OpenEJB.destroy();

        // ensure the bean is removed when the resource is undeployed
        assertFalse(LocalMBeanServer.get().isRegistered(new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=ConnectionFactory,name=CF")));
    }

    private static void check(ObjectName on, final int connectionCount, final int idleCount) throws InstanceNotFoundException, IntrospectionException, ReflectionException, MBeanException, AttributeNotFoundException {
        assertNotNull(LocalMBeanServer.get().getMBeanInfo(on));

        assertEquals(5000, LocalMBeanServer.get().getAttribute(on, "BlockingTimeoutMilliseconds"));
        assertEquals(connectionCount, LocalMBeanServer.get().getAttribute(on, "ConnectionCount"));
        assertEquals(idleCount, LocalMBeanServer.get().getAttribute(on, "IdleConnectionCount"));
        assertEquals(15, LocalMBeanServer.get().getAttribute(on, "IdleTimeoutMinutes"));
        assertEquals(false, LocalMBeanServer.get().getAttribute(on, "MatchAll"));
        assertEquals(10, LocalMBeanServer.get().getAttribute(on, "MaxSize"));
        assertEquals(0, LocalMBeanServer.get().getAttribute(on, "MinSize"));
        assertEquals("CF", LocalMBeanServer.get().getAttribute(on, "Name"));
        assertEquals(1, LocalMBeanServer.get().getAttribute(on, "PartitionCount"));
        assertEquals(10, LocalMBeanServer.get().getAttribute(on, "PartitionMaxSize"));
        assertEquals(0, LocalMBeanServer.get().getAttribute(on, "PartitionMinSize"));
        assertEquals("none", LocalMBeanServer.get().getAttribute(on, "PartitionStrategy"));
        assertEquals("xa", LocalMBeanServer.get().getAttribute(on, "TxSupport"));
    }

    public interface FakeStateless {
        public void doIt();
    }

    public static class FakeStatelessBean implements FakeStateless {

        @Resource
        private ConnectionFactory cf;

        @Override
        public void doIt() {
            try {
                final Connection connection = cf.createConnection();

                // check we see the connection and it is not idle
                check(
                        new ObjectName("openejb.management:J2EEServer=openejb,J2EEApplication=<empty>,j2eeType=ConnectionFactory,name=CF"),
                        1,
                        0
                );

                connection.close();
            } catch (Exception e) {
                fail("Unexpected exception thrown " + e);
            }


        }
    }
}

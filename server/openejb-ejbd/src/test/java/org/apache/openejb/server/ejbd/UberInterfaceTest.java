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
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServicePool;
import org.apache.openejb.spi.ContainerSystem;

import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.jws.WebService;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.util.Properties;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class UberInterfaceTest extends TestCase {

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

        final int port = serviceDaemon.getPort();

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        final ConfigurationFactory config = new ConfigurationFactory();

        final EjbJar ejbJar = new EjbJar();
        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(SuperBean.class));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        final EnterpriseBeanInfo beanInfo = ejbJarInfo.enterpriseBeans.get(0);

        assertEquals(asList(Everything.class.getName()), beanInfo.businessLocal);
        assertEquals(asList(Everything.class.getName()), beanInfo.businessRemote);

        assembler.createApplication(ejbJarInfo);

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext deployment = containerSystem.getBeanContext(beanInfo.ejbDeploymentId);

        assertEquals(asList(Everything.class), deployment.getBusinessLocalInterfaces());
        assertEquals(asList(Everything.class), deployment.getBusinessRemoteInterfaces());

        { // remote invoke
            final Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
            final Context context = new InitialContext(props);

            final Everything remote = (Everything) context.lookup("SuperBeanRemote");

            final Reference reference = new Reference("test");

            assertEquals(reference, remote.echo(reference));
            assertNotSame(reference, remote.echo(reference)); // pass by value
        }

        { // local invoke
            final Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.core.LocalInitialContextFactory");
            final Context context = new InitialContext(props);

            final Everything local = (Everything) context.lookup("SuperBeanLocal");

            final Reference reference = new Reference("test");

            assertEquals(reference, local.echo(reference));
            assertSame(reference, local.echo(reference)); // pass by reference
        }

        serviceDaemon.stop();
        OpenEJB.destroy();
    }

    @Local
    @Remote
    @WebService
    public static interface Everything {

        public Object echo(Object o);
    }

    public static class SuperBean implements Everything {

        @Override
        public Object echo(final Object o) {
            return o;
        }
    }

    public static class Reference implements Serializable {

        private static final long serialVersionUID = -7739317717965024181L;
        private final String value;

        public Reference(final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Reference value1 = (Reference) o;

            if (!value.equals(value1.value)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

}

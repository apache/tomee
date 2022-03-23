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
package org.apache.openejb.config;

import junit.framework.TestCase;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;

import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.jws.WebService;
import javax.naming.InitialContext;
import java.io.Serializable;

import static java.util.Arrays.asList;

public class UberInterfaceTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(SuperBean.class));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        final EnterpriseBeanInfo beanInfo = ejbJarInfo.enterpriseBeans.get(0);

        assertEquals(asList(Everything.class.getName()), beanInfo.businessLocal);
        assertEquals(asList(Everything.class.getName()), beanInfo.businessRemote);
        assertEquals(Everything.class.getName(), beanInfo.serviceEndpoint);

        assembler.createApplication(ejbJarInfo);

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext deployment = containerSystem.getBeanContext(beanInfo.ejbDeploymentId);

        assertEquals(asList(Everything.class), deployment.getBusinessLocalInterfaces());
        assertEquals(asList(Everything.class), deployment.getBusinessRemoteInterfaces());
        assertEquals(Everything.class, deployment.getServiceEndpointInterface());

        final InitialContext context = new InitialContext();

        final Everything local = (Everything) context.lookup("SuperBeanLocal");
        final Everything remote = (Everything) context.lookup("SuperBeanRemote");

        final Reference reference = new Reference("test");

        assertEquals(reference, local.echo(reference));
        assertSame(reference, local.echo(reference)); // pass by reference

        assertEquals(reference, remote.echo(reference));
        assertNotSame(reference, remote.echo(reference)); // pass by value
    }


    @Local
    @Remote
    @WebService
    public static interface Everything {
        public Object echo(Object o);
    }

    public static class SuperBean implements Everything {
        public Object echo(final Object o) {
            return o;
        }
    }

    public static class Reference implements Serializable {
        private final String value;

        public Reference(final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Reference value1 = (Reference) o;

            if (!value.equals(value1.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

}

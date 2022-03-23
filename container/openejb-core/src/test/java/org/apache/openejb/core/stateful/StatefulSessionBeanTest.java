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
package org.apache.openejb.core.stateful;

import junit.framework.TestCase;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.junit.AfterClass;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import javax.naming.InitialContext;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class StatefulSessionBeanTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        statefulContainerInfo.properties.setProperty("PoolSize", "0");
        statefulContainerInfo.properties.setProperty("BulkPassivate", "1");
        statefulContainerInfo.properties.setProperty("Frequency", "0");
        assembler.createContainer(statefulContainerInfo);

        assembler.createApplication(config.configureApplication(buildTestApp()));

        StatefulSessionBeanTest.calls.clear();

        final InitialContext ctx = new InitialContext();
        final TargetHome home = (TargetHome) ctx.lookup("TargetBeanRemoteHome");
        assertNotNull(home);

        final Target target = home.create("Fuzz");
        assertNotNull(target);

        final String name = target.getName();
        assertEquals("Fuzz", name);

        target.remove();

        assertCalls(Call.values());

    }

    private void assertCalls(final Call... expectedCalls) {
        final List expected = Arrays.asList(expectedCalls);
        assertEquals(StatefulSessionBeanTest.join("\n", expected), StatefulSessionBeanTest.join("\n", StatefulSessionBeanTest.calls));
    }

    public EjbModule buildTestApp() {
        final EjbJar ejbJar = new EjbJar();

        final StatefulBean bean = ejbJar.addEnterpriseBean(new StatefulBean(StatefulSessionBeanTest.TargetBean.class));
        bean.setHomeAndRemote(TargetHome.class, Target.class);

        return new EjbModule(this.getClass().getClassLoader(), this.getClass().getSimpleName(), "test", ejbJar, null);
    }

    public static List<Call> calls = new ArrayList<Call>();

    public static enum Call {
        // construction
        Constructor, SetSessionContext, EjbCreate, EjbPassivate1,
        // business method
        EjbActivate1, BusinessMethod, EjbPassivate2,
        // remove
        EjbActivate2, EjbRemove
    }

    public static class TargetBean implements SessionBean {

        private int activates = 0;
        private int passivates = 0;

        private String name;

        public TargetBean() {
            calls.add(Call.Constructor);
        }

        @Override
        public void setSessionContext(final SessionContext sessionContext) {
            calls.add(Call.SetSessionContext);
        }

        public void ejbCreate(final String name) throws CreateException {
            calls.add(Call.EjbCreate);
            this.name = name;
        }

        public String getName() {
            calls.add(Call.BusinessMethod);
            return name;
        }

        @Override
        public void ejbActivate() throws EJBException, RemoteException {
            calls.add(Enum.valueOf(Call.class, "EjbActivate" + (++activates)));
        }

        @Override
        public void ejbPassivate() throws EJBException, RemoteException {
            calls.add(Enum.valueOf(Call.class, "EjbPassivate" + (++passivates)));
        }

        @Override
        public void ejbRemove() throws EJBException, RemoteException {
            calls.add(Call.EjbRemove);
        }
    }

    public static interface TargetHome extends EJBHome {
        Target create(String name) throws RemoteException, CreateException;
    }

    public static interface Target extends EJBObject {
        String getName();
    }

    private static String join(final String delimeter, final List items) {
        final StringBuilder sb = new StringBuilder();
        for (final Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }
}

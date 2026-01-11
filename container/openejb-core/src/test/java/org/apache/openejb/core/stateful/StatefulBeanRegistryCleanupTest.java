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
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.OpenEJBInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertNotEquals;

/**
 *
 * @version $Rev$ $Date$
 */
public class StatefulBeanRegistryCleanupTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, OpenEJBInitialContextFactory.class.getName());
//        System.setProperty("openejb.validation.output.level" , "VERBOSE");

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        final StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        assembler.createContainer(statefulContainerInfo);

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(MyBean.class));
        ejbJar.addEnterpriseBean(new StatefulBean(MyBean2.class));

        assembler.createApplication(config.configureApplication(ejbJar));
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        final Context context = new InitialContext();
        final MyBeanInterface myBean = (MyBeanInterface) context.lookup("MyBeanRemote");
        StatefulEjbObjectHandler handler = (StatefulEjbObjectHandler) Proxy.getInvocationHandler(myBean);
        ConcurrentMap reg = handler.getLiveHandleRegistry();

        myBean.cleanup();
        assertTrue("Live handle registry should be empty after removal", reg.isEmpty());
    }

    public void testLiveHandleRegistryForSameBean() throws Exception {
        final Context context = new InitialContext();

        // Create two proxies/handles
        final MyBeanInterface bean1 = (MyBeanInterface) context.lookup("MyBeanRemote");
        final MyBeanInterface bean2 = (MyBeanInterface) context.lookup("MyBeanRemote");

        final StatefulEjbObjectHandler handler1 = (StatefulEjbObjectHandler) Proxy.getInvocationHandler(bean1);
        final StatefulEjbObjectHandler handler2 = (StatefulEjbObjectHandler) Proxy.getInvocationHandler(bean2);

        final ConcurrentMap<?, ?> reg1 = handler1.getLiveHandleRegistry();
        final ConcurrentMap<?, ?> reg2 = handler2.getLiveHandleRegistry();

        assertEquals("Both the registry references should be same", reg1, reg2);

        // Exercise the proxies to ensure handles are registered
        bean1.echo("a");
        bean2.echo("b");

        assertEquals("Registry size should have references for both the beans", 2, reg1.size());

        // Remove first bean and ensure its registry is cleared
        bean1.cleanup();
        assertEquals("Registry should have reference for only bean2 after bean1 removal", 1, reg1.size());

        // Remove second bean
        bean2.cleanup();
        assertEquals("Registry should be empty after removing both the beans", 0, reg1.size());
    }

    public void testLiveHandleRegistryForDifferentBeans() throws Exception {
        final Context context = new InitialContext();

        // Create two proxies/handles
        final MyBeanInterface bean1 = (MyBeanInterface) context.lookup("MyBeanRemote");
        final MyBeanInterface2 bean2 = (MyBeanInterface2) context.lookup("MyBean2Remote");

        final StatefulEjbObjectHandler handler1 = (StatefulEjbObjectHandler) Proxy.getInvocationHandler(bean1);
        final StatefulEjbObjectHandler handler2 = (StatefulEjbObjectHandler) Proxy.getInvocationHandler(bean2);

        final ConcurrentMap<?, ?> reg1 = handler1.getLiveHandleRegistry();
        final ConcurrentMap<?, ?> reg2 = handler2.getLiveHandleRegistry();

        assertNotEquals("Both the registry references should be different", reg1, reg2);

        // Exercise the proxies to ensure handles are registered
        bean1.echo("a");
        bean2.echo("b");

        assertEquals("Registry for bean1 should have single entry", 1, reg1.size());
        assertEquals("Registry for bean2 should have single entry", 1, reg2.size());

        // Remove first bean and ensure its registry is cleared
        bean1.cleanup();
        assertEquals("Registry for bean1 should be empty", 0, reg1.size());
        assertEquals("Registry for bean2 should have single entry", 1, reg2.size());

        // Remove second bean
        bean2.cleanup();
        assertEquals("Registry for bean1 should be empty", 0, reg1.size());
        assertEquals("Registry for bean2 should be empty", 0, reg2.size());
    }

    public interface MyBeanInterface {
        String echo(String string);

        @Remove
        void cleanup();
    }


    @Stateful
    @Remote
    public static class MyBean implements MyBeanInterface {

        @Override
        public String echo(final String string) {
            final StringBuilder sb = new StringBuilder(string);
            return sb.reverse().toString();
        }

        @Override
        public void cleanup() {
            System.out.println("cleaning up MyBean instance");
        }

    }


    public interface MyBeanInterface2 {
        String echo(String string);

        @Remove
        void cleanup();
    }


    @Stateful
    @Remote
    public static class MyBean2 implements MyBeanInterface2 {

        @Override
        public String echo(final String string) {
            final StringBuilder sb = new StringBuilder(string);
            return sb.reverse().toString();
        }

        @Override
        public void cleanup() {
            System.out.println("cleaning up MyBean instance");
        }

    }
}

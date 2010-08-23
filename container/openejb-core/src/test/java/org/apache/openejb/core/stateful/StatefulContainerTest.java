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
package org.apache.openejb.core.stateful;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Revision$ $Date$
 */
public class StatefulContainerTest extends TestCase {
    private List<Lifecycle> inTxExpectedLifecycle;
    private List expectedLifecycle;

    public void testBusinessLocalInterface() throws Exception {
        testBusinessLocalInterface(expectedLifecycle);
    }
    
    public void testBusinessLocalBeanInterface() throws Exception {
        List localbeanExpectedLifecycle = new ArrayList();
        localbeanExpectedLifecycle.addAll(expectedLifecycle);
        // can't avoid the extra constructor call
        localbeanExpectedLifecycle.add(4, Lifecycle.CONSTRUCTOR);

        testBusinessLocalBeanInterface(localbeanExpectedLifecycle);
    }

    public void testBusinessRemoteInterfaceInTx() throws Exception {
        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        transactionManager.begin();
        try {
            testBusinessRemoteInterface(inTxExpectedLifecycle);
        } finally {
            transactionManager.commit();
        }
    }

    protected void testBusinessLocalInterface(List expectedLifecycle) throws Exception {

        // Do a create...

        InitialContext ctx = new InitialContext();
        Object object = ctx.lookup("WidgetBeanLocal");

        assertTrue("instanceof widget", object instanceof Widget);

        Widget widget = (Widget) object;

        // Do a business method...
        Stack<Object> actual = widget.getLifecycle();
        assertNotNull("lifecycle", actual);

        // test app exception
        try {
            widget.throwAppException();
            fail("Expected application exception");
        } catch (SQLException e) {
            assertEquals("test", e.getMessage());
        }

        // Do another business method...
        widget.afterAppException();

        // Do a remove...
        widget.destroy();

        // Check the lifecycle of the bean
        assertEquals(StatefulContainerTest.join("\n", expectedLifecycle) , join("\n", WidgetBean.lifecycle));
    }

    protected void testBusinessLocalBeanInterface(List expectedLifecycle) throws Exception {

        // Do a create...

        InitialContext ctx = new InitialContext();
        Object object = ctx.lookup("WidgetBeanLocalBean");

        assertTrue("instanceof widget", object instanceof WidgetBean);

        WidgetBean widget = (WidgetBean) object;

        // Do a business method...
        Stack<Object> actual = widget.getLifecycle();
        assertNotNull("lifecycle", actual);

        // test app exception
        try {
            widget.throwAppException();
            fail("Expected application exception");
        } catch (SQLException e) {
            assertEquals("test", e.getMessage());
        }

        // Do another business method...
        widget.afterAppException();

        // Do a remove...
        widget.destroy();

        // Check the lifecycle of the bean

        assertEquals(StatefulContainerTest.join("\n", expectedLifecycle) , join("\n", WidgetBean.lifecycle));
    }

    public void testBusinessRemoteInterface() throws Exception {
        testBusinessRemoteInterface(expectedLifecycle);
    }

    public void testBusinessLocalInterfaceInTx() throws Exception {
        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        transactionManager.begin();
        try {
            testBusinessLocalInterface(inTxExpectedLifecycle);
        } finally {
            transactionManager.commit();
        }
    }

    public void testBusinessLocalBeanInterfaceInTx() throws Exception {
         List localbeanExpectedLifecycle = new ArrayList();
        localbeanExpectedLifecycle.addAll(inTxExpectedLifecycle);
        // can't avoid the extra constructor call
        localbeanExpectedLifecycle.add(3, Lifecycle.CONSTRUCTOR);

        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        transactionManager.begin();
        try {
            testBusinessLocalBeanInterface(localbeanExpectedLifecycle);
        } finally {
            transactionManager.commit();
        }
    }

    protected void testBusinessRemoteInterface(List expectedLifecycle) throws Exception {
        WidgetBean.lifecycle.clear();

        // Do a create...

        InitialContext ctx = new InitialContext();
        Object object = ctx.lookup("WidgetBeanRemote");

        assertTrue("instanceof widget", object instanceof RemoteWidget);

        RemoteWidget widget = (RemoteWidget) object;

        // Do a business method...
        Stack<Object> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle",lifecycle);
        assertNotSame("is copy", lifecycle, WidgetBean.lifecycle);

        // test app exception
        try {
            widget.throwAppException();
            fail("Expected application exception");
        } catch (SQLException e) {
            assertEquals("test", e.getMessage());
        }

        // Do another business method...
        widget.afterAppException();

        // Do a remove...
        widget.destroy();

        try {
            widget.destroy();
            fail("Calling a removed bean should not be possible");
        } catch (Exception e) {
        }

        // Check the lifecycle of the bean
        assertEquals(StatefulContainerTest.join("\n", expectedLifecycle) , join("\n", WidgetBean.lifecycle));
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatefulSessionContainerInfo statefulContainerInfo = config.configureService(StatefulSessionContainerInfo.class);
        statefulContainerInfo.properties.setProperty("PoolSize", "0");
        statefulContainerInfo.properties.setProperty("BulkPassivate", "1");
        statefulContainerInfo.properties.setProperty("Frequency", "0");
        assembler.createContainer(statefulContainerInfo);

        // Setup the descriptor information

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(WidgetBean.class));

        assembler.createApplication(config.configureApplication(ejbJar));

        WidgetBean.lifecycle.clear();

        expectedLifecycle = Arrays.asList(Lifecycle.values());
        inTxExpectedLifecycle = new ArrayList<Lifecycle>();
        for (Lifecycle lifecycle : Lifecycle.values()) {
            if (!lifecycle.name().startsWith("PRE_PASSIVATE") &&
                    !lifecycle.name().startsWith("POST_ACTIVATE")) {
                inTxExpectedLifecycle.add(lifecycle);
            }
        }
    }

    private static String join(String delimeter, List items){
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    @Local
    public static interface Widget {
        Stack<Object> getLifecycle();
        void throwAppException() throws SQLException;
        void afterAppException();
        void destroy();
    }

    @Remote
    public static interface RemoteWidget extends Widget {

    }

    public static enum Lifecycle {
        // construction
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, PRE_PASSIVATE1,
        // business method
        POST_ACTIVATE1, BUSINESS_METHOD, PRE_PASSIVATE2,
        // throw app exception
        POST_ACTIVATE2, PRE_PASSIVATE3,
        // business method after app exception
        POST_ACTIVATE3, PRE_PASSIVATE4,
        // remove
        POST_ACTIVATE4, REMOVE, PRE_DESTROY,
    }

    @LocalBean
    public static class WidgetBean implements Widget, RemoteWidget {
        private int activates = 0;
        private int passivates = 0;

        public static Stack<Object> lifecycle = new Stack<Object>();

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        public void throwAppException() throws SQLException {
            throw new SQLException("test");
        }

        public void afterAppException() {
        }

        @Resource
        public void setContext(SessionContext context){
            lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Object> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        @PostActivate
        public void activate(){
            String name = "POST_ACTIVATE" + (++activates);
            try {
                lifecycle.push(Enum.valueOf(Lifecycle.class, name));
            } catch (Exception e) {
                lifecycle.push(name);
            }
        }

        @PrePassivate
        public void passivate(){
            String name = "PRE_PASSIVATE" + (++passivates);
            try {
                lifecycle.push(Enum.valueOf(Lifecycle.class, name));
            } catch (Exception e) {
                lifecycle.push(name);
            }
        }

        @PostConstruct
        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        @PreDestroy
        public void predestroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }

        @Remove
        public void destroy() {
            lifecycle.push(Lifecycle.REMOVE);
        }
    }
}

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
package org.apache.openejb.core.singleton;

import junit.framework.TestCase;

import javax.naming.InitialContext;
import javax.ejb.SessionContext;
import javax.annotation.Resource;
import java.util.List;
import java.util.Arrays;
import java.util.Stack;
import java.util.ArrayList;

import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Empty;

/**
 * @version $Revision$ $Date$
 */
public class SingletonContainerTest extends TestCase {

    public void testPojoStyleBean() throws Exception {
        List expected = Arrays.asList(Lifecycle.values());
        InitialContext ctx = new InitialContext();

        {
            WidgetBean.lifecycle.clear();

            Object object = ctx.lookup("WidgetBeanLocal");

            assertTrue("instanceof widget", object instanceof Widget);

            Widget widget = (Widget) object;

            // Do a business method...
            Stack<Lifecycle> lifecycle = widget.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            assertEquals(join("\n", expected), join("\n", lifecycle));
        }{
            WidgetBean.lifecycle.clear();

            Object object = ctx.lookup("WidgetBeanLocalBean");

            assertTrue("instanceof widget", object instanceof WidgetBean);

            WidgetBean widget = (WidgetBean) object;

            // Do a business method...
            Stack<Lifecycle> lifecycle = widget.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertSame("lifecycle", lifecycle, WidgetBean.lifecycle);

           // Check the lifecycle of the bean
            assertEquals(Lifecycle.CONSTRUCTOR + "\n" + Lifecycle.BUSINESS_METHOD + "\n", join("\n", lifecycle));
        }
        {

            WidgetBean.lifecycle.clear();

            Object object = ctx.lookup("WidgetBeanRemote");

            assertTrue("instanceof widget", object instanceof RemoteWidget);

            RemoteWidget remoteWidget = (RemoteWidget) object;

            // Do a business method...
            Stack<Lifecycle> lifecycle = remoteWidget.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertNotSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            assertEquals(Lifecycle.BUSINESS_METHOD + "\n", join("\n", lifecycle));
        }
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
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

        // Setup the descriptor information

        SingletonBean bean = new SingletonBean(WidgetBean.class);
        bean.addBusinessLocal(Widget.class.getName());
        bean.addBusinessRemote(RemoteWidget.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");
        bean.setLocalBean(new Empty());

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        assembler.createApplication(config.configureApplication(ejbJar));

    }

    private static String join(String delimeter, List items) {
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
    }

    public static interface RemoteWidget extends Widget {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, BUSINESS_METHOD
    }

    public static class WidgetBean implements Widget, RemoteWidget {

        private static Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            WidgetBean.lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setSessionContext(SessionContext sessionContext) {
            WidgetBean.lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            WidgetBean.lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return WidgetBean.lifecycle;
        }

        public void init() {
            WidgetBean.lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void destroy() {
//            WidgetBean.lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }
}

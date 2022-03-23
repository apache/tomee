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
import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.SingletonSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import javax.naming.InitialContext;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
public class SingletonContainerTest extends TestCase {

    public void testPojoStyleBean() throws Exception {
        final List expected = Arrays.asList(Lifecycle.values());
        final InitialContext ctx = new InitialContext();

        {
            WidgetBean.lifecycle.clear();

            final Object object = ctx.lookup("WidgetBeanLocal");

            assertTrue("instanceof widget", object instanceof Widget);

            final Widget widget = (Widget) object;

            // Do a business method...
            final Stack<Lifecycle> lifecycle = widget.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            assertEquals(join("\n", expected), join("\n", lifecycle));
        }
        {
            WidgetBean.lifecycle.clear();

            final Object object = ctx.lookup("WidgetBeanLocalBean");

            assertTrue("instanceof widget", object instanceof WidgetBean);

            final WidgetBean widget = (WidgetBean) object;

            // Do a business method...
            final Stack<Lifecycle> lifecycle = widget.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            // assertEquals(Lifecycle.CONSTRUCTOR + "\n" + Lifecycle.BUSINESS_METHOD + "\n", join("\n", lifecycle));
            assertEquals(Lifecycle.BUSINESS_METHOD + "\n", join("\n", lifecycle));
        }
        {

            WidgetBean.lifecycle.clear();

            final Object object = ctx.lookup("WidgetBeanRemote");

            assertTrue("instanceof widget", object instanceof RemoteWidget);

            final RemoteWidget remoteWidget = (RemoteWidget) object;

            // Do a business method...
            final Stack<Lifecycle> lifecycle = remoteWidget.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertNotSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            assertEquals(Lifecycle.BUSINESS_METHOD + "\n", join("\n", lifecycle));
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        assembler.createContainer(config.configureService(SingletonSessionContainerInfo.class));

        // Setup the descriptor information

        final SingletonBean bean = new SingletonBean(WidgetBean.class);
        bean.addBusinessLocal(Widget.class.getName());
        bean.addBusinessRemote(RemoteWidget.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");
        bean.setLocalBean(new Empty());

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        assembler.createApplication(config.configureApplication(ejbJar));

    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    private static String join(final String delimeter, final List items) {
        final StringBuilder sb = new StringBuilder();
        for (final Object item : items) {
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

        private static final Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            WidgetBean.lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setSessionContext(final SessionContext sessionContext) {
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

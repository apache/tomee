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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
@RunWith(ApplicationComposer.class)
public class StatelessContainerTest extends TestCase {

    @EJB
    private WidgetBean localBean;

    @EJB
    private Widget local;

    @EJB
    private RemoteWidget remote;

    @Test
    public void testPojoStyleBean() throws Exception {
        final List expected = Arrays.asList(Lifecycle.values());

        {
            WidgetBean.lifecycle.clear();

            // Do a business method...
            final Stack<Lifecycle> lifecycle = local.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            assertEquals(join("\n", expected), join("\n", lifecycle));
        }
        {
            WidgetBean.lifecycle.clear();

            // Do a business method...
            final Stack<Lifecycle> lifecycle = localBean.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            final List localBeanExpected = new ArrayList(expected);
            assertEquals(join("\n", localBeanExpected), join("\n", lifecycle));
        }
        {

            WidgetBean.lifecycle.clear();

            // Do a business method...
            final Stack<Lifecycle> lifecycle = remote.getLifecycle();
            assertNotNull("lifecycle", lifecycle);
            assertNotSame("lifecycle", lifecycle, WidgetBean.lifecycle);

            // Check the lifecycle of the bean
            assertEquals(join("\n", expected), join("\n", lifecycle));
        }
    }

    @Configuration
    public Properties config() {
        final Properties properties = new Properties();
        properties.put("statelessContainer", "new://Container?type=STATELESS");
        properties.put("statelessContainer.TimeOut", "10");
        properties.put("statelessContainer.MaxSize", "0");
        properties.put("statelessContainer.StrictPooling", "false");

        return properties;
    }

    @Module
    public StatelessBean app() throws Exception {

        final StatelessBean bean = new StatelessBean(WidgetBean.class);
        bean.addBusinessLocal(Widget.class.getName());
        bean.addBusinessRemote(RemoteWidget.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");
        bean.setLocalBean(new Empty());

        return bean;
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
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    public static class WidgetBean implements Widget, RemoteWidget {

        private static final Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Resource
        public void setSessionContext(final SessionContext sessionContext) {
            lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void destroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }
}

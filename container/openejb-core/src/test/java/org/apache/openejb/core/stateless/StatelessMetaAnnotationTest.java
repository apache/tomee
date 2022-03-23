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
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Remote;
import jakarta.ejb.SessionContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @version $Revision$ $Date$
 */
//@RunWith(ApplicationComposer.class)
public class StatelessMetaAnnotationTest extends TestCase {

    @EJB
    private WidgetBean localBean;

    @EJB
    private Widget local;

    @EJB
    private RemoteWidget remote;

    @Test
    public void testNothing() {
    }

    //    @Test
    public void _testPojoStyleBean() throws Exception {
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
        return new StatelessBean(WidgetBean.class);
    }

    private static String join(final String delimeter, final List items) {
        final StringBuilder sb = new StringBuilder();
        for (final Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    @PassByReference
    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
    }

    @PassByValue
    public static interface RemoteWidget extends Widget {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    @NoInterfaceView
    public static class WidgetBean implements Widget, RemoteWidget {

        private static final Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        @Context
        public void setSessionContext(final Object o) {
            lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        @Start
        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        @Stop
        public void destroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }

    @Metatype
    @Retention(RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    public static @interface Metatype {
    }

    @Metatype
    @Retention(RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Start {
        public static interface $ {

            @Start
            @PostConstruct
            public void method();
        }
    }

    @Metatype
    @Retention(RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Stop {
        public static interface $ {

            @Stop
            @PreDestroy
            public void method();
        }
    }

    @Metatype
    @Resource(type = SessionContext.class, name = "context")
    @Retention(RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
    public static @interface Context {
    }


    @Metatype
    @Local
    @Retention(RUNTIME)
    @Target({ElementType.TYPE})
    public static @interface PassByReference {
    }


    @Metatype
    @Remote
    @Retention(RUNTIME)
    @Target({ElementType.TYPE})
    public static @interface PassByValue {
    }

    @Metatype
    @LocalBean
    @Retention(RUNTIME)
    @Target({ElementType.TYPE})
    public static @interface NoInterfaceView {
    }


}
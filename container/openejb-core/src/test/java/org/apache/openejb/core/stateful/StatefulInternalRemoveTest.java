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

import junit.framework.TestCase;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.Remote;
import jakarta.ejb.Remove;
import javax.naming.InitialContext;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */

@RunWith(ApplicationComposer.class)
public class StatefulInternalRemoveTest extends TestCase {

    @Module
    public StatefulBean beans() {
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        return new StatefulBean(Widget.class);
    }

    @Test
    public void testBusinessLocalInterface() throws Exception {
        Widget.lifecycle.clear();

        final WidgetLocal widgetLocal = (WidgetLocal) new InitialContext().lookup("WidgetLocal");

        widgetLocal.widget();

        final BeanContext.Removable removable = (BeanContext.Removable) widgetLocal;

        removable.$$remove();

        try {
            widgetLocal.widget();

            fail("The bean should have been removed");
        } catch (final NoSuchEJBException e) {
            // pass
        }

        // Check the lifecycle of the bean

        final Lifecycle[] expected = {
            Lifecycle.CONSTRUCTOR,
            Lifecycle.POST_CONSTRUCT,
            Lifecycle.BUSINESS_METHOD,
            Lifecycle.PRE_DESTROY};

        assertEquals(join("\n", Arrays.asList(expected)), join("\n", Widget.lifecycle));
    }

    public void testBusinessRemoteInterface() throws Exception {

    }

    public void testLocalBeanInterface() throws Exception {

    }

    public static enum Lifecycle {
        CONSTRUCTOR,
        POST_CONSTRUCT,
        BUSINESS_METHOD,
        REMOVE,
        PRE_DESTROY,
    }

    private static String join(final String delimeter, final List items) {
        final StringBuilder sb = new StringBuilder();
        for (final Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    @Local
    public static interface WidgetLocal {
        void destroy();

        void widget();
    }

    @Remote
    public static interface WidgetRemote extends WidgetLocal {

    }

    @LocalBean
    public static class Widget implements WidgetLocal, WidgetRemote {

        public static LinkedList<Object> lifecycle = new LinkedList<Object>();

        public Widget() {
            lifecycle.add(Lifecycle.CONSTRUCTOR);
        }

        public void widget() {
            lifecycle.add(Lifecycle.BUSINESS_METHOD);
        }

        @PostConstruct
        public void init() {
            lifecycle.add(Lifecycle.POST_CONSTRUCT);
        }

        @PreDestroy
        public void predestroy() {
            lifecycle.add(Lifecycle.PRE_DESTROY);
        }

        @Remove
        public void destroy() {
            lifecycle.add(Lifecycle.REMOVE);
        }
    }
}

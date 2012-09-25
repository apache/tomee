/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.unittests.intercept;

import java.util.List;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.intercept.InterceptedComponent;
import org.apache.webbeans.test.component.intercept.InterceptorWithSuperClassInterceptedComponent;
import org.apache.webbeans.test.component.intercept.MultipleInterceptedComponent;
import org.apache.webbeans.test.component.intercept.MultipleListOfInterceptedComponent;
import org.apache.webbeans.test.component.intercept.MultipleListOfInterceptedWithExcludeClassComponent;
import org.junit.Before;
import org.junit.Test;

public class EJBInterceptComponentTest extends TestContext
{

    public EJBInterceptComponentTest()
    {
        super(EJBInterceptComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testInterceptedComponent()
    {
        defineManagedBean(InterceptedComponent.class);
    }

    @Test
    public void testInterceptorCalls()
    {
        clear();
        defineManagedBean(InterceptedComponent.class);

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        List<AbstractOwbBean<?>> comps = getComponents();

        Object object = getManager().getInstance(comps.get(0));

        Assert.assertTrue(object instanceof InterceptedComponent);

        InterceptedComponent comp = (InterceptedComponent) object;
        Object s = comp.hello(null);

        Assert.assertEquals(new Integer(5), s);

        contextFactory.destroyRequestContext(null);
    }

    @Test
    public void testMultipleInterceptedComponent()
    {
        clear();
        defineManagedBean(MultipleInterceptedComponent.class);

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        List<AbstractOwbBean<?>> comps = getComponents();

        Object object = getManager().getInstance(comps.get(0));

        Assert.assertTrue(object instanceof MultipleInterceptedComponent);

        MultipleInterceptedComponent comp = (MultipleInterceptedComponent) object;
        Object obj = comp.intercepted();

        Assert.assertTrue(obj instanceof String[]);

        String[] arr = (String[]) obj;

        Assert.assertEquals(2, arr.length);
        Assert.assertTrue("key".equals(arr[0]) && "key2".equals(arr[1]) || "key".equals(arr[1]) && "key2".equals(arr[0]));
        contextFactory.destroyRequestContext(null);
    }

    @Test
    public void testInterceptorWithSuperClassComponent()
    {
        clear();
        defineManagedBean(InterceptorWithSuperClassInterceptedComponent.class);

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        List<AbstractOwbBean<?>> comps = getComponents();

        Object object = getManager().getInstance(comps.get(0));

        Assert.assertTrue(object instanceof InterceptorWithSuperClassInterceptedComponent);

        InterceptorWithSuperClassInterceptedComponent comp = (InterceptorWithSuperClassInterceptedComponent) object;
        Object obj = comp.intercepted();

        Assert.assertTrue(obj instanceof String[]);

        String[] arr = (String[]) obj;

        Assert.assertEquals(1, arr.length);
        Assert.assertTrue("key0".equals(arr[0]));
        contextFactory.destroyRequestContext(null);
    }

    @Test
    public void testMultipleListOfInterceptedComponent()
    {
        clear();
        defineManagedBean(MultipleListOfInterceptedComponent.class);

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        List<AbstractOwbBean<?>> comps = getComponents();

        Object object = getManager().getInstance(comps.get(0));

        Assert.assertTrue(object instanceof MultipleListOfInterceptedComponent);

        MultipleListOfInterceptedComponent comp = (MultipleListOfInterceptedComponent) object;
        Object obj = comp.intercepted();

        Assert.assertTrue(obj instanceof String);

        Assert.assertEquals("ok", (String) obj);

        contextFactory.destroyRequestContext(null);
    }

    @Test
    public void testMultipleListOfInterceptedWithExcludeClassComponent()
    {
        clear();
        defineManagedBean(MultipleListOfInterceptedWithExcludeClassComponent.class);

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        List<AbstractOwbBean<?>> comps = getComponents();

        Object object = getManager().getInstance(comps.get(0));

        Assert.assertTrue(object instanceof MultipleListOfInterceptedWithExcludeClassComponent);

        MultipleListOfInterceptedWithExcludeClassComponent comp = (MultipleListOfInterceptedWithExcludeClassComponent) object;
        Object obj = comp.intercepted();

        Assert.assertTrue(obj instanceof String);

        Assert.assertEquals("value2", (String) obj);

        contextFactory.destroyRequestContext(null);
    }

}

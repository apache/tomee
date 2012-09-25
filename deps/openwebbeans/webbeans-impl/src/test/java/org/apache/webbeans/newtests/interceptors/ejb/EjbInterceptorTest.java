/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.interceptors.ejb;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.beans.ApplicationScopedBean;
import org.apache.webbeans.newtests.interceptors.beans.RequestScopedBean;
import org.junit.Test;

public class EjbInterceptorTest extends AbstractUnitTest
{

    @Test
    public void testEjbInterceptor()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(RequestScopedBean.class);
        beanClasses.add(ManagedBeanWithEjbInterceptor.class);
        beanClasses.add(ApplicationScopedBean.class);
        beanClasses.add(EjbInterceptor.class);
        
        startContainer(beanClasses, beanXmls);        

        ManagedBeanWithEjbInterceptor reference = getInstance(ManagedBeanWithEjbInterceptor.class);
        Assert.assertNotNull(reference);

        EjbInterceptor.CALLED = false;
        reference.sayHello();
        Assert.assertTrue(EjbInterceptor.CALLED);
        
        shutDownContainer();
    }

    @Test
    public void testEjbMethodInterceptor()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(RequestScopedBean.class);
        beanClasses.add(ManagedBeanWithMethodEjbInterceptor.class);
        beanClasses.add(ApplicationScopedBean.class);
        beanClasses.add(EjbInterceptor.class);

        startContainer(beanClasses, beanXmls);

        ManagedBeanWithMethodEjbInterceptor reference = getInstance(ManagedBeanWithMethodEjbInterceptor.class);
        Assert.assertNotNull(reference);

        EjbInterceptor.CALLED = false;
        reference.sayHello();
        Assert.assertTrue(EjbInterceptor.CALLED);

        EjbInterceptor.CALLED = false;
        reference.uninterceptedAction();
        Assert.assertFalse(EjbInterceptor.CALLED);

        shutDownContainer();
    }


    @Test
    public void testDynamicEjbInterceptor()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(ManagedBeanWithoutInterceptor.class);
        beanClasses.add(EjbInterceptor.class);
        addExtension(new EjbInterceptorExtension());

        startContainer(beanClasses, beanXmls);

        ManagedBeanWithoutInterceptor reference = getInstance(ManagedBeanWithoutInterceptor.class);
        Assert.assertNotNull(reference);

        EjbInterceptor.CALLED = false;
        reference.sayHello();
        Assert.assertTrue(EjbInterceptor.CALLED);

        shutDownContainer();
    }
}

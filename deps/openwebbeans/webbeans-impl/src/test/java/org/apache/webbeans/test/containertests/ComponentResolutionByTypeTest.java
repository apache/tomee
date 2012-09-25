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
package org.apache.webbeans.test.containertests;

import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.AnnotationWithBindingMember;
import org.apache.webbeans.test.annotation.binding.AnnotationWithNonBindingMember;
import org.apache.webbeans.test.component.BindingComponent;
import org.apache.webbeans.test.component.NonBindingComponent;
import org.junit.Before;
import org.junit.Test;

public class ComponentResolutionByTypeTest extends TestContext
{
    public @AnnotationWithBindingMember(value = "B", number = 3)
    BindingComponent s1 = null;
    public @AnnotationWithBindingMember(value = "B")
    BindingComponent s2 = null;

    public @AnnotationWithNonBindingMember(value = "B", arg1 = "arg1", arg2 = "arg2")
    NonBindingComponent s3 = null;
    public @AnnotationWithNonBindingMember(value = "B", arg1 = "arg11", arg2 = "arg21")
    NonBindingComponent s4 = null;
    public @AnnotationWithNonBindingMember(value = "C", arg1 = "arg11", arg2 = "arg21")
    NonBindingComponent s5 = null;

    private BeanManagerImpl cont;

    private static final String CLAZZ_NAME = ComponentResolutionByTypeTest.class.getName();

    public ComponentResolutionByTypeTest()
    {
        super(CLAZZ_NAME);
    }
    
    @Before
    public void init()
    {
        cont = WebBeansContext.getInstance().getBeanManagerImpl();
    }


    @Test
    public void testBindingTypeOk() throws Throwable
    {
        cont.getBeans(BindingComponent.class, ComponentResolutionByTypeTest.class.getDeclaredField("s1").getAnnotations());
    }

    @Test
    public void testBindingTypeNonOk() throws Throwable
    {
        cont.getBeans(BindingComponent.class, ComponentResolutionByTypeTest.class.getDeclaredField("s2").getAnnotations());
    }

    @Test
    public void testNonBindingTypeOk1() throws Throwable
    {
        cont.getBeans(NonBindingComponent.class, ComponentResolutionByTypeTest.class.getDeclaredField("s3").getAnnotations());
    }

    @Test
    public void testNonBindingTypeOk2() throws Throwable
    {
        Set<Bean<?>> beans = cont.getBeans(NonBindingComponent.class, ComponentResolutionByTypeTest.class.getDeclaredField("s4").getAnnotations());
        Assert.assertNotNull(beans);
        Assert.assertTrue(beans.isEmpty());
    }

    @Test
    public void testNonBindingTypeNonOk() throws Throwable
    {
        cont.getBeans(NonBindingComponent.class, ComponentResolutionByTypeTest.class.getDeclaredField("s5").getAnnotations());
    }

}

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
package org.apache.webbeans.test.unittests.exception;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.exception.AroundInvokeWithFinalMethodComponent;
import org.apache.webbeans.test.component.exception.AroundInvokeWithSameMethodNameComponent;
import org.apache.webbeans.test.component.exception.AroundInvokeWithStaticMethodComponent;
import org.apache.webbeans.test.component.exception.AroundInvokeWithWrongReturnTypeComponent;
import org.apache.webbeans.test.component.exception.AroundInvokeWithoutParameterComponent;
import org.apache.webbeans.test.component.exception.AroundInvokeWithoutReturnTypeComponent;
import org.apache.webbeans.test.component.exception.FinalComponent;
import org.apache.webbeans.test.component.exception.HasFinalMethodComponent;
import org.apache.webbeans.test.component.exception.MoreThanOneAroundInvokeComponent;
import org.apache.webbeans.test.component.exception.MoreThanOneConstructureComponent;
import org.apache.webbeans.test.component.exception.MoreThanOneConstructureComponent2;
import org.apache.webbeans.test.component.exception.MoreThanOnePostConstructComponent;
import org.apache.webbeans.test.component.exception.MultipleDisposalMethodComponent;
import org.apache.webbeans.test.component.exception.NewComponentBindingComponent;
import org.apache.webbeans.test.component.exception.NoConstructureComponent;
import org.apache.webbeans.test.component.exception.PostContructMethodHasCheckedExceptionComponent;
import org.apache.webbeans.test.component.exception.PostContructMethodHasParameterComponent;
import org.apache.webbeans.test.component.exception.PostContructMethodHasReturnTypeComponent;
import org.apache.webbeans.test.component.exception.PostContructMethodHasStaticComponent;
import org.apache.webbeans.test.component.exception.ProducerTypeStaticComponent;
import org.apache.webbeans.test.component.exception.InnerComponent.InnerInnerComponent;
import org.apache.webbeans.test.component.intercept.NoArgConstructorInterceptorComponent;
import org.junit.Before;
import org.junit.Test;

public class ExceptionComponentTest extends TestContext
{

    public ExceptionComponentTest()
    {
        super(ExceptionComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();

    }

    @Test
    public void testFinal()
    {
        clear();
        defineManagedBean(FinalComponent.class);
    }

    @Test
    public void testAbstract()
    {
        try
        {
            clear();
            defineManagedBean(AbstractOwbBean.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testInner()
    {
        try
        {
            clear();
            defineManagedBean(InnerInnerComponent.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testHasFinalMethod()
    {
        try
        {
            clear();
            defineManagedBean(HasFinalMethodComponent.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void constructorTest()
    {
        try
        {
            clear();
            defineManagedBean(MoreThanOneConstructureComponent.class);
            Assert.fail("expecting an exception!");
        }
        catch (WebBeansConfigurationException e)
        {
            // all ok
            System.out.println("got expected exception: " + e.getMessage());
        }

        try
        {
            clear();
            defineManagedBean(MoreThanOneConstructureComponent2.class);
            // all ok
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
        }

        clear();
        defineManagedBean(NoConstructureComponent.class);
    }

    @Test
    public void testStaticProducerMethod()
    {
        clear();
        defineManagedBean(ProducerTypeStaticComponent.class);
    }

    @Test
    public void testDisposeMethod()
    {
        try
        {
            clear();
            defineManagedBean(MultipleDisposalMethodComponent.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testNewInterface()
    {
        Assert.assertTrue(true); //No more exist
    }

    @Test
    public void testNewBinding()
    {
        try
        {
            clear();
            defineManagedBean(NewComponentBindingComponent.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testNewMethod()
    {
        Assert.assertTrue(true); //No more test in spec
    }

    @Test
    public void testMoreThanOnePostConstruct()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<MoreThanOnePostConstructComponent> component = defineManagedBean(MoreThanOnePostConstructComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testPostConstructHasParameter()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<PostContructMethodHasParameterComponent> component = defineManagedBean(PostContructMethodHasParameterComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testPostConstructHasReturnType()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(PostContructMethodHasReturnTypeComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testPostConstructHasCheckedException()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(PostContructMethodHasCheckedExceptionComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testPostConstructHasStatic()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(PostContructMethodHasStaticComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testMoreThanOneAroundInvoke()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(MoreThanOneAroundInvokeComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testAroundInvokeWithSameMethodName()
    {
        clear();
        defineManagedBean(AroundInvokeWithSameMethodNameComponent.class);
        Bean<?> comp = getComponents().get(0);

        Assert.assertEquals(0, ((AbstractInjectionTargetBean<?>) comp).getInterceptorStack().size());
    }

    @Test
    public void testAroundInvokeWithoutParameter()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(AroundInvokeWithoutParameterComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testAroundInvokeWithoutReturnType()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(AroundInvokeWithoutReturnTypeComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testAroundInvokeWithWrongReturnType()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(AroundInvokeWithWrongReturnTypeComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testAroundInvokeWithStatic()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(AroundInvokeWithStaticMethodComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testAroundInvokeWithFinal()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(AroundInvokeWithFinalMethodComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testNoArgConstructorInterceptor()
    {
        try
        {
            clear();
            AbstractInjectionTargetBean<?> component = defineManagedBean(NoArgConstructorInterceptorComponent.class);
            AnnotatedType annotatedType = getWebBeansContext().getAnnotatedElementFactory().getAnnotatedType(component.getReturnType());
            getWebBeansContext().getEJBInterceptorConfig().configure(annotatedType, component.getInterceptorStack());
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

}

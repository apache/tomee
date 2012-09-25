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
package org.apache.webbeans.test.unittests.decorator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.webbeans.annotation.RequestedScopeLiteral;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.DummyAnnotationLiteral;
import org.apache.webbeans.test.component.CheckWithCheckPaymentDecoratorField;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.decorator.broken.DelegateAttributeIsnotInterface;
import org.apache.webbeans.test.component.decorator.broken.DelegateAttributeMustImplementAllDecoratedTypes;
import org.apache.webbeans.test.component.decorator.broken.MoreThanOneDelegateAttribute;
import org.apache.webbeans.test.component.decorator.broken.PaymentDecorator;
import org.junit.Before;
import org.junit.Test;

public class DecoratorExceptionTest extends TestContext
{
    public DecoratorExceptionTest()
    {
        super(DecoratorExceptionTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }


    @Test
    public void testDelegateAttributeIsnotInterface()
    {
        try
        {
            defineDecorator(DelegateAttributeIsnotInterface.class);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testMoreThanOneDelegateAttribute()
    {
        try
        {
            defineDecorator(MoreThanOneDelegateAttribute.class);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testApplyToSimpleWebBeanFinal()
    {
        // TODO test is incomplete!
    }

    @Test
    public void testApplyToSimpleWebBeanFinalMethodsDecoratorImplements()
    {
        try
        {
            defineDecorator(PaymentDecorator.class);
            defineManagedBean(CheckWithCheckPaymentDecoratorField.class);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testDelegateAttributeMustImplementAllDecoratedTypes()
    {
        try
        {
            defineDecorator(DelegateAttributeMustImplementAllDecoratedTypes.class);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testResolveDuplicateBindingParameterType()
    {
        try
        {

            Set<Type> api = new HashSet<Type>();
            api.add(IPayment.class);

            Annotation[] anns = new Annotation[2];
            anns[0] = new DummyAnnotationLiteral();
            anns[1] = new DummyAnnotationLiteral();

            getManager().resolveDecorators(api, anns);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testResolveNonBindingTypeAnnotation()
    {
        try
        {

            Set<Type> api = new HashSet<Type>();
            api.add(IPayment.class);

            Annotation[] anns = new Annotation[2];
            anns[0] = new RequestedScopeLiteral();

            getManager().resolveDecorators(api, anns);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testResolveApiTypesEmpty()
    {
        try
        {

            Set<Type> api = new HashSet<Type>();

            Annotation[] anns = new Annotation[2];
            anns[0] = new DummyAnnotationLiteral();

            getManager().resolveDecorators(api, anns);
        }
        catch (Exception e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

}

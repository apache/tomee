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
package org.apache.webbeans.test.unittests.inject.named;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.inject.named.NamedFieldWithNamedValue;
import org.apache.webbeans.test.component.inject.named.NamedFieldWithoutNamedValue;
import org.apache.webbeans.test.component.inject.named.NamedOtherWithNamedValue;
import org.apache.webbeans.test.component.inject.named.NamedOtherWithoutNamedValue;
import org.apache.webbeans.util.WebBeansUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NamedTests extends TestContext
{
    public NamedTests()
    {
        super(NamedTests.class.getName());
    }
    
    @Before
    public void init()
    {
        super.init();
    }
    
    
    @Test
    public void testFieldWithNamedValue() throws Exception
    {
        Bean<NamedFieldWithNamedValue> bean = defineManagedBean(NamedFieldWithNamedValue.class);
        Field field = NamedFieldWithNamedValue.class.getDeclaredField("paymentProcessor");

        InjectionPoint point =
            WebBeansContext.getInstance().getInjectionPointFactory().getFieldInjectionPointData(bean, field);
        
        WebBeansUtil.checkInjectionPointNamedQualifier(point);
        
        String value = qulifier(point);
        
        Assert.assertEquals("payment", value);
    }
    
    @Test
    public void testFieldWithoutNamedValue() throws Exception
    {
        Bean<NamedFieldWithoutNamedValue> bean = defineManagedBean(NamedFieldWithoutNamedValue.class);
        Field field = NamedFieldWithoutNamedValue.class.getDeclaredField("paymentProcessor");

        InjectionPoint point =
            WebBeansContext.getInstance().getInjectionPointFactory().getFieldInjectionPointData(bean, field);
        
        WebBeansUtil.checkInjectionPointNamedQualifier(point);
        
        String value = qulifier(point);
        
        Assert.assertEquals("paymentProcessor", value);
        
    }
    
    @Test
    public void testOtherWithNamedValue() throws Exception
    {
        Bean<NamedOtherWithNamedValue> bean = defineManagedBean(NamedOtherWithNamedValue.class);
        Constructor<?> constructor = NamedOtherWithNamedValue.class.getDeclaredConstructor(new Class<?>[]{IPayment.class});

        InjectionPoint point =
            WebBeansContext.getInstance().getInjectionPointFactory().getConstructorInjectionPointData(bean,
                                                                                                       constructor).get(0);
        
        WebBeansUtil.checkInjectionPointNamedQualifier(point);
        
        String value = qulifier(point);
        
        Assert.assertEquals("value", value);
        
    }
    
    @Test(expected=WebBeansConfigurationException.class)
    public void testOtherWithoutNamedValue() throws Exception
    {
        Bean<NamedOtherWithoutNamedValue> bean = defineManagedBean(NamedOtherWithoutNamedValue.class);
        Constructor<?> constructor = NamedOtherWithoutNamedValue.class.getDeclaredConstructor(new Class<?>[]{IPayment.class});

        InjectionPoint point =
            WebBeansContext.getInstance().getInjectionPointFactory().getConstructorInjectionPointData(bean,
                                                                                                       constructor).get(0);
                
        String value = qulifier(point);
        
        Assert.assertEquals("", value);
        
        WebBeansUtil.checkInjectionPointNamedQualifier(point);

    }
    
    
    private String qulifier(InjectionPoint injectionPoint)
    {
        Set<Annotation> qualifierset = injectionPoint.getQualifiers();
        Named namedQualifier = null;
        for(Annotation qualifier : qualifierset)
        {
            if(qualifier.annotationType().equals(Named.class))
            {
                namedQualifier = (Named)qualifier;
                break;
            }
        }
        
        if(namedQualifier != null)
        {
            return namedQualifier.value();
        }
        
        return null;
    } 
}

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
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Decorator;

import junit.framework.Assert;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.Binding1Literal;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.decorator.clean.Account;
import org.apache.webbeans.test.component.decorator.clean.AccountComponent;
import org.apache.webbeans.test.component.decorator.clean.LargeTransactionDecorator;
import org.apache.webbeans.test.component.decorator.clean.ServiceDecorator;
import org.apache.webbeans.test.component.service.IService;
import org.apache.webbeans.test.component.service.ServiceImpl1;
import org.junit.Before;
import org.junit.Test;

public class Decorator1Test extends TestContext
{

    public Decorator1Test()
    {
        super(Decorator1Test.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
        
    }

    @Test
    public void test1()
    {
        clear();
        
        initializeDecoratorType(ServiceDecorator.class);
        initializeDecoratorType(LargeTransactionDecorator.class);        
        
        defineManagedBean(CheckWithCheckPayment.class);
        defineDecorator(ServiceDecorator.class);
        AbstractOwbBean<ServiceImpl1> component = defineManagedBean(ServiceImpl1.class);

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getContextFactory().initRequestContext(null);
        webBeansContext.getContextFactory().initApplicationContext(null);

        ServiceImpl1 serviceImpl = getManager().getInstance(component);
        String s = serviceImpl.service();

        Assert.assertEquals("ServiceDecorator", s);

        Set<Type> apiTyeps = new HashSet<Type>();
        apiTyeps.add(IService.class);

        List<Decorator<?>> decs = getManager().resolveDecorators(apiTyeps, new Annotation[] { new Binding1Literal() });

        ServiceDecorator dec = (ServiceDecorator) getManager().getInstance(decs.get(0));
        Assert.assertEquals(null, dec.getDelegateAttr());

    }

    @Test
    public void test2()
    {
        clear();
        initializeDecoratorType(LargeTransactionDecorator.class);
        
        defineDecorator(LargeTransactionDecorator.class);
        AbstractOwbBean<AccountComponent> component = defineManagedBean(AccountComponent.class);

        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        AccountComponent account = getManager().getInstance(component);

        account.deposit(new BigDecimal(1500));
        account.withdraw(new BigDecimal(3000));

        Set<Type> apiTyeps = new HashSet<Type>();
        apiTyeps.add(Account.class);

        List<Decorator<?>> decs = getManager().resolveDecorators(apiTyeps, new Annotation[] { new DefaultLiteral() });

        LargeTransactionDecorator dec = (LargeTransactionDecorator) getManager().getInstance(decs.get(0));
        Assert.assertEquals(null, dec.getDepositeAmount());
        Assert.assertEquals(null, dec.getWithDrawAmount());

    }

}

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
package org.apache.webbeans.test.unittests.event.component;

import java.lang.annotation.Annotation;

import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.Check;
import org.apache.webbeans.test.annotation.binding.NotAnyLiteral;
import org.apache.webbeans.test.annotation.binding.Role;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.CheckWithMoneyPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.apache.webbeans.test.component.event.normal.ComponentWithObservable1;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves1;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves2;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves3;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves4;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves5;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves6;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves7;
import org.apache.webbeans.test.component.event.normal.TransactionalInterceptor;
import org.apache.webbeans.test.event.LoggedInEvent;
import org.junit.Before;
import org.junit.Test;

public class ObserversComponentTest extends TestContext
{
    public ObserversComponentTest()
    {
        super(ObserversComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testObserves()
    {
        clear();

        AbstractOwbBean<ComponentWithObserves1> component = defineManagedBean(ComponentWithObserves1.class);
        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        LoggedInEvent event = new LoggedInEvent("Gurkan");

        Annotation[] anns = new Annotation[1];
        anns[0] = new AnyLiteral();        

        getManager().fireEvent(event, anns);

        ComponentWithObserves1 instance = getManager().getInstance(component);

        Assert.assertEquals("Gurkan", instance.getUserName());
    }

    @Test
    public void testWithObservable()
    {
        clear();

        getManager().addBean(WebBeansContext.getInstance().getWebBeansUtil().getEventBean());

        AbstractOwbBean<ComponentWithObserves1> component = defineManagedBean(ComponentWithObserves1.class);
        AbstractOwbBean<ComponentWithObservable1> componentObservable = defineManagedBean(ComponentWithObservable1.class);

        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        ComponentWithObserves1 instance = getManager().getInstance(component);
        ComponentWithObservable1 observable = getManager().getInstance(componentObservable);

        observable.afterLoggedIn();

        Assert.assertEquals("Gurkan", instance.getUserName());
    }

    @Test
    public void testObservesIfExists()
    {
        clear();

        getManager().addBean(WebBeansContext.getInstance().getWebBeansUtil().getEventBean());

        AbstractOwbBean<ComponentWithObserves3> component3 = defineManagedBean(ComponentWithObserves3.class);
        AbstractOwbBean<ComponentWithObserves4> component4 = defineManagedBean(ComponentWithObserves4.class);
        AbstractOwbBean<ComponentWithObserves5> component5 = defineManagedBean(ComponentWithObserves5.class);
        AbstractOwbBean<ComponentWithObserves6> component6 = defineManagedBean(ComponentWithObserves6.class);

        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        /*
         * DO NOT CALL getInstance FOR component3! IF_EXISTS NEEDS TO FAIL FOR THAT OBJECT.
         */
        ComponentWithObserves4 instance = getManager().getInstance(component4);
        ComponentWithObserves5 instanceIE = getManager().getInstance(component5);
        ComponentWithObserves6 outstance = getManager().getInstance(component6);
        instanceIE.getUserName();  // This causes the observer to exist in the context, therefore IF_EXISTS is true.
        
        LoggedInEvent event = new LoggedInEvent("Gurkan");

        Annotation[] anns = new Annotation[1];
        anns[0] = new NotAnyLiteral();

        getManager().fireEvent(event, anns);
        
        Assert.assertEquals("IEGurkan", outstance.getUserIEName());
        Assert.assertEquals("Gurkan", outstance.getUserName());
        Assert.assertNull(outstance.getUserNIEName());
    }

    @Test
    public void testObservesWithBindingMember()
    {
        clear();

        getManager().addBean(WebBeansContext.getInstance().getWebBeansUtil().getEventBean());
        
        AbstractOwbBean<ComponentWithObserves1> component = defineManagedBean(ComponentWithObserves1.class);
        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        LoggedInEvent event = new LoggedInEvent("Gurkan");

        class CheckLiteral extends AnnotationLiteral<Check> implements Check
        {

            public String type()
            {
                return "CHECK";
            }

        }

        Annotation[] anns = new Annotation[1];
        anns[0] = new CheckLiteral();

        getManager().fireEvent(event, anns);

        ComponentWithObserves1 instance = getManager().getInstance(component);

        Assert.assertNotNull(instance.getUserName());

        Assert.assertEquals("Gurkan", instance.getUserNameWithMember());
    }

    @Test
    public void testFireWithAtAnyQualifier()
    {
        clear();

        getManager().addBean(WebBeansContext.getInstance().getWebBeansUtil().getEventBean());
        
        AbstractOwbBean<ComponentWithObserves1> component = defineManagedBean(ComponentWithObserves1.class);
        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        LoggedInEvent event = new LoggedInEvent("Mark");

        Annotation[] anns = new Annotation[1];
        anns[0] = new AnyLiteral();

        getManager().fireEvent(event, anns);

        ComponentWithObserves1 instance = getManager().getInstance(component);

        Assert.assertEquals("Mark", instance.getUserName());
        Assert.assertNull(instance.getUserNameWithMember());
    }


    @Test
    public void testObservesWithBindingMember2()
    {
        clear();

        defineInterceptor(TransactionalInterceptor.class);
        defineManagedBean(CheckWithCheckPayment.class);
        defineManagedBean(CheckWithMoneyPayment.class);
        defineManagedBean(PaymentProcessorComponent.class);
        AbstractOwbBean<ComponentWithObserves2> component = defineManagedBean(ComponentWithObserves2.class);
        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        LoggedInEvent event = new LoggedInEvent("USER");

        class RoleUser extends AnnotationLiteral<Role> implements Role
        {

            public String value()
            {
                return "USER";
            }

        }

        class RoleAdmin extends AnnotationLiteral<Role> implements Role
        {

            public String value()
            {
                return "ADMIN";
            }

        }

        ComponentWithObserves2.hasBeenIntercepted = false;
        
        Annotation[] anns = new Annotation[1];
        anns[0] = new RoleUser();

        getManager().fireEvent(event, anns);
        ComponentWithObserves2 instance = getManager().getInstance(component);

        Assert.assertFalse(ComponentWithObserves2.hasBeenIntercepted);
        
        Assert.assertNotNull(instance.getPayment());
        Assert.assertEquals("USER", instance.getUser());

        anns[0] = new RoleAdmin();
        event = new LoggedInEvent("ADMIN");
        
        getManager().fireEvent(event, anns);
        instance = getManager().getInstance(component);

        Assert.assertTrue(ComponentWithObserves2.hasBeenIntercepted);
        Assert.assertNotNull(instance.getPayment());
        Assert.assertEquals("ADMIN", instance.getUser());

        // lessons learned: do it again sam! ;)
        ComponentWithObserves2.hasBeenIntercepted = false;
        getManager().fireEvent(event, anns);
        instance = getManager().getInstance(component);

        Assert.assertTrue(ComponentWithObserves2.hasBeenIntercepted);
        Assert.assertNotNull(instance.getPayment());
        Assert.assertEquals("ADMIN", instance.getUser());

    }
    
    @Test
    public void testObservesWithEventInjection()
    {
        clear();

        getManager().addBean(WebBeansContext.getInstance().getWebBeansUtil().getEventBean());

        AbstractOwbBean<ComponentWithObserves7> component = defineManagedBean(ComponentWithObserves7.class);
        AbstractOwbBean<ComponentWithObservable1> componentObservable = defineManagedBean(ComponentWithObservable1.class);

        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        ComponentWithObserves7 instance = getManager().getInstance(component);
        ComponentWithObservable1 observable = getManager().getInstance(componentObservable);

        observable.afterLoggedIn();

        Assert.assertEquals("Gurkan", instance.getUserName());
        Assert.assertEquals("Rohit_Kelapure", instance.getEventString());
    }    
    
}
